package com.example.uzradyab.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Colors for the in-app routing screens (light only).
 * Blue and vehicle magenta come from the existing app palette so routing matches Home.
 */
object RoutingColors {
    val primary = AppBlue
    val onPrimary = Color.White
    val primaryContainer = Color(0xFFDCE8FC)
    val onPrimaryContainer = Color(0xFF0B2E66)

    val banner = AppBlueDark
    val bannerNext = Color(0xFF1B55B3)

    val surface = Color.White
    val surfaceContainerLow = Color(0xFFF4F5FA)
    val surfaceContainerHigh = Color(0xFFE9ECF4)
    val onSurface = Color(0xFF1A1B20)
    val onSurfaceVariant = Color(0xFF4A4F5C)
    val outline = Color(0xFF757A87)
    val outlineVariant = Color(0xFFD5D8E2)

    // Same magenta as the Home device marker
    val vehicle = Color(0xFFA22887)
    val vehicleContainer = Color(0xFFFBE3F2)
    val onVehicleContainer = Color(0xFF5E1049)

    val error = Color(0xFFBA1A1A)
    val errorContainer = Color(0xFFFFDAD6)

    val warnBanner = Color(0xFF7A4A00)
    val warnMuted = Color(0xFFC4A57A)

    val success = Color(0xFF1E7F4F)

    val routeAlternative = Color(0xFF9AA3B5)
    val routeTraveled = Color(0xFFA9B0BE)
    val mapBackground = Color(0xFFF1EEE8)

    val scrim = Color(0xFF141828)
}
