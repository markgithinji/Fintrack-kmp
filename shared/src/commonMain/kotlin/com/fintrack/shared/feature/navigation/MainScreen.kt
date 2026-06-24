package com.fintrack.shared.feature.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fintrack.shared.feature.auth.domain.model.AuthState
import com.fintrack.shared.feature.auth.ui.AuthViewModel
import com.fintrack.shared.ui.theme.FinanceTrackerTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen() {
    val isSystemInDarkTheme = isSystemInDarkTheme()

    FinanceTrackerTheme(darkTheme = isSystemInDarkTheme) {
        val authViewModel: AuthViewModel = koinViewModel()
        val authStatus by authViewModel.authStatus.collectAsStateWithLifecycle()

        // Derive isAuthenticated.
        val isAuthenticated = (authStatus as? AuthState.Success<Boolean>)?.data == true
        
        // LOGOUT_DEBUG: Log state change using println to be visible in Logcat
        androidx.compose.runtime.LaunchedEffect(authStatus) {
            println("LOGOUT_DEBUG: [5] MainScreen state change: $authStatus (isAuthenticated: $isAuthenticated)")
        }

        // Use a stable NavController that persists across auth changes.
        // We'll handle backstack clearing via navigation logic instead of recreating the controller.
        val navController = androidx.navigation.compose.rememberNavController()

        // Explicitly handle logout navigation to ensure it's clean and doesn't flicker
        androidx.compose.runtime.LaunchedEffect(isAuthenticated) {
            if (!isAuthenticated) {
                // If we are logged out, force navigation to Login and clear backstack
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        // Track current route
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        AuthOrchestrator(
            authStatus = authStatus,
            currentRoute = currentRoute,
            navController = navController,
            authViewModel = authViewModel
        )
    }
}