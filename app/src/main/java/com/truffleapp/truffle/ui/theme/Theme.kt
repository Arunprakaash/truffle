package com.truffleapp.truffle.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Stillwater is a single warm-light palette — no dark mode by design.
private val StillwaterColorScheme = lightColorScheme(
    background        = ColorPage,
    surface           = ColorSurface,
    onBackground      = ColorTextPrimary,
    onSurface         = ColorTextPrimary,
    outline           = ColorBorderPrimary,
    outlineVariant    = ColorBorderTertiary,
    // keep other slots neutral so Material components don't clash
    primary           = ColorInk,
    onPrimary         = ColorPage,
    secondary         = ColorMuted,
    onSecondary       = ColorPage,
    tertiary          = ColorFeature,
    onTertiary        = ColorTextSecondary,
)

@Composable
fun StillwaterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StillwaterColorScheme,
        content = content,
    )
}
