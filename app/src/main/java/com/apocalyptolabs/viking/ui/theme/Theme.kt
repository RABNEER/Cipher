package com.apocalyptolabs.viking.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = VikingTeal,
    secondary = VikingNavy,
    tertiary = VikingSafe,
    background = VikingBlack,
    surface = VikingNavy,
    onPrimary = VikingBlack,
    onSecondary = VikingWhite,
    onBackground = VikingWhite,
    onSurface = VikingWhite,
    error = VikingCritical
)

@Composable
fun VikingTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = VikingTypography,
        content = content
    )
}
