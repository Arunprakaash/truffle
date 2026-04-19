package com.truffleapp.truffle.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.truffleapp.truffle.data.Account
import com.truffleapp.truffle.data.Bill
import com.truffleapp.truffle.data.Goal
import com.truffleapp.truffle.data.BackupImportPreview
import com.truffleapp.truffle.data.ImportBackupResult
import com.truffleapp.truffle.data.Transaction
import com.truffleapp.truffle.data.UNASSIGNED_ACCOUNT_LABEL
import com.truffleapp.truffle.data.db.LedgerRepository

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
        }
        return result
    }

    fun clearAllData() {
        repo.clearAllDataAndResetOnboarding()
        hasOnboarded = repo.readHasOnboarded()
        data         = repo.loadLedgerData()
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

    fun addTransaction(tx: Transaction) {
        data = repo.persist(
            data.copy(
                transactions = listOf(tx) + data.transactions,
                accounts     = data.accounts.adjustBalanceForAccountNamed(tx.account, tx.amount),
            ),
        )
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
    }

    fun markBillPaid(billId: String) {
        data = repo.persist(
            data.copy(
                bills = data.bills.map { bill ->
                    if (bill.id == billId) bill.copy(paid = true) else bill
                },
            ),
        )
    }

    // ── Goals ──────────────────────────────────────────────────────────────

    fun addGoal(goal: Goal) {
        data = repo.persist(data.copy(goals = data.goals + goal))
    }

    fun addToGoal(goalId: String, amount: Double) {
        data = repo.persist(
            data.copy(
                goals = data.goals.map { goal ->
                    if (goal.id == goalId) goal.copy(saved = goal.saved + amount) else goal
                },
            ),
        )
    }

    private fun List<Account>.adjustBalanceForAccountNamed(accountName: String, delta: Double): List<Account> {
        val key = accountName.trim()
        if (key.isEmpty()) return this
        return map { acc ->
            if (acc.name == key) acc.copy(balance = acc.balance + delta) else acc
        }
    }
}
