package com.example.uzradyab.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.uzradyab.BuildConfig
import com.example.uzradyab.presentation.alerts.AlertsSettingsRoute
import com.example.uzradyab.presentation.debug.DebugLogScreen
import com.example.uzradyab.safePopBackStack

fun NavGraphBuilder.settingsNavGraph(navController: NavHostController) {
    if (BuildConfig.DEBUG) {
        composable(AppRoute.DebugLog.path) {
            DebugLogScreen(
                onBackClick = { navController.safePopBackStack() }
            )
        }
    }
    composable("alerts_settings") {
        AlertsSettingsRoute(
            onBackClick = { navController.safePopBackStack() }
        )
    }
}
