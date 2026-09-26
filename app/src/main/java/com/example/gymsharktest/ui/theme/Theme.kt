package com.example.gymsharktest.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Ink,
    onPrimary = Paper,
    secondary = Accent,
    onSecondary = Ink,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperMuted,
    onSurfaceVariant = InkMuted,
    outline = Outline,
    error = Sale,
    onError = OnSale,
)

private val DarkColors = darkColorScheme(
    primary = PaperDark,
    onPrimary = InkDark,
    secondary = Accent,
    onSecondary = InkDark,
    background = InkDark,
    onBackground = PaperDark,
    surface = InkDark,
    onSurface = PaperDark,
    surfaceVariant = InkDarkElevated,
    onSurfaceVariant = PaperDarkMuted,
    outline = OutlineDark,
    error = Sale,
    onError = OnSale,
)

/**
 * Dynamic colour is deliberately not used: a retail catalogue should render brand colours
 * identically on every device rather than adopting the user's wallpaper palette.
 */
@Composable
fun GymsharkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = GymsharkTypography,
        content = content,
    )
}
