package com.fintrack.shared.feature.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fintrack.shared.feature.auth.ui.AuthViewModel
import com.fintrack.shared.feature.core.util.Result
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthOrchestrator(
    authStatus: Result<Boolean>,
    currentRoute: String?,
    navController: NavHostController
) {
    LaunchedEffect(authStatus, currentRoute) {
        handleAuthNavigation(authStatus, currentRoute, navController)
    }

    // Show UI based on auth status
    when (authStatus) {
        is Result.Loading -> AuthLoadingScreen()
        is Result.Success -> {
            val isAuthenticated = authStatus.data
            MainAppScaffold(
                isAuthenticated = isAuthenticated,
                currentRoute = currentRoute,
                navController = navController
            )
        }
        is Result.Error -> AuthErrorScreen()
    }
}



// Pure function for navigation logic (easier to test)
private fun handleAuthNavigation(
    authStatus: Result<Boolean>,
    currentRoute: String?,
    navController: NavHostController
) {
    when (authStatus) {
        is Result.Success -> {
            val isAuthenticated = authStatus.data
            if (isAuthenticated && currentRoute == Screen.Login.route) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            } else if (!isAuthenticated && currentRoute != Screen.Login.route) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
        is Result.Error -> {
            if (currentRoute != Screen.Login.route) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
        else -> {
            // Loading state - no navigation needed
        }
    }
}

@Composable
fun AuthLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Checking authentication...")
        }
    }
}

@Composable
fun AuthErrorScreen() {
    val authViewModel: AuthViewModel = koinViewModel() // Get ViewModel here

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Authentication error", color = Color.Red)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { authViewModel.checkAuthenticationStatus() }) {
                Text("Retry")
            }
        }
    }
}