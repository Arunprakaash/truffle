package com.truffleapp.truffle.data

import java.time.LocalDate
import kotlin.random.Random

/**
 * Picks one reflection line per [date] from a small curated pool for the current
 * [LedgerData] "situation". Same inputs always yield the same line (stable across recompositions).
 */
fun pickReflection(data: LedgerData, date: LocalDate): String {
    val situation = reflectionSituation(data)
    val lines     = REFLECTION_LINES.getValue(situation)
    val seed      =
        date.toEpochDay() * 1_000_003L +
            (31L * data.user.firstName.hashCode()) +
            (1009L * situation.ordinal)
    return lines[Random(seed).nextInt(lines.size)]
}

private enum class ReflectionSituation {
    QuietMonth,
    OutflowShape,
    SlowSpend,
    AlignedSpend,
    HeavySpend,
}

private fun reflectionSituation(data: LedgerData): ReflectionSituation = when {
    data.inflow <= 0.0 && data.outflow <= 0.0 -> ReflectionSituation.QuietMonth
    data.inflow <= 0.0                       -> ReflectionSituation.OutflowShape
    data.spendRatio < 0.45                   -> ReflectionSituation.SlowSpend
    data.spendRatio < 0.70                   -> ReflectionSituation.AlignedSpend
    else                                     -> ReflectionSituation.HeavySpend
}

private val REFLECTION_LINES = mapOf(
    ReflectionSituation.QuietMonth to listOf(
        "Nothing has moved yet. A calm place to begin.",
        "The ledger is quiet. That counts as information too.",
        "Stillness is not absence — it is room before the next chapter.",
        "No numbers to chase today. Rest is part of the work.",
    ),
    ReflectionSituation.OutflowShape to listOf(
        "A different shape of week — judgment can wait.",
        "Money moved out before new income landed. Curiosity, not alarm.",
        "Some weeks arrive sideways. You can look when you are ready.",
        "The picture is incomplete. That is allowed.",
    ),
    ReflectionSituation.SlowSpend to listOf(
        "A slow week. Not every month needs a plan.",
        "Your spending stayed small beside what came in. Breathing room.",
        "Light outflows this month — space for what matters next.",
        "You held more than you released. That is its own kind of wealth.",
    ),
    ReflectionSituation.AlignedSpend to listOf(
        "You are spending in line with what you value.",
        "In and out are telling a steady story together.",
        "What arrived and what left are in conversation, not conflict.",
        "The balance between income and spending feels intentional.",
    ),
    ReflectionSituation.HeavySpend to listOf(
        "This week held more than usual. Return without judgment.",
        "Outflows rose — no verdict, only a mirror.",
        "When spending runs warm, gentleness helps more than a spreadsheet.",
        "A fuller week of letting go. You can revisit it when the dust settles.",
    ),
)
