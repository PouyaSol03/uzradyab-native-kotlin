package com.example.uzradyab.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AppBlue,
    onPrimary = Color.White,
    secondary = AppTextPrimary,
    background = AppTextBody,
    surface = AppSurface,
    onSurface = AppTextBody
)

private val LightColorScheme = lightColorScheme(
    primary = AppBlue,
    onPrimary = Color.White,
    secondary = AppTextPrimary,
    onSecondary = Color.White,
    background = AppBackground,
    onBackground = AppTextBody,
    surface = AppSurface,
    onSurface = AppTextBody,
    outline = AppInputBorder
)

@Composable
fun UzradyabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
