package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Navy900,
    primaryContainer = Navy600,
    onPrimaryContainer = CyanBright,
    secondary = CyanMuted,
    onSecondary = Navy900,
    secondaryContainer = Navy700,
    onSecondaryContainer = CyanGlow,
    tertiary = WarningAmber,
    onTertiary = Navy900,
    background = Navy900,
    onBackground = SlateTextPrimary,
    surface = Navy800,
    onSurface = SlateTextPrimary,
    surfaceVariant = Navy700,
    onSurfaceVariant = SlateTextSecondary,
    outline = Navy500,
    outlineVariant = Navy600,
    error = DangerRed,
    onError = Color.White
)

private val LightColorScheme = darkColorScheme(
    // We intentionally enforce dark navy/cyan professional look even in light mode
    primary = CyanAccent,
    onPrimary = Navy900,
    primaryContainer = Navy600,
    onPrimaryContainer = CyanBright,
    secondary = CyanMuted,
    onSecondary = Navy900,
    secondaryContainer = Navy700,
    onSecondaryContainer = CyanGlow,
    tertiary = WarningAmber,
    onTertiary = Navy900,
    background = Navy900,
    onBackground = SlateTextPrimary,
    surface = Navy800,
    onSurface = SlateTextPrimary,
    surfaceVariant = Navy700,
    onSurfaceVariant = SlateTextSecondary,
    outline = Navy500,
    outlineVariant = Navy600,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun TimestampPhotoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
