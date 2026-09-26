package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// SYNC ships dark only for v1 (Section 18.3)
private val SyncDarkColorScheme = darkColorScheme(
    primary = Moonlight,
    onPrimary = Night,
    primaryContainer = WallRaised,
    onPrimaryContainer = Moonlight,
    secondary = Haze,
    onSecondary = Night,
    secondaryContainer = WallRaised,
    onSecondaryContainer = Moonlight,
    tertiary = Dusk,
    onTertiary = Moonlight,
    background = Night,
    onBackground = Moonlight,
    surface = Wall,
    onSurface = Moonlight,
    surfaceVariant = WallRaised,
    onSurfaceVariant = Haze,
    outline = Mullion,
    outlineVariant = Dusk,
    error = Signal,
    onError = Night
)

@Composable
fun SyncTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SyncDarkColorScheme,
        typography = SyncTypography,
        content = content
    )
}
