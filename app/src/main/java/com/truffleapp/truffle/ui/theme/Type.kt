package com.truffleapp.truffle.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.truffleapp.truffle.R

// ── Font families ─────────────────────────────────────────────────────────

val InterFamily: FontFamily = FontFamily(
    Font(R.font.inter_regular,  weight = FontWeight.Normal),
    Font(R.font.inter_medium,   weight = FontWeight.Medium),
    Font(R.font.inter_semibold, weight = FontWeight.SemiBold),
)

val CormorantFamily: FontFamily = FontFamily(
    Font(R.font.cormorant_garamond_regular, weight = FontWeight.Normal, style = FontStyle.Normal),
    // Upright Medium/SemiBold — matches the visual weight of semibold italic used for reflections.
    Font(R.font.cormorant_garamond_semibold, weight = FontWeight.Medium, style = FontStyle.Normal),
    Font(R.font.cormorant_garamond_semibold, weight = FontWeight.SemiBold, style = FontStyle.Normal),
    // All italic uses the semibold cut — the regular italic is too light on warm backgrounds.
    Font(R.font.cormorant_garamond_semibold_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(R.font.cormorant_garamond_semibold_italic, weight = FontWeight.Medium, style = FontStyle.Italic),
    Font(R.font.cormorant_garamond_semibold_italic, weight = FontWeight.SemiBold, style = FontStyle.Italic),
    Font(R.font.cormorant_garamond_semibold_italic, weight = FontWeight.Bold, style = FontStyle.Italic),
)

// Convenience aliases used across the UI layer
val SansFamily:  FontFamily get() = InterFamily
val SerifFamily: FontFamily get() = CormorantFamily

// ── Type scale ─────────────────────────────────────────────────────────────
object StillwaterType {
    // Bottom nav labels — Inter, 9sp, line-height 1.4, 0.12em tracking
    val navLabel = TextStyle(
        fontFamily = SansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 9.sp,
        lineHeight = (9 * 1.4).sp,
        letterSpacing = 0.12.em,
    )
}
