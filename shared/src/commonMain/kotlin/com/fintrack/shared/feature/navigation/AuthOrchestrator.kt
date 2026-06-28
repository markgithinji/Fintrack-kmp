package com.fintrack.shared.feature.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.fintrack.shared.feature.auth.domain.model.AuthState
import com.fintrack.shared.feature.auth.ui.AuthViewModel
import com.fintrack.shared.feature.auth.ui.LockScreen
import com.fintrack.shared.feature.core.ui.CommonErrorState

@Composable
fun AuthOrchestrator(
    authStatus: AuthState<Boolean>,
    currentRoute: String?,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val isAppLocked by authViewModel.isAppLocked.collectAsStateWithLifecycle()

    // LOGIN_DEBUG: Log orchestration decision
    androidx.compose.runtime.SideEffect {
        println("LOGIN_DEBUG: AuthOrchestrator recomposing. authStatus: $authStatus, currentRoute: $currentRoute, isAppLocked: $isAppLocked")
    }

    if (isAppLocked) {
        LockScreen(
            onUnlock = { authViewModel.unlockWithBiometrics() }
        )
        return
    }

    when (authStatus) {
        is AuthState.Loading -> {
            AuthLoadingScreen(message = authStatus.message)
        }

        is AuthState.Success -> {
            MainAppScaffold(
                isAuthenticated = authStatus.data,
                currentRoute = currentRoute,
                navController = navController,
                authViewModel = authViewModel,
                onLogout = { 
                    authViewModel.logout() 
                }
            )
        }

        is AuthState.Error -> {
            AuthErrorScreen(
                error = authStatus.exception,
                onRetry = { authViewModel.checkAuthenticationStatus() }
            )
        }

        is AuthState.Idle -> {
            AuthLoadingScreen(message = "Initializing...")
        }
    }
}

@Composable
fun AuthLoadingScreen(message: String = "") {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
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
                Text(
                    text = message.ifEmpty { "Checking authentication..." },
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun AuthErrorScreen(
    error: Throwable,
    onRetry: () -> Unit
) {
    CommonErrorState(
        modifier = Modifier.fillMaxSize(),
        title = "Authentication error",
        error = error,
        onRetry = onRetry
    )
}