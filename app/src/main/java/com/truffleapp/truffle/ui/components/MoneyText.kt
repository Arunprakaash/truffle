package com.truffleapp.truffle.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.truffleapp.truffle.data.DEFAULT_LEDGER_CURRENCY
import com.truffleapp.truffle.data.formatLedgerMoney
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorTextSerifMuted
import com.truffleapp.truffle.ui.theme.SerifFamily

// ── Formatting ────────────────────────────────────────────────────────────
// Device locale separators; ISO 4217 [currencyCode] via [formatLedgerMoney].

fun fmt(
    amount: Double,
    currencyCode: String = DEFAULT_LEDGER_CURRENCY,
    cents: Boolean = false,
    sign: Boolean = false,
): String = formatLedgerMoney(amount, currencyCode, cents, sign)

// ── MoneyText composable ──────────────────────────────────────────────────
// Serif, tabular numerals, ink color by default.
// `dimmed` → tertiary color (used for secondary amounts, hints).

@Composable
fun MoneyText(
    amount: Double,
    modifier: Modifier = Modifier,
    currencyCode: String = DEFAULT_LEDGER_CURRENCY,
    size: TextUnit = 28.sp,
    color: Color? = null,           // null = auto (ink or tertiary if dimmed)
    dimmed: Boolean = false,
    cents: Boolean = false,
    sign: Boolean = false,
    weight: FontWeight = FontWeight.SemiBold,
    textAlign: TextAlign = TextAlign.Unspecified,
) {
    val resolvedColor = color ?: if (dimmed) ColorTextSerifMuted else ColorInk
    Text(
        text = fmt(amount, currencyCode = currencyCode, cents = cents, sign = sign),
        modifier = modifier,
        textAlign = textAlign,
        style = TextStyle(
            fontFamily = SerifFamily,
            fontWeight = weight,
            fontSize = size,
            color = resolvedColor,
            // tabular-nums so digits align in lists
            fontFeatureSettings = "\"tnum\" on, \"lnum\" on",
            letterSpacing = (-0.01).sp,  // tight, like the reference
        ),
    )
}
