package com.truffleapp.truffle.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.truffleapp.truffle.data.LedgerData
import com.truffleapp.truffle.data.SampleData
import com.truffleapp.truffle.data.Transaction
import com.truffleapp.truffle.ui.components.Caps
import com.truffleapp.truffle.ui.components.MoneyText
import com.truffleapp.truffle.ui.components.TxRow
import com.truffleapp.truffle.ui.components.fmt
import com.truffleapp.truffle.ui.theme.ColorFeature2
import com.truffleapp.truffle.ui.theme.ColorSurface
import com.truffleapp.truffle.ui.theme.ColorTextPrimary
import com.truffleapp.truffle.ui.theme.ColorTextSecondary
import com.truffleapp.truffle.ui.theme.ColorTextTertiary
import com.truffleapp.truffle.ui.theme.SansFamily
import com.truffleapp.truffle.ui.theme.SerifFamily
import com.truffleapp.truffle.ui.theme.StillwaterTheme
import java.time.LocalDate

private val CardShape = RoundedCornerShape(14.dp)
private val PillShape = RoundedCornerShape(999.dp)

@Composable
fun FlowScreen(
    data: LedgerData,
    modifier: Modifier = Modifier,
    onTx: (Transaction) -> Unit = {},
) {
    var range  by remember { mutableStateOf("week") }
    var filter by remember { mutableStateOf("all") }

    val rangeFiltered = remember(data.transactions, range) {
        val today = LocalDate.now()
        val minDay = when (range) {
            "month" -> today.toEpochDay() - 29
            "year"  -> today.toEpochDay() - 364
            else    -> today.toEpochDay() - 6 // week ≈ last 7 days
        }
        data.transactions.filter { tx ->
            tx.recordedEpochDay == 0L || tx.recordedEpochDay >= minDay
        }
    }

    val filtered = remember(rangeFiltered, filter) {
        rangeFiltered.filter { tx ->
            when (filter) {
                "in"  -> tx.amount > 0
                "out" -> tx.amount < 0
                else  -> true
            }
        }
    }

    // groupBy preserves insertion order — dates stay newest-first
    val grouped  = remember(filtered) { filtered.groupBy { it.date } }
    val totalOut = remember(filtered) { filtered.filter { it.amount < 0 }.sumOf { it.amount } }
    val totalIn  = remember(filtered) { filtered.filter { it.amount > 0 }.sumOf { it.amount } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 18.dp)
            .padding(top = 12.dp, bottom = 100.dp),
    ) {
        // ── Summary header ─────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .padding(top = 8.dp, bottom = 4.dp),
        ) {
            Text(
                text = when (range) {
                    "month" -> "This month you released"
                    "year"  -> "This year you released"
                    else    -> "This week you released"
                },
                style = TextStyle(
                    fontFamily = SerifFamily,
                    fontStyle  = FontStyle.Italic,
                    fontSize   = 16.sp,
                    color      = ColorTextSecondary,
                    lineHeight = (16 * 1.5).sp,
                ),
                modifier = Modifier.padding(bottom = 6.dp),
            )

            // totalOut is negative — MoneyText will prefix with "− "
            MoneyText(amount = totalOut, size = 32.sp)

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontFamily = SerifFamily, fontStyle = FontStyle.Italic)) {
                        append("and received ")
                    }
                    withStyle(
                        SpanStyle(
                            fontFamily         = SerifFamily,
                            fontFeatureSettings = "\"tnum\" on",
                        )
                    ) {
                        append(fmt(totalIn, cents = true))
                    }
                },
                style    = TextStyle(fontSize = 12.sp, color = ColorTextTertiary),
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        // ── Range selector (Week / Month / Year) ───────────────────────────
        RangeSelector(
            selected = range,
            onSelect = { range = it },
            modifier = Modifier.padding(top = 18.dp),
        )

        // ── Filter chips (All / In / Out) ──────────────────────────────────
        Row(
            modifier = Modifier
                .padding(top = 14.dp, bottom = 10.dp)
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf("all" to "All", "in" to "In", "out" to "Out").forEach { (value, label) ->
                FilterChip(
                    label    = label,
                    isActive = filter == value,
                    onClick  = { filter = value },
                )
            }
        }

        // ── Grouped transaction list ───────────────────────────────────────
        if (filtered.isEmpty()) {
            Spacer(Modifier.height(14.dp))
            FlowEmptyHintCard(hasAnyTransactions = data.transactions.isNotEmpty())
        } else {
            grouped.entries.forEach { (date, items) ->
                Spacer(Modifier.height(14.dp))

                Caps(
                    text     = date,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .padding(bottom = 8.dp),
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CardShape)
                        .background(ColorSurface)
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                ) {
                    items.forEachIndexed { i, tx ->
                        TxRow(tx = tx, isLast = i == items.lastIndex, onClick = { onTx(tx) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowEmptyHintCard(hasAnyTransactions: Boolean) {
    val message = if (hasAnyTransactions) {
        "Nothing matches this filter. Try All, In, or Out."
    } else {
        "No transactions yet. When something moves, add it from Today with the + button."
    }
    Text(
        text = message,
        style = TextStyle(
            fontFamily = SansFamily,
            fontSize   = 13.sp,
            color      = ColorTextSecondary,
            lineHeight = (13 * 1.55).sp,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(ColorSurface)
            .padding(horizontal = 18.dp, vertical = 20.dp),
    )
}

// ── Range selector — pill segmented control ────────────────────────────────
// Surface bg, feature-2 active pill, 320ms color animation.
@Composable
private fun RangeSelector(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf("week" to "Week", "month" to "Month", "year" to "Year")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(PillShape)
            .background(ColorSurface)
            .padding(2.dp),
    ) {
        options.forEach { (value, label) ->
            val isActive = selected == value
            val color by animateColorAsState(
                targetValue    = if (isActive) ColorTextPrimary else ColorTextTertiary,
                animationSpec  = tween(320),
                label          = "range_$value",
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(PillShape)
                    .background(if (isActive) ColorFeature2 else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = { onSelect(value) },
                    )
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text  = label.uppercase(),
                    style = TextStyle(
                        fontFamily    = SansFamily,
                        fontWeight    = FontWeight.Medium,
                        fontSize      = 11.sp,
                        letterSpacing = 0.12.em,
                        color         = color,
                    ),
                )
            }
        }
    }
}

// ── Filter chip — "All / In / Out" ────────────────────────────────────────
@Composable
private fun FilterChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val color by animateColorAsState(
        targetValue   = if (isActive) ColorTextPrimary else ColorTextTertiary,
        animationSpec = tween(320),
        label         = "filterChip_$label",
    )

    Box(
        modifier = Modifier
            .clip(PillShape)
            .background(if (isActive) ColorFeature2 else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text  = label.uppercase(),
            style = TextStyle(
                fontFamily    = SansFamily,
                fontWeight    = FontWeight.Medium,
                fontSize      = 11.sp,
                letterSpacing = 0.08.em,
                color         = color,
            ),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1EB, showSystemUi = true)
@Composable
private fun FlowScreenPreview() {
    StillwaterTheme {
        FlowScreen(data = SampleData)
    }
}
