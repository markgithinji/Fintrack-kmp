package com.fintrack.shared.feature.navigation

import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController

@Composable
fun MainAppScaffold(
    isAuthenticated: Boolean,
    currentRoute: String?,
    navController: NavHostController,
    onLogout: () -> Unit = {}
) {
    // State to update AppBar per screen
    var appBarState by remember { mutableStateOf(AppBarState(title = "Home")) }

    // LOGIN_DEBUG: Log scaffold state
    androidx.compose.runtime.SideEffect {
        println("LOGIN_DEBUG: MainAppScaffold recomposing. authenticated: $isAuthenticated, route: $currentRoute")
    }

    // Show bars only if authenticated and not on login/register screens
    val showTopBar = remember(currentRoute, isAuthenticated) {
        if (!isAuthenticated) return@remember false
        // If route is null, we are just starting up or transitioning, show it by default
        if (currentRoute == null) return@remember true

        when (currentRoute) {
            Screen.Login.route -> false
            Screen.Register.route -> false
            else -> true
        }
    }

    val showBottomBar = remember(currentRoute, isAuthenticated) {
        if (!isAuthenticated) return@remember false
        if (currentRoute == null) return@remember true

        when (currentRoute) {
            Screen.Home.route -> true
            Screen.Statistics.route -> true
            Screen.Budget.route -> true
            Screen.Profile.route -> true
            else -> false
        }
    }

    val showFAB = remember(currentRoute, isAuthenticated) {
        if (!isAuthenticated) return@remember false
        if (currentRoute == null) return@remember true

        when (currentRoute) {
            Screen.Home.route -> true
            Screen.Statistics.route -> true
            Screen.Budget.route -> true
            Screen.Profile.route -> true
            else -> false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (showTopBar) {
                AppTopBar(
                    appBarState = appBarState,
                    onUpdateAppBarState = { newState -> appBarState = newState }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                BottomBar(navController)
            }
        },
        floatingActionButton = {
            if (showFAB) {
                AddTransactionFAB(
                    onClick = { navController.navigate(Screen.AddTransaction.route) }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        AppNavigation(
            isAuthenticated = isAuthenticated,
            navController = navController,
            paddingValues = paddingValues,
            onUpdateAppBarState = { newState -> appBarState = newState },
            onLogout = onLogout
        )
    }
}