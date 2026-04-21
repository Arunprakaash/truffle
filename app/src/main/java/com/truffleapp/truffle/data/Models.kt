package com.truffleapp.truffle.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Pure data — no UI imports.
// Icon keys (e.g. "coffee", "cart") are mapped to Material icons in the UI layer.

data class User(val firstName: String)

data class Transaction(
    val id: String,
    val date: String,
    val time: String,
    val merchant: String,
    val note: String,
    val amount: Double,       // negative = outflow, positive = inflow
    val category: String,     // key into CATEGORIES map
    val icon: String,         // icon key for IconCircle
    val account: String,
    /** For date-range filters (Flow). `0` = legacy / preview data — always included in every range. */
    val recordedEpochDay: Long = 0L,
    val lat: Double? = null,
    val lng: Double? = null,
)

enum class BillRecurrence {
    None,
    Weekly,
    Monthly,
    Yearly,
}

fun parseBillRecurrence(raw: String): BillRecurrence =
    when (raw.trim().uppercase()) {
        "WEEKLY" -> BillRecurrence.Weekly
        "MONTHLY" -> BillRecurrence.Monthly
        "YEARLY" -> BillRecurrence.Yearly
        else -> BillRecurrence.None
    }

fun BillRecurrence.toPersistCode(): String =
    when (this) {
        BillRecurrence.None -> "NONE"
        else -> name.uppercase()
    }

fun BillRecurrence.pickerLabel(): String =
    when (this) {
        BillRecurrence.None -> "Does not repeat"
        BillRecurrence.Weekly -> "Every week"
        BillRecurrence.Monthly -> "Every month"
        BillRecurrence.Yearly -> "Every year"
    }

fun BillRecurrence.rowHint(): String =
    when (this) {
        BillRecurrence.None -> ""
        BillRecurrence.Weekly -> "Weekly"
        BillRecurrence.Monthly -> "Monthly"
        BillRecurrence.Yearly -> "Yearly"
    }

/** One-off: set paid. Recurring: keep unpaid and advance due date by one period. */
fun Bill.appliedAfterMarkPaid(): Bill =
    if (recurrence == BillRecurrence.None) {
        copy(paid = true)
    } else {
        val d = LocalDate.ofEpochDay(dueDateEpoch)
        val next = when (recurrence) {
            BillRecurrence.Weekly -> d.plusWeeks(1)
            BillRecurrence.Monthly -> d.plusMonths(1)
            BillRecurrence.Yearly -> d.plusYears(1)
            BillRecurrence.None -> d
        }
        copy(paid = false, dueDateEpoch = next.toEpochDay())
    }

data class Bill(
    val id: String,
    val label: String,
    val amount: Double,       // always positive — sign applied in display
    /** Local calendar day when the bill is due ([LocalDate.toEpochDay]). */
    val dueDateEpoch: Long,
    val paid: Boolean,
    val account: String,
    val recurrence: BillRecurrence = BillRecurrence.None,
) {
    /** Whole days from [fromEpochDay] until the due date (negative if overdue). */
    fun daysUntilDue(fromEpochDay: Long = LocalDate.now().toEpochDay()): Int =
        ChronoUnit.DAYS.between(LocalDate.ofEpochDay(fromEpochDay), LocalDate.ofEpochDay(dueDateEpoch)).toInt()

    fun dueDateShortLabel(): String =
        LocalDate.ofEpochDay(dueDateEpoch).format(DateTimeFormatter.ofPattern("MMM d"))

    /** Short phrase for subtitles (e.g. "Due in 3 days", "Due today", "3 days overdue"). */
    fun relativeDuePhrase(fromEpochDay: Long = LocalDate.now().toEpochDay()): String {
        val d = daysUntilDue(fromEpochDay)
        return when {
            d < 0 -> "${-d} days overdue"
            d == 0 -> "Due today"
            d == 1 -> "Due tomorrow"
            else -> "Due in $d days"
        }
    }
}

data class Account(
    val id: String,
    val name: String,
    val institution: String,
    val balance: Double,      // negative for credit balances owed
    val kind: AccountKind,
    /** ISO 4217; amounts for this account are in this currency. */
    val currency: String = DEFAULT_LEDGER_CURRENCY,
    /**
     * Credit only: max principal owed (positive number). Spending is blocked when
     * `balance - expense < -creditLimit`. **0** means no cap in the app (legacy behavior).
     */
    val creditLimit: Double = 0.0,
)

/** Positive [expenseAmount] (outflow magnitude). */
fun Account.canCoverExpense(expenseAmount: Double): Boolean {
    if (expenseAmount <= 0) return true
    return when (kind) {
        AccountKind.Credit -> {
            if (creditLimit <= 0) return true
            balance - expenseAmount >= -creditLimit - 1e-9
        }
        else -> balance + 1e-9 >= expenseAmount
    }
}

enum class AccountKind { Cash, Invest, Credit }

/** Bills tied to a deleted account are relabeled; not a real [Account] row. */
const val UNASSIGNED_ACCOUNT_LABEL = "Unassigned"

data class Goal(
    val id: String,
    val title: String,
    val note: String,
    val saved: Double,
    val target: Double,
) {
    val progress: Float get() = (saved / target).toFloat().coerceIn(0f, 1f)
    val isComplete: Boolean get() = saved >= target
}

data class Budget(
    val id: String,
    val label: String,
    val icon: String,
    val spent: Double,
    val limit: Double,
) {
    val progress: Float get() = (spent / limit).toFloat().coerceIn(0f, 1f)
    val isOver: Boolean get() = spent > limit
}

data class WeeklyFlow(val week: String, val inflow: Double, val outflow: Double) {
    val net: Double get() = inflow - outflow
}

data class BillSuggestion(
    val merchant: String,
    val amount: Double,
    val nextDueDateEpoch: Long,
    val account: String,
) {
    val key: String get() = merchant.trim().lowercase()
}

data class LedgerData(
    val user: User,
    val netWorth: Double,
    val netWorthLastMonth: Double,
    val inflow: Double,
    val outflow: Double,
    /** ISO 4217 for goals, budgets, and aggregates that are not tied to a single account. */
    val displayCurrency: String = DEFAULT_LEDGER_CURRENCY,
    val accounts: List<Account>,
    val transactions: List<Transaction>,
    val bills: List<Bill>,
    val goals: List<Goal>,
    val budgets: List<Budget>,
    val weeklyFlow: List<WeeklyFlow>,
) {
    val netWorthDelta: Double get() = netWorth - netWorthLastMonth
    val spendRatio: Double get() = outflow / inflow
    val remainder: Double get() = inflow - outflow
}
