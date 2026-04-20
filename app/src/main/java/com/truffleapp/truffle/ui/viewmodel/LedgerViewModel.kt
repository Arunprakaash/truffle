package com.truffleapp.truffle.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.truffleapp.truffle.data.Account
import com.truffleapp.truffle.data.AccountKind
import com.truffleapp.truffle.data.Bill
import com.truffleapp.truffle.data.appliedAfterMarkPaid
import com.truffleapp.truffle.data.Goal
import com.truffleapp.truffle.data.BackupImportPreview
import com.truffleapp.truffle.data.ImportBackupResult
import com.truffleapp.truffle.data.Transaction
import com.truffleapp.truffle.data.canCoverExpense
import com.truffleapp.truffle.data.UNASSIGNED_ACCOUNT_LABEL
import com.truffleapp.truffle.data.normalizeLedgerCurrencyCode
import com.truffleapp.truffle.data.db.LedgerRepository
import com.truffleapp.truffle.reminders.BillReminderNotifications
import com.truffleapp.truffle.reminders.BillReminderScheduler

class LedgerViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = LedgerRepository(application)

    var hasOnboarded by mutableStateOf(repo.readHasOnboarded())
        private set

    var data by mutableStateOf(repo.loadLedgerData())
        private set

    // ── Onboarding ─────────────────────────────────────────────────────────

    fun completeOnboarding(name: String, account: Account) {
        repo.completeOnboarding(name, account)
        hasOnboarded = true
        data         = repo.loadLedgerData()
        syncBillReminderWork()
    }

    fun setDisplayCurrency(code: String) {
        data = repo.persist(data.copy(displayCurrency = normalizeLedgerCurrencyCode(code)))
    }

    fun addAccount(account: Account) {
        data = repo.persist(data.copy(accounts = data.accounts + account))
    }

    fun updateAccount(account: Account) {
        val old = data.accounts.find { it.id == account.id } ?: return
        val oldName = old.name.trim()
        val newName = account.name.trim()
        var next = data.copy(
            accounts = data.accounts.map { if (it.id == account.id) account else it },
        )
        if (oldName.isNotEmpty() && oldName != newName) {
            next = next.copy(
                transactions = next.transactions.map { tx ->
                    if (tx.account.trim() == oldName) tx.copy(account = newName) else tx
                },
                bills = next.bills.map { bill ->
                    if (bill.account.trim() == oldName) bill.copy(account = newName) else bill
                },
            )
        }
        data = repo.persist(next)
    }

    fun exportBackupJson(): String = repo.exportBackupJson()

    fun peekBackupPreview(json: String): BackupImportPreview? = repo.peekBackupPreview(json)

    fun importBackupJson(json: String): ImportBackupResult {
        val result = repo.importBackupJson(json)
        if (result is ImportBackupResult.Success) {
            hasOnboarded = repo.readHasOnboarded()
            data         = repo.loadLedgerData()
            syncBillReminderWork()
        }
        return result
    }

    fun clearAllData() {
        repo.clearAllDataAndResetOnboarding()
        hasOnboarded = repo.readHasOnboarded()
        data         = repo.loadLedgerData()
        BillReminderNotifications.cancelSummary(getApplication())
        syncBillReminderWork()
    }

    fun deleteAccount(accountId: String) {
        val acc = data.accounts.find { it.id == accountId } ?: return
        val nameKey = acc.name.trim()
        val next = data.copy(
            accounts = data.accounts.filter { it.id != accountId },
            transactions = data.transactions.filter { it.account.trim() != nameKey },
            bills = data.bills.map { bill ->
                if (bill.account.trim() == nameKey) bill.copy(account = UNASSIGNED_ACCOUNT_LABEL) else bill
            },
        )
        data = repo.persist(next)
    }

    // ── Transactions ───────────────────────────────────────────────────────

    /**
     * Records a transaction and updates the named account balance.
     * Expenses cannot pull cash or investment balances below zero (credit is uncapped here).
     */
    fun addTransaction(tx: Transaction): Boolean {
        if (tx.amount < 0.0) {
            val key = tx.account.trim()
            val acc = data.accounts.find { it.name.trim() == key } ?: return false
            if (!acc.canCoverExpense(-tx.amount)) return false
        }
        data = repo.persist(
            data.copy(
                transactions = listOf(tx) + data.transactions,
                accounts     = data.accounts.adjustBalanceForAccountNamed(tx.account, tx.amount),
            ),
        )
        return true
    }

    fun removeTransaction(txId: String) {
        val tx = data.transactions.find { it.id == txId } ?: return
        data = repo.persist(
            data.copy(
                transactions = data.transactions.filter { it.id != txId },
                accounts     = data.accounts.adjustBalanceForAccountNamed(tx.account, -tx.amount),
            ),
        )
    }

    fun recategorize(txId: String, category: String) {
        data = repo.persist(
            data.copy(
                transactions = data.transactions.map { t ->
                    if (t.id == txId) t.copy(category = category) else t
                },
            ),
        )
    }

    // ── Bills ──────────────────────────────────────────────────────────────

    fun addBill(bill: Bill) {
        data = repo.persist(data.copy(bills = data.bills + bill))
        syncBillReminderWork()
    }

    /** Marks the bill paid and posts its amount from the linked account when one is set. */
    fun markBillPaid(billId: String): Boolean {
        val bill = data.bills.find { it.id == billId } ?: return false
        val key = bill.account.trim()
        val acc = if (key.isNotEmpty() && key != UNASSIGNED_ACCOUNT_LABEL) {
            data.accounts.find { it.name.trim() == key }
        } else {
            null
        }
        if (acc != null && !acc.canCoverExpense(bill.amount)) return false
        data = repo.persist(
            data.copy(
                bills = data.bills.map { b ->
                    if (b.id == billId) b.appliedAfterMarkPaid() else b
                },
                accounts = if (acc != null) {
                    data.accounts.adjustBalanceForAccountNamed(bill.account, -bill.amount)
                } else {
                    data.accounts
                },
            ),
        )
        syncBillReminderWork()
        return true
    }

    // ── Goals ──────────────────────────────────────────────────────────────

    fun addGoal(goal: Goal) {
        data = repo.persist(data.copy(goals = data.goals + goal))
    }

    /** Moves [amount] from the given cash/invest account into the goal (credit accounts are ignored). */
    fun addToGoal(goalId: String, amount: Double, fromAccountId: String): Boolean {
        if (amount <= 0.0 || fromAccountId.isBlank()) return false
        val acc = data.accounts.find { it.id == fromAccountId } ?: return false
        if (acc.kind == AccountKind.Credit) return false
        if (acc.balance + 1e-9 < amount) return false
        data = repo.persist(
            data.copy(
                goals = data.goals.map { goal ->
                    if (goal.id == goalId) goal.copy(saved = goal.saved + amount) else goal
                },
                accounts = data.accounts.map { a ->
                    if (a.id == fromAccountId) a.copy(balance = a.balance - amount) else a
                },
            ),
        )
        return true
    }

    /** Updates persisted monthly limits; spending stays derived from transactions. */
    fun updateBudgetLimits(limitsById: Map<String, Double>) {
        if (limitsById.isEmpty()) return
        data = repo.persist(
            data.copy(
                budgets = data.budgets.map { b ->
                    limitsById[b.id]?.let { lim -> b.copy(limit = lim) } ?: b
                },
            ),
        )
    }

    private fun syncBillReminderWork() {
        BillReminderScheduler.schedule(getApplication())
    }

    private fun List<Account>.adjustBalanceForAccountNamed(accountName: String, delta: Double): List<Account> {
        val key = accountName.trim()
        if (key.isEmpty()) return this
        return map { acc ->
            if (acc.name == key) acc.copy(balance = acc.balance + delta) else acc
        }
    }
}
