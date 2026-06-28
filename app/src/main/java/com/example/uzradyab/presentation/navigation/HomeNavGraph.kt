package com.example.uzradyab.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.uzradyab.BuildConfig
import com.example.uzradyab.presentation.command.CommandCenterRoute
import com.example.uzradyab.presentation.device.AddDeviceRoute
import com.example.uzradyab.presentation.geofence.GeofenceRoute
import com.example.uzradyab.presentation.map.HomeMapRoute
import com.example.uzradyab.presentation.replay.ReplayTripRoute
import com.example.uzradyab.safePopBackStack

fun NavGraphBuilder.homeNavGraph(navController: NavHostController) {
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
}
