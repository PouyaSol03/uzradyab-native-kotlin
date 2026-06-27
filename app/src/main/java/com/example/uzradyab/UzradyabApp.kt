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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.uzradyab.presentation.geofence.GeofenceRoute
import com.example.uzradyab.presentation.reports.StopReportsRoute
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import com.example.uzradyab.ui.theme.AppBlue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape

fun NavHostController.safePopBackStack() {
    if (currentDestination?.route != AppRoute.Home.path && previousBackStackEntry != null) {
        popBackStack()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UzradyabApp(
    biometricHelper: BiometricHelper? = null,
    sessionEventBus: SessionEventBus? = null,
    networkEventBus: com.example.uzradyab.core.network.NetworkEventBus? = null,
    authRepository: AuthRepository? = null,
    navController: NavHostController = rememberNavController(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val actualBiometricHelper = remember(biometricHelper) {
        biometricHelper ?: BiometricHelper(context.applicationContext)
    }

    // Listen for 401 Unauthorized events globally
    LaunchedEffect(sessionEventBus, authRepository, navController) {
        sessionEventBus?.unauthorizedEvent?.collectLatest {
            authRepository?.logout()
            if (navController.currentDestination?.route != AppRoute.SignIn.path) {
                navController.navigate(AppRoute.SignIn.path) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    var showNetworkErrorSheet by remember { mutableStateOf(false) }
    var lastNetworkErrorTime by rememberSaveable { mutableStateOf(0L) }

    LaunchedEffect(networkEventBus) {
        networkEventBus?.networkErrorEvent?.collectLatest {
            val currentTime = System.currentTimeMillis()
            val oneHourMs = 60L * 60L * 1000L
            if (currentTime - lastNetworkErrorTime > oneHourMs) {
                showNetworkErrorSheet = true
                lastNetworkErrorTime = currentTime
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
                    navController.safePopBackStack()
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
                onGeofenceClick = { deviceId ->
                    navController.navigate("${AppRoute.Geofence.path}?deviceId=$deviceId") {
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
            route = "${AppRoute.Geofence.path}?deviceId={deviceId}",
            arguments = listOf(
                navArgument("deviceId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) {
            GeofenceRoute(
                onBackClick = {
                    navController.safePopBackStack()
                }
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
                    navController.safePopBackStack()
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
                    navController.safePopBackStack() // Or handle drawer
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
                    navController.safePopBackStack()
                },
                onMenuClick = {
                    // Usually opens drawer, for now pop back stack as in Devices
                    navController.safePopBackStack()
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
                onBackClick = { navController.safePopBackStack() }
            )
        }
        composable(AppRoute.Reports.path) {
            ReportsRoute(
                onBackClick = {
                    navController.safePopBackStack()
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
                },
                onNavigateToDailyReport = {
                    navController.navigate(AppRoute.DailyReport.path) {
                        launchSingleTop = true
                    }
                },
                onNavigateToStopReports = {
                    navController.navigate(AppRoute.StopReports.path) {
                        launchSingleTop = true
                    }
                },
                onNavigateToReplayTrip = { deviceId ->
                    val path = if (deviceId != null) "${AppRoute.ReplayTrip.path}?deviceId=$deviceId" else AppRoute.ReplayTrip.path
                    navController.navigate(path) {
                        launchSingleTop = true
                    }
                },
                onNavigateToEvents = {
                    navController.navigate(AppRoute.Events.path) { launchSingleTop = true }
                },
                onNavigateToTripReports = {
                    navController.navigate(AppRoute.TripReports.path) { launchSingleTop = true }
                }
            )
        }
        composable(AppRoute.DeviceStatus.path) {
            com.example.uzradyab.presentation.reports.DeviceStatusRoute(
                onBackClick = {
                    navController.safePopBackStack()
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
                    navController.safePopBackStack()
                }
            )
        }
        composable(AppRoute.DailyReport.path) {
            com.example.uzradyab.presentation.reports.DailyReportRoute(
                onBackClick = {
                    navController.safePopBackStack()
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
                    navController.navigate(AppRoute.ReplayTrip.path) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(AppRoute.StopReports.path) {
            StopReportsRoute(
                onBackClick = {
                    navController.safePopBackStack()
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
                    navController.safePopBackStack()
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
                onBackClick = { navController.safePopBackStack() }
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
                onBackClick = { navController.safePopBackStack() }
            )
        }
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
        composable(AppRoute.TripReports.path) {
            com.example.uzradyab.presentation.reports.TripReportsRoute(
                onBackClick = { navController.safePopBackStack() },
                onLogoutClick = {
                    scope.launch {
                        authRepository?.logout()
                    }
                    navController.navigate(AppRoute.SignIn.path) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onAddDeviceClick = {
                    navController.navigate(AppRoute.AddDevice.path) { launchSingleTop = true }
                }
            )
        }
        composable(AppRoute.Events.path) {
            com.example.uzradyab.presentation.events.EventsReportRoute(
                onBackClick = { navController.safePopBackStack() }
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

            if (showNetworkErrorSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showNetworkErrorSheet = false },
                    containerColor = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 8.dp)
                            .navigationBarsPadding(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Warning Icon in a circular badge
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(Color(0xFFFFF4E5), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFE5B850),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Centered Title
                        Text(
                            text = "خطا در اتصال به شبکه",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = com.example.uzradyab.ui.theme.AppTextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Centered and slightly larger description
                        Text(
                            text = "ارتباط با سرور برقرار نشد.\nلطفاً اتصال اینترنت خود را بررسی کرده یا در صورت روشن بودن VPN، آن را خاموش کنید.",
                            fontSize = 15.sp,
                            color = com.example.uzradyab.ui.theme.AppTextPrimary.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Modern Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .background(AppBlue, RoundedCornerShape(12.dp))
                                .clickable { showNetworkErrorSheet = false },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "متوجه شدم",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            // Local Crash Reporter Dialog
            var crashLogToShow by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(Unit) {
                crashLogToShow = com.example.uzradyab.core.debug.LocalCrashReporter.getCrashLog(context)
            }

            if (crashLogToShow != null) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { },
                    title = { Text("App Recovered from a Crash") },
                    text = {
                        Column {
                            Text(
                                text = "Please share this crash log with the developer so they can fix the issue.",
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false)
                                    .background(Color(0xFFF1F1F1))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = crashLogToShow!!,
                                    fontSize = 10.sp,
                                    maxLines = 15,
                                    color = Color.Black
                                )
                            }
                        }
                    },
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = {
                            val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                putExtra(android.content.Intent.EXTRA_TEXT, crashLogToShow!!)
                                type = "text/plain"
                            }
                            context.startActivity(android.content.Intent.createChooser(sendIntent, "Share Crash Log"))
                        }) {
                            Text("Share Log")
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = {
                            com.example.uzradyab.core.debug.LocalCrashReporter.clearCrashLog(context)
                            crashLogToShow = null
                        }) {
                            Text("Clear & Continue")
                        }
                    }
                )
            }
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
    DailyReport("/daily-report"),
    StopReports("/stop-reports"),
    ReplayTrip("/replay-trip"),
    CommandCenter("/command-center"),
    DebugLog("/debug-logs"),
    Geofence("/geofences"),
    TripReports("/trip-reports"),
}
