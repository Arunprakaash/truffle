package com.truffleapp.truffle.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.truffleapp.truffle.navigation.NavDestination
import com.truffleapp.truffle.ui.theme.ColorBorderPrimary
import com.truffleapp.truffle.ui.theme.ColorFeature2
import com.truffleapp.truffle.ui.theme.ColorPage
import com.truffleapp.truffle.ui.theme.ColorTextPrimary
import com.truffleapp.truffle.ui.theme.ColorTextTertiary
import com.truffleapp.truffle.ui.theme.StillwaterTheme
import com.truffleapp.truffle.ui.theme.StillwaterType

/** Bottom padding for scrollable main tabs so the last rows clear the floating pill + gesture inset. */
val BottomNavContentPadding: Dp = 132.dp

private const val NAV_ANIM_MS = 320

private val PillShape = RoundedCornerShape(999.dp)

private data class NavItem(
    val destination: NavDestination,
    val icon: ImageVector,
)

private val navItems = listOf(
    NavItem(NavDestination.Today,    Icons.Outlined.Home),
    NavItem(NavDestination.Accounts, Icons.Outlined.Layers),
    NavItem(NavDestination.Flow,     Icons.AutoMirrored.Outlined.ShowChart),
    NavItem(NavDestination.Goals,    Icons.Outlined.TrackChanges),
)

@Composable
fun BottomNavBar(
    current: NavDestination,
    onNav: (NavDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = navItems.indexOfFirst { it.destination == current }
    val haptics = rememberHaptics()

    // Animates as a float so the pill slides continuously between positions
    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = tween(durationMillis = NAV_ANIM_MS, easing = FastOutSlowInEasing),
        label = "navPillSlide",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 18.dp)
            .shadow(
                elevation = 2.dp,
                shape = PillShape,
                ambientColor = ColorTextPrimary.copy(alpha = 0.04f),
                spotColor = ColorTextPrimary.copy(alpha = 0.04f),
            )
            .clip(PillShape)
            .background(ColorPage)
            .border(width = 0.5.dp, color = ColorBorderPrimary, shape = PillShape)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            // Single pill drawn behind all items — slides on every frame during animation
            .drawBehind {
                val itemWidth = size.width / navItems.size
                val pillLeft  = itemWidth * animatedIndex
                val radius    = size.height / 2f
                drawRoundRect(
                    color        = ColorFeature2,
                    topLeft      = Offset(pillLeft, 0f),
                    size         = Size(itemWidth, size.height),
                    cornerRadius = CornerRadius(radius, radius),
                )
            },
        // No SpaceAround — weight(1f) already gives each item equal width
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        navItems.forEach { item ->
            NavBarItem(
                item     = item,
                isActive = current == item.destination,
                onClick  = {
                    if (current != item.destination) {
                        haptics.tick()
                        onNav(item.destination)
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun NavBarItem(
    item: NavItem,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color by animateColorAsState(
        targetValue   = if (isActive) ColorTextPrimary else ColorTextTertiary,
        animationSpec = tween(durationMillis = NAV_ANIM_MS),
        label         = "navColor_${item.destination.name}",
    )

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            )
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            imageVector        = item.icon,
            contentDescription = item.destination.label,
            modifier           = Modifier.size(20.dp),
            tint               = color,
        )
        Text(
            text      = item.destination.label.uppercase(),
            style     = StillwaterType.navLabel,
            color     = color,
            textAlign = TextAlign.Center,
            maxLines  = 1,
            softWrap  = false,
            overflow  = TextOverflow.Visible,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1EB)
@Composable
private fun BottomNavBarPreview() {
    StillwaterTheme {
        BottomNavBar(
            current = NavDestination.Today,
            onNav   = {},
        )
    }
}
