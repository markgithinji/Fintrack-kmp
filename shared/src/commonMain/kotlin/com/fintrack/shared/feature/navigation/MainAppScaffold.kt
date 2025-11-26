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

    // Decide when to show bars
    val hideBars = currentRoute == Screen.TransactionList.route ||
            currentRoute == Screen.BudgetDetail.route ||
            currentRoute == Screen.AddTransaction.route ||
            currentRoute == Screen.Accounts.route ||
            currentRoute == Screen.Categories.route ||
            currentRoute == Screen.Settings.route ||
            currentRoute == Screen.Login.route ||
            currentRoute == Screen.Register.route

    Scaffold(
        topBar = {
            if (!hideBars) {
                AppTopBar(
                    appBarState = appBarState,
                    onUpdateAppBarState = { newState -> appBarState = newState }
                )
            }
        },
        bottomBar = {
            if (!hideBars) {
                BottomBar(navController)
            }
        },
        floatingActionButton = {
            if (!hideBars) {
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