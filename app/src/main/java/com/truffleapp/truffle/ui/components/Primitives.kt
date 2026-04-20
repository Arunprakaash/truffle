package com.truffleapp.truffle.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.remember
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.VolunteerActivism
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
import com.truffleapp.truffle.ui.theme.ColorPage
import com.truffleapp.truffle.ui.theme.ColorSurface
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

// ── SurfaceCircleIconButton ────────────────────────────────────────────────
// Circular surface icon tap target — back, overflow (⋯), Today +, etc. No ripple.
@Composable
fun SurfaceCircleIconButton(
    imageVector: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    iconSize: Dp = 16.dp,
    containerColor: Color = ColorSurface,
    iconTint: Color = ColorInk,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = if (enabled) iconTint else ColorTextTertiary,
        )
    }
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
    /** Label for the trailing control when [onMore] is set (e.g. "All", "Configure"). */
    moreLabel: String = "All",
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
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onMore,
                    )
                    .padding(horizontal = 2.dp, vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = moreLabel,
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

// ── ScreenTopBar ──────────────────────────────────────────────────────────
// Main-tab screens: circular back to Today + Caps page title (reference headers).
@Composable
fun ScreenTopBar(
    title: String,
    modifier: Modifier = Modifier,
    showBack: Boolean = true,
    onBackToToday: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showBack) {
            SurfaceCircleIconButton(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back to Today",
                onClick = onBackToToday,
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
        if (title.isNotBlank()) {
            Caps(text = title)
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
    "book"            -> Icons.AutoMirrored.Outlined.MenuBook
    "gift"            -> Icons.Outlined.CardGiftcard
    "tree"            -> Icons.Outlined.Spa
    "arrowUp"         -> Icons.Outlined.ArrowUpward
    "briefcase"       -> Icons.Outlined.AccountBalance
    else              -> Icons.Outlined.Circle
}

fun accountIcon(kind: AccountKind): ImageVector = when (kind) {
    AccountKind.Cash   -> Icons.Outlined.AccountBalance
    AccountKind.Invest -> Icons.AutoMirrored.Outlined.TrendingUp
    AccountKind.Credit -> Icons.Outlined.CreditCard
}

private val GoalRingIconPool: List<ImageVector> = listOf(
    Icons.Outlined.TrackChanges,
    Icons.Outlined.Flag,
    Icons.Outlined.StarBorder,
    Icons.Outlined.EmojiEvents,
    Icons.Outlined.Savings,
    Icons.Outlined.Home,
    Icons.Outlined.Flight,
    Icons.Outlined.Spa,
    Icons.Outlined.CardGiftcard,
    Icons.Outlined.LocalCafe,
    Icons.Outlined.DirectionsCar,
    Icons.Outlined.FavoriteBorder,
    Icons.Outlined.Lightbulb,
    Icons.Outlined.Park,
    Icons.Outlined.VolunteerActivism,
    Icons.Outlined.AutoAwesome,
    Icons.Outlined.ShoppingCart,
    Icons.Outlined.AccountBalance,
)

fun goalRingIcon(goalId: String): ImageVector {
    val h = goalId.hashCode()
    val n = GoalRingIconPool.size
    val idx = ((h % n) + n) % n
    return GoalRingIconPool[idx]
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

// ── SheetButton ───────────────────────────────────────────────────────────
// Full-width tappable button for bottom sheets. No ripple, no Material chrome.
// Ghost: surface card bg + ink text (neutral actions).
// Destructive: transparent bg + tertiary text (remove / delete actions).
// Primary: ink bg + page text (submit / confirm actions).
enum class SheetButtonVariant { Primary, Ghost, Destructive }

@Composable
fun SheetButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SheetButtonVariant = SheetButtonVariant.Ghost,
    icon: ImageVector? = null,
) {
    val bg = when (variant) {
        SheetButtonVariant.Primary     -> ColorInk
        SheetButtonVariant.Ghost       -> ColorSurface
        SheetButtonVariant.Destructive -> ColorFeature2
    }
    val fg = when (variant) {
        SheetButtonVariant.Primary     -> ColorPage
        SheetButtonVariant.Ghost       -> ColorInk
        SheetButtonVariant.Destructive -> ColorTextSecondary
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .size(14.dp),
                    tint = fg,
                )
            }
            Text(
                text = text,
                style = TextStyle(
                    fontFamily = SansFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = fg,
                ),
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
