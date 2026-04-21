package com.truffleapp.truffle.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

private val DEFAULT_BUDGET_LIMITS: Map<String, Double> = mapOf(
    "food"     to 600.0,
    "groc"     to 450.0,
    "home"     to 2_100.0,
    "trans"    to 250.0,
    "learn"    to 150.0,
    "gifts"    to 300.0,
    "wellness" to 220.0,
)

private fun spendingByCategory(transactions: List<Transaction>): Map<String, Double> =
    transactions
        .filter { it.amount < 0.0 }
        .groupBy { it.category }
        .mapValues { (_, txs) -> txs.sumOf { -it.amount } }

private fun defaultBudgets(spent: Map<String, Double>): List<Budget> =
    RECATEGORIZABLE.map { (id, info) ->
        Budget(
            id    = id,
            label = info.label,
            icon  = info.icon,
            spent = spent[id] ?: 0.0,
            limit = DEFAULT_BUDGET_LIMITS[id] ?: 500.0,
        )
    }

/** Recomputes each budget’s spent from outflow transactions (category id = budget id). */
fun budgetsWithSpentFromTransactions(
    template: List<Budget>,
    transactions: List<Transaction>,
): List<Budget> {
    val spent = spendingByCategory(transactions)
    if (template.isEmpty()) return defaultBudgets(spent)
    return template.map { b -> b.copy(spent = spent[b.id] ?: 0.0) }
}

/** Seven Monday-based weeks (oldest → newest) for the sparkline; only dated transactions. */
fun weeklyFlowFromTransactions(transactions: List<Transaction>): List<WeeklyFlow> {
    val dated = transactions.filter { it.recordedEpochDay > 0L }
    if (dated.isEmpty()) return emptyList()

    val today        = LocalDate.now()
    val thisMonday   = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val oldestMonday = thisMonday.minusWeeks(6L)
    val labelFmt     = DateTimeFormatter.ofPattern("MMM d")

    return (0..6).map { w ->
        val weekStart = oldestMonday.plusWeeks(w.toLong())
        val start      = weekStart.toEpochDay()
        val end        = weekStart.plusDays(6).toEpochDay()
        val slice      = dated.filter { it.recordedEpochDay in start..end }
        val inflow     = slice.filter { it.amount > 0.0 }.sumOf { it.amount }
        val outflow    = slice.filter { it.amount < 0.0 }.sumOf { -it.amount }
        val label      = if (weekStart == thisMonday) "This week" else weekStart.format(labelFmt)
        WeeklyFlow(week = label, inflow = inflow, outflow = outflow)
    }
}

/**
 * Scans expense transactions for monthly recurring patterns and suggests bills.
 * A suggestion is raised when the same merchant appears ≥2 times with ~30-day
 * intervals (25–35 days) and amounts within ±10% of each other.
 */
fun detectRecurringBills(
    transactions: List<Transaction>,
    existingBills: List<Bill>,
    dismissed: Set<String> = emptySet(),
): List<BillSuggestion> {
    val today         = LocalDate.now().toEpochDay()
    val existingKeys  = existingBills.map { it.label.trim().lowercase() }.toSet()

    return transactions
        .filter { it.amount < 0.0 && it.recordedEpochDay > 0L }
        .groupBy { it.merchant.trim().lowercase() }
        .filterKeys { it !in existingKeys && it !in dismissed && it.isNotBlank() }
        .mapNotNull { (_, txs) ->
            if (txs.size < 2) return@mapNotNull null
            val sorted    = txs.sortedBy { it.recordedEpochDay }
            val intervals = sorted.zipWithNext { a, b -> b.recordedEpochDay - a.recordedEpochDay }
            val avgInterval = intervals.average()
            if (avgInterval !in 25.0..35.0) return@mapNotNull null
            if (intervals.any { it < 20 || it > 40 }) return@mapNotNull null
            val amounts   = sorted.map { -it.amount }
            val avgAmount = amounts.average()
            if (amounts.any { kotlin.math.abs(it - avgAmount) / avgAmount > 0.10 }) return@mapNotNull null
            val nextDue   = sorted.last().recordedEpochDay + avgInterval.toLong()
            if (nextDue < today - 7) return@mapNotNull null
            BillSuggestion(
                merchant        = sorted.last().merchant.trim(),
                amount          = avgAmount,
                nextDueDateEpoch = nextDue,
                account         = sorted.last().account,
            )
        }
        .sortedByDescending { it.nextDueDateEpoch }
}

fun ledgerWithDerivedBudgetsAndWeekly(ledger: LedgerData): LedgerData =
    ledger.copy(
        budgets    = budgetsWithSpentFromTransactions(ledger.budgets, ledger.transactions),
        weeklyFlow = weeklyFlowFromTransactions(ledger.transactions),
    )
