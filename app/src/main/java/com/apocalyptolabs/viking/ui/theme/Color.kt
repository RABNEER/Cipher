package com.apocalyptolabs.viking.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * VIKING monochrome design language.
 * Pure black & white — hierarchy is expressed through brightness,
 * weight and whitespace instead of hue.
 *
 * Severity maps to luminance: the louder the threat, the brighter the white.
 */

// Core surfaces (Obsidian Cipher stepped elevation)
val VikingPitchBlack = Color(0xFF000000) // Deep OLED pitch black
val VikingBlack = Color(0xFF09090B)      // Canvas base
val VikingNavy = Color(0xFF0E0E10)       // Cavity recess
val VikingSurface = Color(0xFF131315)    // Card panels
val VikingSurfaceLow = Color(0xFF18181B) // Recessed container
val VikingSurfaceHigh = Color(0xFF202022)// Elevated surface
val VikingSurfaceHighest = Color(0xFF27272A) // High popover

// Hairlines & Borders
val VikingBorder = Color(0xFF27272A)     // 1px hairline wireframe
val VikingBorderInteractive = Color(0xFF3F3F46) // Focus / Hover border
val VikingDarkGray = Color(0xFF27272A)   // Legacy compatibility

// Primary signal & typography
val VikingWhite = Color(0xFFF4F4F5)      // Signal white text
val VikingGray = Color(0xFFA1A1AA)       // Secondary signal
val VikingMuted = Color(0xFF71717A)      // Muted telemetry
val VikingTeal = Color(0xFFFFFFFF)       // Primary action white
val VikingTealDeep = Color(0xFF27272A)   // Inset button surface

// Tactical status & severity ladder
val VikingCritical = Color(0xFFEF4444)   // Critical threat crimson
val VikingHigh = Color(0xFFF97316)       // High threat orange
val VikingMedium = Color(0xFFFACC15)     // Medium caution yellow
val VikingSafe = Color(0xFF22C55E)       // Verified safe green

// Tactical Shield identity tones
val VikingApk = Color(0xFFE2E8F0)        // APK Binary Scanner
val VikingBlue = Color(0xFF38BDF8)       // UPI Link Guard (Cyber Financial)
val VikingGold = Color(0xFFFACC15)       // SMS Shield (Phishing Alert)
val VikingRose = Color(0xFFFF6B6B)       // Call / Digital Arrest Guard
val VikingCyan = Color(0xFF22D3EE)       // Network Spectrum
val VikingPurple = Color(0xFFC084FC)     // Privilege / Permission Audit
