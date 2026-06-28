package com.example.uzradyab.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.uzradyab.presentation.auth.LoginRoute
import com.example.uzradyab.presentation.auth.RegisterRoute
import com.example.uzradyab.safePopBackStack

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
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
}
