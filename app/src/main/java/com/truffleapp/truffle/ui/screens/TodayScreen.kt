package com.truffleapp.truffle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.truffleapp.truffle.data.Budget
import com.truffleapp.truffle.data.LedgerData
import com.truffleapp.truffle.data.SampleData
import com.truffleapp.truffle.data.ledgerWithDerivedBudgetsAndWeekly
import com.truffleapp.truffle.data.pickReflection
import com.truffleapp.truffle.data.Transaction
import com.truffleapp.truffle.data.Bill
import com.truffleapp.truffle.data.currencyForAccountName
import com.truffleapp.truffle.data.primaryAmountCurrency
import com.truffleapp.truffle.data.WeeklyFlow
import com.truffleapp.truffle.navigation.NavDestination
import com.truffleapp.truffle.ui.components.BottomNavContentPadding
import com.truffleapp.truffle.ui.components.BillRow
import com.truffleapp.truffle.ui.components.Caps
import com.truffleapp.truffle.ui.components.Hairline
import com.truffleapp.truffle.ui.components.IntentionCard
import com.truffleapp.truffle.ui.components.MoneyText
import com.truffleapp.truffle.ui.components.ProgressBar
import com.truffleapp.truffle.ui.components.SectionHeader
import com.truffleapp.truffle.ui.components.TxRow
import com.truffleapp.truffle.ui.components.fmt
import com.truffleapp.truffle.ui.theme.ColorBorderPrimary
import com.truffleapp.truffle.ui.theme.ColorFeature
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorMuted
import com.truffleapp.truffle.ui.theme.ColorSurface
import com.truffleapp.truffle.ui.theme.ColorTextPrimary
import com.truffleapp.truffle.ui.theme.ColorTextSecondary
import com.truffleapp.truffle.ui.theme.ColorTextSerifBody
import com.truffleapp.truffle.ui.theme.ColorTextSerifMuted
import com.truffleapp.truffle.ui.theme.ColorTextTertiary
import com.truffleapp.truffle.ui.theme.SansFamily
import com.truffleapp.truffle.ui.theme.SerifFamily
import com.truffleapp.truffle.ui.theme.StillwaterTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Canvas
import androidx.compose.foundation.Canvas

private val CardShape = RoundedCornerShape(14.dp)

private fun nearlyZero(value: Double) = kotlin.math.abs(value) < 0.005

// ── Today / Journal screen ────────────────────────────────────────────────
// Matches HomeJournal from screens-home.jsx.
// Screen handles its own scroll and bottom padding — caller passes fillMaxSize.

@Composable
fun TodayScreen(
    data: LedgerData,
    modifier: Modifier = Modifier,
    onTx: (Transaction) -> Unit = {},
    onBill: (Bill) -> Unit = {},
    onNav: (NavDestination) -> Unit = {},
    onAdd: () -> Unit = {},
) {
    val topTx      = remember(data) { data.transactions.take(4) }
    val upcoming   = remember(data) {
        data.bills.filter { !it.paid }.sortedBy { it.dueDateEpoch }.take(3)
    }
    val today      = LocalDate.now()
    val reflection = remember(data, today) { pickReflection(data, today) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()                   // push content below status bar
            // screen-level padding: 12 top / 18 sides / bottom clears floating nav (see BottomNavContentPadding)
            .padding(horizontal = 18.dp)
            .padding(top = 12.dp, bottom = BottomNavContentPadding),
    ) {
        // ── Greeting ──────────────────────────────────────────────────────
        GreetingSection(
            firstName = data.user.firstName,
            onAdd = onAdd,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        )

        // ── Today's reflection ────────────────────────────────────────────
        IntentionCard(
            label = "Today\u2019s reflection",
            body = reflection,
        )

        Spacer(Modifier.height(14.dp))

        if (data.accounts.isNotEmpty() && data.transactions.isEmpty()) {
            DayOneAcknowledgment(data = data)
            Spacer(Modifier.height(14.dp))
        }

        // ── Net worth feature card ─────────────────────────────────────────
        NetWorthCard(data = data)

        Spacer(Modifier.height(14.dp))

        // ── This month, so far ────────────────────────────────────────────
        ThisMonthSection(
            data = data,
            modifier = Modifier.padding(horizontal = 4.dp),
        )

        Spacer(Modifier.height(14.dp))

        if (data.transactions.isNotEmpty() && data.budgets.isNotEmpty()) {
            SectionHeader(
                title    = "Budgets",
                modifier = Modifier.padding(bottom = 8.dp),
            )
            ListCard(padding = 6) {
                data.budgets.forEachIndexed { i, budget ->
                    BudgetTodayRow(
                        budget          = budget,
                        isLast          = i == data.budgets.lastIndex,
                        displayCurrency = data.primaryAmountCurrency(),
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        // ── Recent transactions ───────────────────────────────────────────
        SectionHeader(
            title = "Recent",
            onMore = { onNav(NavDestination.Flow) },
            modifier = Modifier.padding(bottom = 8.dp),
        )
        ListCard(padding = 6) {
            if (topTx.isEmpty()) {
                EmptyListHint(text = "Nothing recent yet. Add a transaction when you are ready.")
            } else {
                topTx.forEachIndexed { i, tx ->
                    TxRow(
                        tx            = tx,
                        currencyCode  = data.currencyForAccountName(tx.account),
                        isLast        = i == topTx.lastIndex,
                        onClick       = { onTx(tx) },
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Coming up (bills) ─────────────────────────────────────────────
        SectionHeader(
            title = "Coming up",
            modifier = Modifier.padding(bottom = 8.dp),
        )
        ListCard(padding = 4) {
            if (upcoming.isEmpty()) {
                EmptyListHint(text = "No bills coming up. They will land here when you add them.")
            } else {
                upcoming.forEachIndexed { i, bill ->
                    BillRow(
                        bill          = bill,
                        currencyCode  = data.currencyForAccountName(bill.account),
                        isLast        = i == upcoming.lastIndex,
                        onClick       = { onBill(bill) },
                    )
                }
            }
        }
    }
}

// ── Greeting ──────────────────────────────────────────────────────────────
// "Good morning, Sasha" — one 30sp serif line (same size as former name-only line) · date below
@Composable
private fun GreetingSection(
    firstName: String,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val greeting = remember {
        when (LocalTime.now().hour) {
            in 0..11  -> "Good morning"
            in 12..16 -> "Good afternoon"
            else      -> "Good evening"
        }
    }
    val date = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
    }
    val name = firstName.trim().ifEmpty { "there" }

    Row(
        modifier = modifier.padding(bottom = 14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$greeting, $name",
                style = TextStyle(
                    fontFamily = SerifFamily,
                    fontSize = 30.sp,
                    color = ColorInk,
                    lineHeight = (30 * 1.2).sp,
                    letterSpacing = (-0.01).em,
                ),
            )

            Text(
                text = date,
                style = TextStyle(
                    fontFamily = SansFamily,
                    fontSize = 12.sp,
                    color = ColorTextTertiary,
                    letterSpacing = 0.02.em,
                ),
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        // + button
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(ColorSurface)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onAdd,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Add",
                modifier = Modifier.size(16.dp),
                tint = ColorInk,
            )
        }
    }
}

// ── Net worth card ─────────────────────────────────────────────────────────
// Feature card: "What you hold" · big number · delta line · optional sparkline
@Composable
private fun NetWorthCard(data: LedgerData) {
    val showTrend = data.weeklyFlow.size >= 2
    val bottomPad = if (showTrend) 22.dp else 16.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(ColorFeature)
            .padding(horizontal = 22.dp)
            .padding(top = 20.dp, bottom = bottomPad),
    ) {
        Caps(
            text = "What you hold",
            color = ColorTextSecondary,
            modifier = Modifier.padding(bottom = 14.dp),
        )

        MoneyText(amount = data.netWorth, size = 40.sp, currencyCode = data.primaryAmountCurrency())

        val deltaLine = remember(data.netWorth, data.netWorthLastMonth, data.netWorthDelta) {
            when {
                nearlyZero(data.netWorth) &&
                    nearlyZero(data.netWorthLastMonth) &&
                    nearlyZero(data.netWorthDelta) ->
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontFamily = SerifFamily, fontStyle = FontStyle.Italic)) {
                            append("You\u2019re starting here. This number will grow as your balances do.")
                        }
                    }
                nearlyZero(data.netWorthDelta) ->
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontFamily = SerifFamily, fontStyle = FontStyle.Italic)) {
                            append("Unchanged from last month.")
                        }
                    }
                else -> {
                    val moreOrLess = if (data.netWorthDelta >= 0) "a little more" else "a little less"
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontFamily = SerifFamily, fontStyle = FontStyle.Italic)) {
                            append(moreOrLess)
                        }
                        withStyle(SpanStyle(fontFamily = SansFamily)) {
                            append(" than last month · ")
                        }
                        withStyle(
                            SpanStyle(
                                fontFamily = SerifFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontFeatureSettings = "\"tnum\" on, \"lnum\" on",
                            )
                        ) {
                            append(fmt(data.netWorthDelta, currencyCode = data.primaryAmountCurrency(), sign = true))
                        }
                    }
                }
            }
        }

        Text(
            text = deltaLine,
            style = TextStyle(
                fontSize = 12.sp,
                color = ColorTextSerifBody,
            ),
            modifier = Modifier.padding(top = 10.dp),
        )

        if (showTrend) {
            MiniTrend(
                weeklyFlow = data.weeklyFlow,
                modifier = Modifier.padding(top = 18.dp),
            )
        }
    }
}

// ── MiniTrend sparkline ────────────────────────────────────────────────────
// No axes, no labels — just a thin ink line + terminal dot.
// Matches reference MiniTrend: W=full, H=40, 6dp top/bottom pad, 55% opacity.
@Composable
private fun MiniTrend(weeklyFlow: List<WeeklyFlow>, modifier: Modifier = Modifier) {
    val nets = remember(weeklyFlow) { weeklyFlow.map { it.net.toFloat() } }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
    ) {
        if (nets.size < 2) return@Canvas

        val min   = nets.min()
        val max   = nets.max()
        val range = (max - min).coerceAtLeast(1f)
        val n     = nets.size
        val vPad  = 6.dp.toPx()
        val W     = size.width
        val H     = size.height - vPad * 2

        val points = nets.mapIndexed { i, v ->
            val x = (i.toFloat() / (n - 1)) * W
            val y = vPad + H - ((v - min) / range) * H
            androidx.compose.ui.geometry.Offset(x, y)
        }

        // line
        val path = Path()
        points.forEachIndexed { i, pt ->
            if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
        }
        drawPath(
            path  = path,
            color = ColorInk.copy(alpha = 0.55f),
            style = Stroke(
                width = 1.dp.toPx(),
                cap   = StrokeCap.Round,
                join  = StrokeJoin.Round,
            ),
        )

        // terminal dot on the last point
        points.lastOrNull()?.let { last ->
            drawCircle(color = ColorInk, radius = 2.5.dp.toPx(), center = last)
        }
    }
}

// ── This month section ─────────────────────────────────────────────────────
// Full sentence in Cormorant semibold italic (amounts, symbol/code from fmt, and prose).
@Composable
private fun ThisMonthSection(data: LedgerData, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(bottom = 12.dp)) {
        Caps(text = "This month, so far", modifier = Modifier.padding(bottom = 10.dp))

        if (nearlyZero(data.inflow) && nearlyZero(data.outflow)) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontFamily = SansFamily)) {
                        append("No income or spending logged this month yet. ")
                    }
                    withStyle(SpanStyle(fontFamily = SerifFamily, fontStyle = FontStyle.Italic)) {
                        append("When you add transactions, this will fill in.")
                    }
                },
                style = TextStyle(
                    fontSize   = 17.sp,
                    color      = ColorTextPrimary,
                    lineHeight = (17 * 1.55).sp,
                ),
            )
        } else {
            Text(
                text = buildAnnotatedString {
                    val line = SpanStyle(
                        fontFamily = SerifFamily,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.SemiBold,
                        fontFeatureSettings = "\"tnum\" on, \"lnum\" on",
                    )
                    withStyle(line) { append("You received ") }
                    withStyle(line) {
                        append(fmt(data.inflow, currencyCode = data.primaryAmountCurrency()))
                    }
                    withStyle(line) { append(" and let ") }
                    withStyle(line) {
                        append(fmt(data.outflow, currencyCode = data.primaryAmountCurrency()))
                    }
                    withStyle(line) { append(" go. A pace that feels right.") }
                },
                style = TextStyle(
                    fontSize   = 17.sp,
                    color      = ColorTextPrimary,
                    lineHeight = (17 * 1.55).sp,
                ),
            )
        }
    }
}

@Composable
private fun DayOneAcknowledgment(data: LedgerData) {
    val line = remember(data.accounts) {
        when (data.accounts.size) {
            1 -> "For now, your money lives in ${data.accounts[0].name}."
            else ->
                "Your money is spread across ${data.accounts.size} accounts — " +
                    "activity will gather here over time."
        }
    }
    Text(
        text = line,
        style = TextStyle(
            fontFamily    = SerifFamily,
            fontSize      = 15.sp,
            fontStyle     = FontStyle.Italic,
            color           = ColorTextSerifBody,
            lineHeight     = (15 * 1.45).sp,
        ),
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun ColumnScope.EmptyListHint(text: String) {
    Text(
        text = text,
        style = TextStyle(
            fontFamily = SansFamily,
            fontSize   = 13.sp,
            color      = ColorTextSecondary,
            lineHeight = (13 * 1.55).sp,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 18.dp),
    )
}

@Composable
private fun BudgetTodayRow(budget: Budget, isLast: Boolean, displayCurrency: String) {
    val ratio = if (budget.limit > 0.0) (budget.spent / budget.limit).toFloat() else 0f
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Bottom,
        ) {
            Text(
                text     = budget.label,
                style    = TextStyle(
                    fontFamily = SansFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize   = 13.sp,
                    color      = ColorInk,
                ),
                modifier = Modifier.weight(1f).padding(end = 8.dp),
            )
            Text(
                text  = "${fmt(budget.spent, currencyCode = displayCurrency)} / ${fmt(budget.limit, currencyCode = displayCurrency)}",
                style = TextStyle(
                    fontFamily = SerifFamily,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 12.sp,
                    color      = ColorTextSerifMuted,
                    fontFeatureSettings = "\"tnum\" on, \"lnum\" on",
                ),
            )
        }
        Spacer(Modifier.height(8.dp))
        ProgressBar(
            value      = ratio.coerceIn(0f, 1f),
            trackColor = ColorBorderPrimary,
            fillColor  = if (budget.isOver) ColorInk.copy(alpha = 0.55f) else ColorMuted,
        )
    }
    if (!isLast) {
        Hairline(modifier = Modifier.padding(horizontal = 14.dp))
    }
}

// ── ListCard ───────────────────────────────────────────────────────────────
// Surface card that wraps row lists (transactions, bills).
// `padding` = vertical padding in dp (reference uses 6 for tx, 4 for bills).
@Composable
private fun ListCard(
    padding: Int = 6,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(ColorSurface)
            .padding(horizontal = 4.dp, vertical = padding.dp),
        content = content,
    )
}

// ── Preview ───────────────────────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFFF5F1EB, showSystemUi = true)
@Composable
private fun TodayScreenPreview() {
    StillwaterTheme {
        TodayScreen(data = ledgerWithDerivedBudgetsAndWeekly(SampleData))
    }
}
