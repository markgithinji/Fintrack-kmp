package com.fintrack.shared.feature.transaction.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            IncomeTrackerScreen(
                onAddClicked = { navController.navigate(Screen.AddTransaction.route) },
            )
        }

        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(
                onCancel = { navController.popBackStack() }
            )
        }
    }
}
