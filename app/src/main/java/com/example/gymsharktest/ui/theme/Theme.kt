package com.example.gymsharktest.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Ink,
    onPrimary = Paper,
    secondary = Ink,
    onSecondary = Paper,
    background = Cream,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = ImageWell,
    onSurfaceVariant = InkMuted,
    outline = Outline,
    error = Sale,
    onError = OnSale,
)

private val DarkColors = darkColorScheme(
    primary = PaperDark,
    onPrimary = InkDark,
    secondary = PaperDark,
    onSecondary = InkDark,
    background = InkDark,
    onBackground = PaperDark,
    surface = InkDarkElevated,
    onSurface = PaperDark,
    surfaceVariant = ImageWellDark,
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
