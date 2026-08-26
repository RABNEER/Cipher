package com.apocalyptolabs.viking.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * VIKING monochrome design language.
 * Pure black & white — hierarchy is expressed through brightness,
 * weight and whitespace instead of hue.
 *
 * Severity maps to luminance: the louder the threat, the brighter the white.
 */

// Core surfaces (near-black stepped elevation)
val VikingBlack = Color(0xFF060606)
val VikingNavy = Color(0xFF101010)
val VikingSurface = Color(0xFF161616)
val VikingSurfaceHigh = Color(0xFF202020)

// Primary accent = refined off-white
val VikingTeal = Color(0xFFEDEDED)
val VikingTealDeep = Color(0xFF2E2E2E)

// Severity luminance ladder
val VikingCritical = Color(0xFFFFFFFF) // loudest — pure white
val VikingHigh = Color(0xFFD0D0D0)
val VikingMedium = Color(0xFFA0A0A0)
val VikingSafe = Color(0xFF707070)     // quietest — all clear

// Text
val VikingWhite = Color(0xFFF2F2F2)
val VikingGray = Color(0xFF8F8F8F)
val VikingDarkGray = Color(0xFF262626)

// Per-module identity tones (grayscale steps, subtle differentiation)
val VikingPurple = Color(0xFFEDEDED)
val VikingBlue = Color(0xFFBFBFBF)
val VikingGold = Color(0xFF949494)
val VikingRose = Color(0xFF6E6E6E)
