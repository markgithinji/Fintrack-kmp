package com.fintrack.shared.feature.navigation.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fintrack.shared.feature.auth.ui.LoginScreen
import com.fintrack.shared.feature.auth.ui.RegisterScreen
import com.fintrack.shared.feature.navigation.model.Screen
import com.fintrack.shared.feature.navigation.ui.LocalNavController

@Composable
fun AuthNavigation(
    paddingValues: PaddingValues = PaddingValues()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login,
        modifier = Modifier.fillMaxSize(),
        enterTransition = { fadeIn(animationSpec = tween(250)) },
        exitTransition = { fadeOut(animationSpec = tween(250)) }
    ) {
        composable<Screen.Login> {
            LoginScreen(
                onLoginSuccess = {
                    // Handled globally via authStatus update in ViewModel
                },
                onSignUp = {
                    navController.navigate(Screen.Register) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                },
                onForgotPassword = {
                    // TODO: Handle forgot password
                },
                modifier = Modifier.padding(paddingValues)
            )
        }

        composable<Screen.Register> {
            RegisterScreen(
                onRegisterSuccess = {
                    // Handled globally via authStatus update in ViewModel
                },
                onLogin = {
                    navController.navigate(Screen.Login) {
                        popUpTo(Screen.Register) { inclusive = true }
                    }
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
