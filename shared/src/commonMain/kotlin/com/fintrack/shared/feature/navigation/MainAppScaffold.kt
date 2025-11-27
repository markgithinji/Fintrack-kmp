package com.fintrack.shared.feature.navigation

import androidx.compose.material3.FabPosition
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
    navController: NavHostController
) {
    // State to update AppBar per screen
    var appBarState by remember { mutableStateOf(AppBarState(title = "Home")) }

    val showTopBar = remember(currentRoute) {
        when (currentRoute) {
            Screen.Login.route -> false
            Screen.Register.route -> false
            else -> true  // Show top bar on all other screens
        }
    }

    val showBottomBar = remember(currentRoute) {
        when (currentRoute) {
            Screen.Home.route -> true
            Screen.Statistics.route -> true
            Screen.Budget.route -> true
            Screen.Profile.route -> true
            Screen.Login.route -> false
            Screen.Register.route -> false
            else -> false  // Hide bottom bar on all other screens
        }
    }

    val showFAB = remember(currentRoute) {
        when (currentRoute) {
            Screen.Home.route -> true
            Screen.Statistics.route -> true
            Screen.Budget.route -> true
            Screen.Profile.route -> true
            else -> false  // Hide FAB on all other screens
        }
    }

    Scaffold(
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
            onUpdateAppBarState = { newState -> appBarState = newState }
        )
    }
}