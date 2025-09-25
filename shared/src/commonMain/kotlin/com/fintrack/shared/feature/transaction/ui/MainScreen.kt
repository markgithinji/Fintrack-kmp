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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fintrack.shared.feature.auth.ui.LoginScreen
import com.fintrack.shared.feature.budget.ui.BudgetDetailScreen
import com.fintrack.shared.feature.budget.ui.BudgetScreen
import com.fintrack.shared.feature.summary.ui.StatisticsScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    // State to update AppBar per screen
    var appBarState by remember { mutableStateOf(AppBarState(title = "Home")) }

    // Track current route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Decide when to show bars
    val hideBars = currentRoute == Screen.TransactionList.route ||
            currentRoute == Screen.BudgetDetail.route

    Scaffold(
        topBar = {
            TopBar(
                title = appBarState.title,
                showBackButton = appBarState.showBackButton,
                onBack = appBarState.onBack
            )
        },
        bottomBar = {
            if (!hideBars) {
                BottomBar(navController)
            }
        },
        floatingActionButton = {
            if (!hideBars) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddTransaction.route) },
                    modifier = Modifier.size(60.dp).offset(y = 60.dp),
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                }
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
                // Update app bar
                LaunchedEffect(Unit) {
                    appBarState = AppBarState(title = "Home")
                }
                IncomeTrackerContent(
                    onCardClick = { accountId, isIncome ->
                        navController.navigate(Screen.TransactionList.createRoute(accountId, isIncome))
                    }
                )

            }

            composable(Screen.AddTransaction.route) {
                LaunchedEffect(Unit) {
                    appBarState = AppBarState(
                        title = "Add Transaction",
                        showBackButton = true,
                        onBack = { navController.popBackStack() }
                    )
                }
                AddTransactionScreen()
            }

            composable(Screen.Statistics.route) {
                LaunchedEffect(Unit) {
                    appBarState = AppBarState(title = "Statistics")
                }
                StatisticsScreen()
            }

            composable(Screen.Budget.route) {
                LaunchedEffect(Unit) {
                    appBarState = AppBarState(title = "Budget")
                }
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
                val budgetId = backStackEntry.arguments?.getInt("budgetId")
                LaunchedEffect(Unit) {
                    appBarState = AppBarState(
                        title = if (budgetId == null) "Add Budget" else "Edit Budget",
                        showBackButton = true,
                        onBack = { navController.popBackStack() }
                    )
                }
                BudgetDetailScreen(
                    budgetId = budgetId,
                    onSave = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Login.route) {
                LaunchedEffect(Unit) {
                    appBarState = AppBarState(title = "Login")
                }
                LoginScreen(
                    onLoginSuccess = { user ->
                        // Navigate to Home once logged in
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = "transaction_list/{accountId}?isIncome={isIncome}",
                arguments = listOf(
                    navArgument("accountId") { type = NavType.IntType },
                    navArgument("isIncome") {
                        type = NavType.StringType
                        defaultValue = null
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val accountId = backStackEntry.arguments?.getInt("accountId") ?: return@composable
                val isIncomeStr = backStackEntry.arguments?.getString("isIncome")
                val isIncome: Boolean? = isIncomeStr?.toBooleanStrictOrNull()

                LaunchedEffect(Unit) {
                    appBarState = AppBarState(
                        title = when (isIncome) {
                            true -> "Income Transactions"
                            false -> "Expense Transactions"
                            null -> "All Transactions"
                        },
                        showBackButton = true,
                        onBack = { navController.popBackStack() }
                    )
                }

                TransactionListScreen(accountId = accountId, isIncome = isIncome)
            }

        }
    }
}
