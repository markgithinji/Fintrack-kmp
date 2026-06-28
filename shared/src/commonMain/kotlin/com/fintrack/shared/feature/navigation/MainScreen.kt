package com.fintrack.shared.feature.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fintrack.shared.feature.auth.domain.model.AuthState
import com.fintrack.shared.feature.auth.ui.AuthViewModel
import com.fintrack.shared.feature.settings.ui.CurrencyProvider
import com.fintrack.shared.ui.theme.FinanceTrackerTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen() {
    val isSystemInDarkTheme = isSystemInDarkTheme()

    FinanceTrackerTheme(darkTheme = isSystemInDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            CurrencyProvider {
                val authViewModel: AuthViewModel = koinViewModel()
                val authStatusState by authViewModel.authStatus.collectAsStateWithLifecycle()

                // Use a stable NavController that persists across auth changes.
                val navController = rememberNavController()

                // Track current route
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                AuthOrchestrator(
                    authStatus = authStatusState,
                    currentRoute = currentRoute,
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
        }
    }
}
