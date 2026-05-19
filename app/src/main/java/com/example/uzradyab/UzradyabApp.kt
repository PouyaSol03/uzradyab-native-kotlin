package com.example.uzradyab

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.uzradyab.feature.auth.signin.SignInRoute
import com.example.uzradyab.feature.home.HomeRoute

@Composable
fun UzradyabApp(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.SignIn.path,
    ) {
        composable(AppRoute.SignIn.path) {
            SignInRoute(
                onSignedIn = {
                    navController.navigate(AppRoute.Home.path) {
                        popUpTo(AppRoute.SignIn.path) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(AppRoute.Home.path) {
            HomeRoute(
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
    Home("/home"),
}
