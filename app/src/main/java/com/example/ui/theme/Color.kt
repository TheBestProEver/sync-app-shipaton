package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// SYNC Design System Colors (Section 18.3)
val Night = Color(0xFF0B1120)       // App background
val Wall = Color(0xFF141B2D)        // Cards, committed time blocks, busy windows
val WallRaised = Color(0xFF1C2539)  // Sheets, inputs, pressed states
val Mullion = Color(0xFF2A3450)     // Decorative hairlines and dividers only
val Moonlight = Color(0xFFE6EAF2)   // Primary text and the one filled button
val Haze = Color(0xFFA3ACBF)        // Secondary text and outlined button borders
val Dusk = Color(0xFF7D879C)        // Tertiary text and the frames of unlit windows
val Amber = Color(0xFFF0B429)       // Availability only (10.1:1 contrast on Night)
val Blinds = Color(0xFF6C7FA8)      // Stripes for the focused family
val Signal = Color(0xFFFF6B6B)      // Errors and destructive actions only

// Business / Workplace Mode Accent Colors
val WorkplaceNavy = Color(0xFF0C1322)       // Sleek corporate night
val WorkplaceCard = Color(0xFF131E34)       // Workplace card surface
val WorkplaceBorder = Color(0xFF233557)     // Workplace divider/border
val WorkplaceSteel = Color(0xFF60A5FA)      // Workplace primary badge & indicator
val BadgeGold = Color(0xFFFBBF24)           // Verified student badge

// Window Tones (2px sill under each window - Section 18.3)
val ToneSky = Color(0xFF8FB8DE)
val ToneSage = Color(0xFF9ED0B5)
val ToneRose = Color(0xFFD7A1C4)
val ToneLavender = Color(0xFFB7A6E8)
val ToneTeal = Color(0xFF7FC8C8)
val ToneMoss = Color(0xFFA9C77B)
val ToneStone = Color(0xFFC9C3A8)
val ToneCoral = Color(0xFFE8A0A0)

val AllWindowTones = listOf(
    "Sky" to ToneSky,
    "Sage" to ToneSage,
    "Rose" to ToneRose,
    "Lavender" to ToneLavender,
    "Teal" to ToneTeal,
    "Moss" to ToneMoss,
    "Stone" to ToneStone,
    "Coral" to ToneCoral
)

fun getToneColor(name: String): Color {
    return AllWindowTones.firstOrNull { it.first.equals(name, ignoreCase = true) }?.second ?: ToneSky
}
