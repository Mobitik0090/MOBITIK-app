package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MobitikOrange,
    onPrimary = MobitikWhite,
    primaryContainer = MobitikOrangeDark,
    onPrimaryContainer = MobitikWhite,
    secondary = MobitikOrangeLight,
    onSecondary = MobitikBlack,
    secondaryContainer = MobitikDarkCharcoal,
    onSecondaryContainer = MobitikWhite,
    tertiary = MobitikInfo,
    onTertiary = MobitikWhite,
    background = MobitikBlack,
    onBackground = MobitikWhite,
    surface = MobitikDarkCharcoal,
    onSurface = MobitikWhite,
    surfaceVariant = MobitikCardElevated,
    onSurfaceVariant = MobitikGrayLight,
    outline = MobitikBorder,
    outlineVariant = MobitikGrayDark,
    error = MobitikError,
    onError = MobitikWhite
)

private val LightColorScheme = lightColorScheme(
    primary = MobitikOrange,
    onPrimary = MobitikWhite,
    primaryContainer = MobitikOrangeLight,
    onPrimaryContainer = MobitikBlack,
    secondary = MobitikOrangeDark,
    onSecondary = MobitikWhite,
    secondaryContainer = Color(0xFFFFF0E0),
    onSecondaryContainer = MobitikBlack,
    tertiary = MobitikInfo,
    onTertiary = MobitikWhite,
    background = Color(0xFFF7F7F9),
    onBackground = Color(0xFF1A1A1A),
    surface = MobitikWhite,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF444444),
    outline = Color(0xFFCCCCCC),
    outlineVariant = Color(0xFFE0E0E0),
    error = MobitikError,
    onError = MobitikWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to cryptocurrency dark theme
    dynamicColor: Boolean = false, // Keep branded orange/black palette
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
