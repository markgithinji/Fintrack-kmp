package com.fintrack.shared.feature.navigation.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.shared.feature.auth.domain.model.AuthState
import com.fintrack.shared.feature.auth.ui.AuthViewModel
import com.fintrack.shared.feature.auth.ui.LockScreen
import com.fintrack.shared.feature.settings.domain.model.AppTheme
import com.fintrack.shared.feature.navigation.model.Screen
import com.fintrack.shared.feature.settings.domain.util.BiometricResult
import com.fintrack.shared.ui.theme.FinanceTrackerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen(
    initialTransactionId: String? = null,
    onTransactionIdConsumed: () -> Unit = {},
    mainViewModel: MainViewModel = koinViewModel()
) {
    val appTheme by mainViewModel.theme.collectAsStateWithLifecycle()
    
    val isDarkTheme = when (appTheme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    FinanceTrackerTheme(darkTheme = isDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            AppStateProvider(viewModel = mainViewModel) {
                val navController = LocalNavController.current
                val authViewModel: AuthViewModel = koinViewModel()
                val authStatusState by authViewModel.authStatus.collectAsStateWithLifecycle()
                val isAppLocked by authViewModel.isAppLocked.collectAsStateWithLifecycle()
                val biometricAuthenticator = LocalBiometricAuthenticator.current
                val scope = rememberCoroutineScope()

                // Track the last error for smooth transitions on retry
                var lastError by remember { mutableStateOf<AuthState.Error?>(null) }

                LaunchedEffect(authStatusState) {
                    when (authStatusState) {
                        is AuthState.Error -> lastError = authStatusState as AuthState.Error
                        is AuthState.Success -> {
                            if (lastError != null) {
                                delay(1500)
                                lastError = null
                            }
                        }
                        else -> {}
                    }
                }

                // Handle initial navigation (e.g., from notifications)
                LaunchedEffect(initialTransactionId, authStatusState) {
                    val auth = authStatusState
                    if (initialTransactionId != null && auth is AuthState.Success && auth.data == true) {
                        navController.navigate(Screen.AddTransaction(initialTransactionId))
                        onTransactionIdConsumed()
                    }
                }

                if (isAppLocked) {
                    LockScreen(
                        onUnlock = {
                            scope.launch {
                                val result = biometricAuthenticator.authenticate(
                                    title = "Unlock Fintrack",
                                    subtitle = "Authenticate to access your account"
                                )
                                if (result is BiometricResult.Success) {
                                    authViewModel.unlockWithBiometrics()
                                }
                            }
                        }
                    )
                } else {
                    when (authStatusState) {
                        is AuthState.Loading -> {
                            if (lastError != null) {
                                AuthErrorScreen(
                                    error = lastError!!.exception,
                                    isLoading = true,
                                    onRetry = { authViewModel.checkAuthenticationStatus() }
                                )
                            } else {
                                AuthLoadingScreen(message = (authStatusState as AuthState.Loading).message)
                            }
                        }
                        is AuthState.Error -> {
                            AuthErrorScreen(
                                error = (authStatusState as AuthState.Error).exception,
                                onRetry = { authViewModel.checkAuthenticationStatus() }
                            )
                        }
                        is AuthState.Success, is AuthState.Idle -> {
                            if (lastError != null) {
                                AuthErrorScreen(
                                    error = lastError!!.exception,
                                    isSuccess = true,
                                    onRetry = { authViewModel.checkAuthenticationStatus() }
                                )
                            } else {
                                val isAuthenticated = (authStatusState as? AuthState.Success)?.data ?: false
                                MainAppScaffold(
                                    isAuthenticated = isAuthenticated,
                                    authViewModel = authViewModel,
                                    onLogout = { authViewModel.logout() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
