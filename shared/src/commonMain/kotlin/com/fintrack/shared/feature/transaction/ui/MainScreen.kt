package com.fintrack.shared.feature.transaction.ui

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomBar(navController = navController) },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.size(60.dp).offset(y = 60.dp),
                onClick = { navController.navigate(Screen.AddTransaction.route) },
                containerColor = Color.Black,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->


        NavHost(
            navController = navController,
            startDestination = Screen.Budget.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                IncomeTrackerContent()
            }

            composable(Screen.AddTransaction.route) {
                AddTransactionScreen(
                    onCancel = { navController.popBackStack() }
                )
            }

            composable(Screen.Statistics.route) {
                StatisticsScreen()
            }

            composable(Screen.Budget.route) {
                BudgetScreen(
                    onAddBudget = { navController.navigate(Screen.BudgetDetail.createRoute(null)) },
                    onBudgetClick = { budget ->
                        navController.navigate(Screen.BudgetDetail.createRoute(budget.id))
                    }
                )
            }

            composable(
                route = Screen.BudgetDetail.route,
                arguments = listOf(navArgument("budgetId") { type = NavType.IntType })
            ) { backStackEntry ->
                // IDE might shows "Unresolved reference 'getInt'", but this works at runtime
                val budgetId = backStackEntry.arguments?.getInt("budgetId")
                BudgetDetailScreen(
                    budgetId = budgetId,
                    onSave = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
        }


    }
}