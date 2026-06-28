package com.example.uzradyab.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.uzradyab.core.biometric.BiometricHelper
import com.example.uzradyab.presentation.onboarding.OnboardingScreen
import com.example.uzradyab.presentation.startup.StartupRoute

fun NavGraphBuilder.startupNavGraph(navController: NavHostController, biometricHelper: BiometricHelper) {
    composable(AppRoute.Startup.path) {
        StartupRoute(
            onNavigateToOnboarding = {
                navController.navigate(AppRoute.Onboarding.path) {
                    popUpTo(AppRoute.Startup.path) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onNavigateToSignIn = {
                navController.navigate(AppRoute.SignIn.path) {
                    popUpTo(AppRoute.Startup.path) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onNavigateToHome = {
                navController.navigate(AppRoute.Home.path) {
                    popUpTo(AppRoute.Startup.path) { inclusive = true }
                    launchSingleTop = true
                }
            },
            biometricHelper = biometricHelper
        )
    }
    composable(AppRoute.Onboarding.path) {
        OnboardingScreen(
            onOnboardingFinished = {
                navController.navigate(AppRoute.SignIn.path) {
                    popUpTo(AppRoute.Onboarding.path) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }
}
