package com.fintrack.shared.feature.navigation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import com.fintrack.shared.feature.navigation.ui.components.AuthErrorScreen
import com.fintrack.shared.feature.navigation.ui.components.AuthLoadingScreen
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
                    if (initialTransactionId != null && auth is AuthState.Success && auth.data) {
                        navController.navigate(Screen.AddTransaction(initialTransactionId))
                        onTransactionIdConsumed()
                    }
                }

                AnimatedContent(
                    targetState = isAppLocked to authStatusState,
                    transitionSpec = {
                        if (initialState.first && !targetState.first) {
                            // Transition from Locked to Unlocked
                            (fadeIn(animationSpec = tween(600)) + 
                             scaleIn(initialScale = 0.92f, animationSpec = tween(600, easing = FastOutSlowInEasing)) +
                             slideInVertically(initialOffsetY = { it / 10 }, animationSpec = tween(600, easing = FastOutSlowInEasing))
                            ).togetherWith(
                                fadeOut(animationSpec = tween(600)) + 
                                scaleOut(targetScale = 1.08f, animationSpec = tween(600, easing = FastOutSlowInEasing))
                            )
                        } else if (initialState.second is AuthState.Loading && targetState.second is AuthState.Success) {
                            // Transition from Loading to Success (App start)
                            (fadeIn(animationSpec = tween(800)) + 
                             scaleIn(initialScale = 0.85f, animationSpec = tween(800, easing = FastOutSlowInEasing))
                            ).togetherWith(
                                fadeOut(animationSpec = tween(600))
                            )
                        } else {
                            fadeIn(animationSpec = tween(500)).togetherWith(fadeOut(animationSpec = tween(500)))
                        }
                    },
                    label = "MainAppTransition"
                ) { (locked, authState) ->
                    if (locked) {
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
                        when (authState) {
                            is AuthState.Loading -> {
                                if (lastError != null) {
                                    AuthErrorScreen(
                                        error = lastError!!.exception,
                                        isLoading = true,
                                        onRetry = { authViewModel.checkAuthenticationStatus() }
                                    )
                                } else {
                                    AuthLoadingScreen(message = authState.message)
                                }
                            }
                            is AuthState.Error -> {
                                AuthErrorScreen(
                                    error = authState.exception,
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
                                    val isAuthenticated = (authState as? AuthState.Success)?.data ?: false
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
}
