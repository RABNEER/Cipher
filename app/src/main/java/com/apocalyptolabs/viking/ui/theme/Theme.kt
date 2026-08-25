package com.apocalyptolabs.viking.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MonochromeScheme = darkColorScheme(
    primary = VikingTeal,
    onPrimary = VikingBlack,
    primaryContainer = VikingSurfaceHigh,
    onPrimaryContainer = VikingWhite,
    secondary = VikingWhite,
    onSecondary = VikingBlack,
    secondaryContainer = VikingNavy,
    onSecondaryContainer = VikingWhite,
    tertiary = VikingGray,
    onTertiary = VikingBlack,
    background = VikingBlack,
    onBackground = VikingWhite,
    surface = VikingSurface,
    onSurface = VikingWhite,
    surfaceVariant = VikingSurfaceHigh,
    onSurfaceVariant = VikingGray,
    outline = VikingDarkGray,
    outlineVariant = VikingDarkGray,
    error = VikingCritical,
    onError = VikingBlack,
    scrim = VikingBlack
)

@Composable
fun VikingTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MonochromeScheme,
        typography = VikingTypography,
        content = content
    )
}
