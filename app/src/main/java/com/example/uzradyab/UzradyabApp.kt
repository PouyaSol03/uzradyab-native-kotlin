package com.example.uzradyab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.SnackbarHostState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.uzradyab.presentation.components.AppSnackbarControllerImpl
import com.example.uzradyab.presentation.components.GlobalSnackbarHost
import com.example.uzradyab.presentation.components.LocalSnackbarController
import com.example.uzradyab.BuildConfig
import com.example.uzradyab.core.biometric.BiometricHelper
import com.example.uzradyab.core.network.SessionEventBus
import com.example.uzradyab.domain.repository.AuthRepository
import com.example.uzradyab.presentation.alerts.AlertsSettingsRoute
import com.example.uzradyab.presentation.auth.LoginRoute
import com.example.uzradyab.presentation.auth.RegisterRoute
import com.example.uzradyab.presentation.debug.DebugLogScreen
import com.example.uzradyab.presentation.device.AddDeviceRoute
import com.example.uzradyab.presentation.map.HomeMapRoute
import com.example.uzradyab.presentation.onboarding.OnboardingScreen
import com.example.uzradyab.presentation.reports.ReportsRoute
import com.example.uzradyab.presentation.startup.StartupRoute
import com.example.uzradyab.presentation.replay.ReplayTripRoute
import com.example.uzradyab.presentation.command.CommandCenterRoute
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun UzradyabApp(
    biometricHelper: BiometricHelper? = null,
    sessionEventBus: SessionEventBus? = null,
    authRepository: AuthRepository? = null,
    navController: NavHostController = rememberNavController(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val actualBiometricHelper = remember(biometricHelper) {
        biometricHelper ?: BiometricHelper(context.applicationContext)
    }

    // Listen for 401 Unauthorized events globally
    LaunchedEffect(sessionEventBus, authRepository) {
        sessionEventBus?.unauthorizedEvent?.collectLatest {
            authRepository?.logout()
            navController.navigate(AppRoute.SignIn.path) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarController = remember(snackbarHostState, scope) { 
        AppSnackbarControllerImpl(snackbarHostState, scope) 
    }

    CompositionLocalProvider(LocalSnackbarController provides snackbarController) {
        Box(modifier = Modifier.fillMaxSize()) {
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
        composable(
            route = AppRoute.SignIn.path,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
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
        composable(
            route = AppRoute.Register.path,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
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
                onEventsClick = { deviceId ->
                    if (deviceId != null) {
                        navController.navigate("${AppRoute.Events.path}?deviceId=$deviceId") {
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(AppRoute.Events.path) {
                            launchSingleTop = true
                        }
                    }
                },
                onDevicesClick = {
                    navController.navigate(AppRoute.Devices.path) {
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    navController.navigate(AppRoute.Profile.path) {
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
        composable(
            route = "${AppRoute.Events.path}?deviceId={deviceId}",
            arguments = listOf(
                navArgument("deviceId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            com.example.uzradyab.presentation.events.EventsReportRoute(
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }
        composable(AppRoute.Devices.path) {
            com.example.uzradyab.presentation.device.DevicesRoute(
                onAddDeviceClick = {
                    navController.navigate(AppRoute.AddDevice.path) {
                        launchSingleTop = true
                    }
                },
                onMenuClick = {
                    navController.popBackStack() // Or handle drawer
                },
                onEditDeviceClick = { deviceId ->
                    navController.navigate("${AppRoute.AddDevice.path}?deviceId=$deviceId") {
                        launchSingleTop = true
                    }
                },
                onRenewCreditClick = { deviceId ->
                    navController.navigate("${AppRoute.RenewCredit.path}?deviceId=$deviceId") {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(AppRoute.Profile.path) {
            com.example.uzradyab.presentation.profile.ProfileRoute(
                onLogoutClick = {
                    navController.navigate(AppRoute.SignIn.path) {
                        popUpTo(AppRoute.Home.path) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onMenuClick = {
                    // Usually opens drawer, for now pop back stack as in Devices
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = "${AppRoute.RenewCredit.path}?deviceId={deviceId}",
            arguments = listOf(
                navArgument("deviceId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            com.example.uzradyab.presentation.device.RenewCreditRoute(
                onBackClick = { navController.popBackStack() }
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
                },
                onNavigateToDeviceStatus = {
                    navController.navigate(AppRoute.DeviceStatus.path) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(AppRoute.DeviceStatus.path) {
            com.example.uzradyab.presentation.reports.DeviceStatusRoute(
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
                },
                onTraveledPathsClick = {
                    navController.popBackStack()
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
            
            GlobalSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
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
    Devices("/devices"),
    Profile("/profile"),
    RenewCredit("/renew-credit"),
    Events("/events"),
    AddDevice("/add-device"),
    Reports("/reports"),
    DeviceStatus("/device-status"),
    ReplayTrip("/replay-trip"),
    CommandCenter("/command-center"),
    DebugLog("/debug-logs"),
}
