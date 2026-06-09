package com.example.uzradyab

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.uzradyab.BuildConfig
import com.example.uzradyab.core.biometric.BiometricHelper
import com.example.uzradyab.presentation.alerts.AlertsSettingsRoute
import com.example.uzradyab.presentation.auth.LoginRoute
import com.example.uzradyab.presentation.auth.RegisterRoute
import com.example.uzradyab.presentation.debug.DebugLogScreen
import com.example.uzradyab.presentation.device.AddDeviceRoute
import com.example.uzradyab.presentation.events.EventsDevicesRoute
import com.example.uzradyab.presentation.map.HomeMapRoute
import com.example.uzradyab.presentation.onboarding.OnboardingScreen
import com.example.uzradyab.presentation.reports.ReportsRoute
import com.example.uzradyab.presentation.startup.StartupRoute
import com.example.uzradyab.presentation.replay.ReplayTripRoute
import com.example.uzradyab.presentation.command.CommandCenterRoute

@Composable
fun UzradyabApp(
    biometricHelper: BiometricHelper? = null,
    navController: NavHostController = rememberNavController(),
) {
    val context = LocalContext.current
    val actualBiometricHelper = remember(biometricHelper) {
        biometricHelper ?: BiometricHelper(context.applicationContext)
    }

    NavHost(
        navController = navController,
        startDestination = AppRoute.Startup.path,
    ) {
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
                biometricHelper = actualBiometricHelper
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
        composable(AppRoute.SignIn.path) {
            LoginRoute(
                onSignedIn = {
                    navController.navigate(AppRoute.Home.path) {
                        popUpTo(AppRoute.SignIn.path) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onRegisterClick = {
                    navController.navigate(AppRoute.Register.path) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(AppRoute.Register.path) {
            RegisterRoute(
                onSignedIn = {
                    navController.navigate(AppRoute.Home.path) {
                        popUpTo(AppRoute.SignIn.path) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                },
            )
        }
        composable(AppRoute.Home.path) {
            HomeMapRoute(
                onSignedOut = {
                    navController.navigate(AppRoute.SignIn.path) {
                        popUpTo(AppRoute.Home.path) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onEventsClick = {
                    navController.navigate(AppRoute.Events.path) {
                        launchSingleTop = true
                    }
                },
                onAddDeviceClick = {
                    navController.navigate(AppRoute.AddDevice.path) {
                        launchSingleTop = true
                    }
                },
                onDeviceSpecsClick = { deviceId ->
                    navController.navigate("${AppRoute.AddDevice.path}?deviceId=$deviceId&isReadOnly=true") {
                        launchSingleTop = true
                    }
                },
                onDeviceSettingsClick = { deviceId ->
                    navController.navigate("${AppRoute.AddDevice.path}?deviceId=$deviceId&isReadOnly=false") {
                        launchSingleTop = true
                    }
                },
                onReplayTripClick = { deviceId ->
                    navController.navigate("${AppRoute.ReplayTrip.path}?deviceId=$deviceId") {
                        launchSingleTop = true
                    }
                },
                onCommandsClick = { deviceId ->
                    navController.navigate("${AppRoute.CommandCenter.path}?deviceId=$deviceId") {
                        launchSingleTop = true
                    }
                },
                onReportsClick = {
                    navController.navigate(AppRoute.Reports.path) {
                        launchSingleTop = true
                    }
                },
                onAlertsSettingsClick = {
                    navController.navigate("alerts_settings") {
                        launchSingleTop = true
                    }
                },
                onDebugLogsClick = if (BuildConfig.DEBUG) ({
                    navController.navigate(AppRoute.DebugLog.path) {
                        launchSingleTop = true
                    }
                }) else null,
            )
        }
        composable(AppRoute.Events.path) {
            EventsDevicesRoute(
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }
        composable(AppRoute.Reports.path) {
            ReportsRoute(
                onBackClick = {
                    navController.popBackStack()
                },
                onLogoutClick = {
                    navController.navigate(AppRoute.SignIn.path) {
                        popUpTo(AppRoute.Home.path) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onAddDeviceClick = {
                    navController.navigate(AppRoute.AddDevice.path) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = "${AppRoute.AddDevice.path}?deviceId={deviceId}&isReadOnly={isReadOnly}",
            arguments = listOf(
                navArgument("deviceId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("isReadOnly") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            AddDeviceRoute(
                onBackClick = {
                    navController.popBackStack()
                },
                onSignedOut = {
                    navController.navigate(AppRoute.SignIn.path) {
                        popUpTo(AppRoute.Home.path) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = "${AppRoute.ReplayTrip.path}?deviceId={deviceId}",
            arguments = listOf(
                navArgument("deviceId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            ReplayTripRoute(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route = "${AppRoute.CommandCenter.path}?deviceId={deviceId}",
            arguments = listOf(
                navArgument("deviceId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            CommandCenterRoute(
                onBackClick = { navController.popBackStack() }
            )
        }
        if (BuildConfig.DEBUG) {
            composable(AppRoute.DebugLog.path) {
                DebugLogScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
        composable("alerts_settings") {
            AlertsSettingsRoute(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

private enum class AppRoute(val path: String) {
    Startup("/startup"),
    Onboarding("/onboarding"),
    SignIn("/signin"),
    Register("/register"),
    Home("/home"),
    Events("/events"),
    AddDevice("/add-device"),
    Reports("/reports"),
    ReplayTrip("/replay-trip"),
    CommandCenter("/command-center"),
    DebugLog("/debug-logs"),
}
