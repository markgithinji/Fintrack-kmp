package com.fintrack.shared.feature.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay

@Composable
fun AuthOrchestrator(
    authStatus: AuthState<Boolean>,
    currentRoute: String?,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val isAppLocked by authViewModel.isAppLocked.collectAsStateWithLifecycle()

    // Track the last error to determine if we are in a retry/transition flow.
    // Using this as a 'gate' avoids the flicker between Error -> Home -> Success -> Home.
    var lastError by remember { mutableStateOf<AuthState.Error?>(null) }

    LaunchedEffect(authStatus) {
        when (authStatus) {
            is AuthState.Error -> {
                lastError = authStatus
            }
            is AuthState.Success -> {
                // If we succeeded and were previously in an error state, 
                // stay on the error screen to show the success button for a moment.
                if (lastError != null) {
                    delay(1500)
                    lastError = null
                }
            }
            else -> {}
        }
    }

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
            if (lastError != null) {
                // We are retrying from an error state
                AuthErrorScreen(
                    error = lastError!!.exception,
                    isLoading = true,
                    onRetry = { authViewModel.checkAuthenticationStatus() }
                )
            } else {
                AuthLoadingScreen(message = authStatus.message)
            }
        }

        is AuthState.Success -> {
            if (lastError != null) {
                // We just succeeded after an error; show success in the button
                AuthErrorScreen(
                    error = lastError!!.exception,
                    isSuccess = true,
                    onRetry = { authViewModel.checkAuthenticationStatus() }
                )
            } else {
                // Clean transition to the main app
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
            Surface(
                modifier = Modifier
                    .padding(32.dp)
                    .widthIn(max = 400.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        strokeWidth = 4.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = message.ifEmpty { "Checking authentication..." },
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please wait a moment",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun AuthErrorScreen(
    error: Throwable,
    isLoading: Boolean = false,
    isSuccess: Boolean = false,
    onRetry: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                CommonErrorState(
                    modifier = Modifier.padding(16.dp),
                    title = "Authentication Error",
                    error = error,
                    isLoading = isLoading,
                    isSuccess = isSuccess,
                    onRetry = onRetry
                )
            }
        }
    }
}
