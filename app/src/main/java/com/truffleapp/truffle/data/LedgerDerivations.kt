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

fun ledgerWithDerivedBudgetsAndWeekly(ledger: LedgerData): LedgerData =
    ledger.copy(
        budgets    = budgetsWithSpentFromTransactions(ledger.budgets, ledger.transactions),
        weeklyFlow = weeklyFlowFromTransactions(ledger.transactions),
    )
