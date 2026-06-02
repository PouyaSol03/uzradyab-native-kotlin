package com.example.uzradyab

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.uzradyab.presentation.auth.LoginRoute
import com.example.uzradyab.presentation.auth.RegisterRoute
import com.example.uzradyab.presentation.map.HomeMapRoute

@Composable
fun UzradyabApp(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.SignIn.path,
    ) {
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
                }
            )
        }
    }
}

private enum class AppRoute(val path: String) {
    SignIn("/signin"),
    Register("/register"),
    Home("/home"),
}
