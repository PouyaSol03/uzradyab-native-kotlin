package com.example.uzradyab.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.uzradyab.domain.repository.AuthRepository
import com.example.uzradyab.presentation.reports.ReportsRoute
import com.example.uzradyab.presentation.reports.StopReportsRoute
import com.example.uzradyab.safePopBackStack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun NavGraphBuilder.reportsNavGraph(navController: NavHostController, authRepository: AuthRepository?, scope: CoroutineScope) {
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
}
