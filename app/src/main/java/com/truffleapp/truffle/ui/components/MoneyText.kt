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
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorTextTertiary
import com.truffleapp.truffle.ui.theme.SerifFamily
import java.text.NumberFormat
import java.util.Locale

// ── Formatting ────────────────────────────────────────────────────────────
// Mirrors the reference `fmt()` function exactly:
//   − No color for money (no green/red)
//   − Uses '− ' (proper minus, U+2212) for outflows
//   − Uses '+ ' prefix only when sign=true (detail views)
//   − Thousands separator, optional cents

fun fmt(
    amount: Double,
    cents: Boolean = false,
    sign: Boolean = false,
): String {
    val abs = Math.abs(amount)
    val nf = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = if (cents) 2 else 0
        maximumFractionDigits = if (cents) 2 else 0
    }
    val s = nf.format(abs)
    return when {
        sign   -> "${if (amount >= 0) "+ " else "\u2212 "}$$s"
        amount < 0 -> "\u2212 $$s"
        else   -> "$$s"
    }
}

// ── MoneyText composable ──────────────────────────────────────────────────
// Serif, tabular numerals, ink color by default.
// `dimmed` → tertiary color (used for secondary amounts, hints).

@Composable
fun MoneyText(
    amount: Double,
    modifier: Modifier = Modifier,
    size: TextUnit = 28.sp,
    color: Color? = null,           // null = auto (ink or tertiary if dimmed)
    dimmed: Boolean = false,
    cents: Boolean = false,
    sign: Boolean = false,
    weight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign = TextAlign.Unspecified,
) {
    val resolvedColor = color ?: if (dimmed) ColorTextTertiary else ColorInk
    Text(
        text = fmt(amount, cents = cents, sign = sign),
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
