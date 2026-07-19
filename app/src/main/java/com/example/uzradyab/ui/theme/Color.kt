package com.example.uzradyab.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalIsDarkTheme = staticCompositionLocalOf { false }

@Composable
fun themedColor(light: Color, dark: Color = light): Color {
    return if (LocalIsDarkTheme.current) dark else light
}


// Original Light Colors
val AppBlue = Color(0xFF2F80ED)
val AppBlueDark = Color(0xFF256FE0)
val AppPurple = Color(0xFF7E45B6)

val AppTextPrimary = Color(0xFF384C5C)
val AppTextBody = Color(0xFF202832)
val AppTextMuted = Color(0xFFBEC6CE)
val AppInputBorder = Color(0xFFC9CED3)
val AppSurface = Color(0xFFFFFFFF)
val AppBackground = Color(0xFFF7FAFD)

// Modern Dark Mode Colors
val AppBlueDarkTheme = Color(0xFF307EF3) // Primary/100
val AppBlueDarkerTheme = Color(0xFF3B7CE0)
val AppPurpleDarkTheme = Color(0xFF9B63D8)

val AppTextPrimaryDark = Color.White
val AppTextBodyDark = Color(0xFF97ADBF) // Secondary/40
val AppTextMutedDark = Color(0xFF6A8BA5) // Secondary/60
val AppInputBorderDark = Color(0xFF384C5C) // Secondary/100
val AppSurfaceDark = Color(0xFF27343F) // Secondary/120
val AppBackgroundDark = Color(0xFF1C262E) // Secondary/140

@Immutable
data class UzradyabColors(
    val primary: Color,
    val primaryDark: Color,
    val purple: Color,
    val textPrimary: Color,
    val textBody: Color,
    val textMuted: Color,
    val inputBorder: Color,
    val surface: Color,
    val background: Color
)

val lightUzradyabColors = UzradyabColors(
    primary = AppBlue,
    primaryDark = AppBlueDark,
    purple = AppPurple,
    textPrimary = AppTextPrimary,
    textBody = AppTextBody,
    textMuted = AppTextMuted,
    inputBorder = AppInputBorder,
    surface = AppSurface,
    background = AppBackground
)

val darkUzradyabColors = UzradyabColors(
    primary = AppBlueDarkTheme,
    primaryDark = AppBlueDarkerTheme,
    purple = AppPurpleDarkTheme,
    textPrimary = AppTextPrimaryDark,
    textBody = AppTextBodyDark,
    textMuted = AppTextMutedDark,
    inputBorder = AppInputBorderDark,
    surface = AppSurfaceDark,
    background = AppBackgroundDark
)

val LocalUzradyabColors = staticCompositionLocalOf { lightUzradyabColors }
