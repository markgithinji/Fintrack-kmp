package com.fintrack.shared.feature.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fintrack.shared.feature.auth.ui.AuthViewModel
import com.fintrack.shared.ui.theme.FinanceTrackerTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen() {
    val isSystemInDarkTheme = isSystemInDarkTheme()

    FinanceTrackerTheme(darkTheme = isSystemInDarkTheme) {
        val navController = rememberNavController()
        val authViewModel: AuthViewModel = koinViewModel()
        val authStatus by authViewModel.authStatus.collectAsStateWithLifecycle()

        // Track current route
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        AuthOrchestrator(
            authStatus = authStatus,
            currentRoute = currentRoute,
            navController = navController
        )
    }
}