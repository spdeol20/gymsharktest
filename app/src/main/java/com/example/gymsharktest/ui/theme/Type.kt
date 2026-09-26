package com.example.gymsharktest.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val defaults = Typography()

internal val GymsharkTypography = defaults.copy(
    headlineMedium = defaults.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.6).sp,
    ),
    titleLarge = defaults.titleLarge.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.4).sp,
    ),
    titleMedium = defaults.titleMedium.copy(
        fontWeight = FontWeight.SemiBold,
    ),
    labelMedium = defaults.labelMedium.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.6.sp,
    ),
)
