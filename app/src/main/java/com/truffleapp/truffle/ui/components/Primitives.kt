package com.truffleapp.truffle.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.truffleapp.truffle.data.AccountKind
import com.truffleapp.truffle.ui.theme.ColorBorderPrimary
import com.truffleapp.truffle.ui.theme.ColorBorderSecondary
import com.truffleapp.truffle.ui.theme.ColorBorderTertiary
import com.truffleapp.truffle.ui.theme.ColorFeature2
import com.truffleapp.truffle.ui.theme.ColorInk
import com.truffleapp.truffle.ui.theme.ColorMuted
import com.truffleapp.truffle.ui.theme.ColorTextSecondary
import com.truffleapp.truffle.ui.theme.ColorTextTertiary
import com.truffleapp.truffle.ui.theme.SansFamily
import com.truffleapp.truffle.ui.theme.StillwaterType

// ── Hairline ──────────────────────────────────────────────────────────────
// 0.5dp divider. Used between list rows.
@Composable
fun Hairline(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(ColorBorderTertiary),
    )
}

// ── Caps ──────────────────────────────────────────────────────────────────
// Uppercase spaced label — the repeating UI label language of Stillwater.
@Composable
fun Caps(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 10.sp,
    color: Color = ColorTextTertiary,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = TextStyle(
            fontFamily = SansFamily,
            fontWeight = FontWeight.Medium,
            fontSize = size,
            // tighter tracking at larger sizes, wider at 10sp
            letterSpacing = if (size <= 10.sp) 0.14.em else 0.12.em,
            color = color,
        ),
    )
}

// ── IconCircle ────────────────────────────────────────────────────────────
// Circular container used in transaction / account / bill rows.
// Default matches reference: 34dp circle, 14dp icon, feature-2 bg.
@Composable
fun IconCircle(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    iconSize: Dp = 14.dp,
    background: Color = ColorFeature2,
    tint: Color = ColorTextSecondary,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = tint,
        )
    }
}

// ── SectionHeader ─────────────────────────────────────────────────────────
// "Recent  All ›" row above list cards.
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    onMore: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Caps(text = title)
        Spacer(modifier = Modifier.weight(1f))
        if (onMore != null) {
            androidx.compose.material3.TextButton(
                onClick = onMore,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            ) {
                Text(
                    text = "All",
                    style = StillwaterType.navLabel,
                    color = ColorTextTertiary,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .size(10.dp),
                    tint = ColorTextTertiary,
                )
            }
        }
    }
}

// ── Icon mapping helpers ───────────────────────────────────────────────────
// Maps the icon key strings from data models to Material icons.

fun categoryIcon(key: String): ImageVector = when (key) {
    "coffee"          -> Icons.Outlined.LocalCafe
    "cart"            -> Icons.Outlined.ShoppingCart
    "home", "home2"   -> Icons.Outlined.Home
    "car"             -> Icons.Outlined.DirectionsCar
    "book"            -> Icons.Outlined.MenuBook
    "gift"            -> Icons.Outlined.CardGiftcard
    "tree"            -> Icons.Outlined.Spa
    "arrowUp"         -> Icons.Outlined.ArrowUpward
    "briefcase"       -> Icons.Outlined.AccountBalance
    else              -> Icons.Outlined.Circle
}

fun accountIcon(kind: AccountKind): ImageVector = when (kind) {
    AccountKind.Cash   -> Icons.Outlined.AccountBalance
    AccountKind.Invest -> Icons.Outlined.TrendingUp
    AccountKind.Credit -> Icons.Outlined.CreditCard
}

// ── RingProgress ──────────────────────────────────────────────────────────
// Segmented circular progress ring — pure greige, no color.
// Matches reference: track = border-secondary, fill = ink, round caps.
// Animated with ease-breath (800ms) on value changes.
@Composable
fun RingProgress(
    value: Float,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    strokeWidth: Dp = 2.dp,
) {
    val animated by animateFloatAsState(
        targetValue   = value.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label         = "ringProgress",
    )

    Canvas(modifier = modifier.size(size)) {
        val stroke   = strokeWidth.toPx()
        val inset    = stroke / 2f
        val arcSize  = androidx.compose.ui.geometry.Size(
            this.size.width - stroke,
            this.size.height - stroke,
        )
        val topLeft  = androidx.compose.ui.geometry.Offset(inset, inset)

        // track — full circle, border-secondary
        drawArc(
            color      = ColorBorderSecondary,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter  = false,
            topLeft    = topLeft,
            size       = arcSize,
            style      = Stroke(width = stroke, cap = StrokeCap.Round),
        )

        // progress arc — ink, starts from top (−90°)
        if (animated > 0f) {
            drawArc(
                color      = ColorInk,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
    }
}

// ── ProgressBar ───────────────────────────────────────────────────────────
// Thin horizontal bar — 2dp default height, no fills, soft colors.
// Width animates over 2400ms (matches reference `transition: width 2400ms`).
@Composable
fun ProgressBar(
    value: Float,
    modifier: Modifier = Modifier,
    height: Dp = 2.dp,
    trackColor: Color = ColorBorderPrimary,
    fillColor: Color = ColorMuted,
) {
    val animated by animateFloatAsState(
        targetValue   = value.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 2400),
        label         = "progressBar",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height))
            .background(trackColor),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .background(fillColor),
        )
    }
}
