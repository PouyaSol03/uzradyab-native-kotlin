package com.example.uzradyab.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AppBlueDarkTheme,
    onPrimary = Color.White,
    secondary = AppTextPrimaryDark,
    background = AppBackgroundDark,
    surface = AppSurfaceDark,
    onSurface = AppTextPrimaryDark
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

object UzradyabTheme {
    const val ENABLE_DARK_MODE = false // Hardcoded flag to enable/disable Dark Mode

    val colors: UzradyabColors
        @Composable
        @ReadOnlyComposable
        get() = LocalUzradyabColors.current
}

@Composable
fun UzradyabTheme(
    darkTheme: Boolean = UzradyabTheme.ENABLE_DARK_MODE && isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val customColors = if (darkTheme) darkUzradyabColors else lightUzradyabColors

    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = (view.context as android.app.Activity).window
            androidx.core.view.WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalUzradyabColors provides customColors,
        LocalIsDarkTheme provides darkTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
