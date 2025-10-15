package com.fintrack.shared.feature.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fintrack.shared.feature.auth.ui.AuthViewModel
import com.fintrack.shared.feature.auth.ui.LoginScreen
import com.fintrack.shared.feature.budget.ui.BudgetDetailScreen
import com.fintrack.shared.feature.budget.ui.BudgetScreen
import com.fintrack.shared.feature.profile.AccountsScreen
import com.fintrack.shared.feature.profile.CategoriesScreen
import com.fintrack.shared.feature.profile.ProfileScreen
import com.fintrack.shared.feature.profile.SettingsScreen
import com.fintrack.shared.feature.summary.ui.StatisticsScreen
import com.fintrack.shared.feature.transaction.ui.addtransaction.AddTransactionScreen
import com.fintrack.shared.feature.transaction.ui.home.IncomeTrackerContent
import com.fintrack.shared.feature.transaction.ui.transactionlist.TransactionListScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavigation(
    isAuthenticated: Boolean,
    navController: NavHostController,
    paddingValues: PaddingValues,
    onUpdateAppBarState: (AppBarState) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) Screen.Home.route else Screen.Login.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        // Home Screen
        composable(Screen.Home.route) {
            LaunchedEffect(Unit) {
                onUpdateAppBarState(AppBarState(title = "Home"))
            }
            IncomeTrackerContent(
                onCardClick = { accountId, isIncome ->
                    navController.navigate(
                        Screen.TransactionList.createRoute(
                            accountId,
                            isIncome
                        )
                    )
                }
            )
        }

        // Add Transaction Screen
        composable(Screen.AddTransaction.route) {
            LaunchedEffect(Unit) {
                onUpdateAppBarState(
                    AppBarState(
                        title = "Add Transaction",
                        showBackButton = true,
                        onBack = { navController.popBackStack() }
                    )
                )
            }
            AddTransactionScreen()
        }

        // Statistics Screen
        composable(Screen.Statistics.route) {
            LaunchedEffect(Unit) {
                onUpdateAppBarState(AppBarState(title = "Statistics"))
            }
            StatisticsScreen()
        }

        // Budget Screen
        composable(Screen.Budget.route) {
            LaunchedEffect(Unit) {
                onUpdateAppBarState(AppBarState(title = "Budget"))
            }
            BudgetScreen(
                onAddBudget = {
                    navController.navigate(Screen.BudgetDetail.createRoute(null))
                },
                onBudgetClick = { budgetWithStatus ->
                    navController.navigate(
                        Screen.BudgetDetail.createRoute(
                            budgetWithStatus.budget.id,
                            budgetWithStatus.budget.accountId
                        )
                    )
                }
            )
        }

        // Profile Screen
        composable(Screen.Profile.route) {
            LaunchedEffect(Unit) {
                onUpdateAppBarState(AppBarState(title = "Profile"))
            }
            ProfileScreen(
                onNavigateToAccounts = { navController.navigate(Screen.Accounts.route) },
                onNavigateToCategories = { navController.navigate(Screen.Categories.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToBudgets = { navController.navigate(Screen.Budget.route) }
            )
        }

        // Accounts Screen
        composable(Screen.Accounts.route) {
            LaunchedEffect(Unit) {
                onUpdateAppBarState(
                    AppBarState(
                        title = "Accounts",
                        showBackButton = true,
                        onBack = { navController.popBackStack() }
                    )
                )
            }
            AccountsScreen()
        }

        // Categories Screen
        composable(Screen.Categories.route) {
            LaunchedEffect(Unit) {
                onUpdateAppBarState(
                    AppBarState(
                        title = "Categories",
                        showBackButton = true,
                        onBack = { navController.popBackStack() }
                    )
                )
            }
            CategoriesScreen()
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            LaunchedEffect(Unit) {
                onUpdateAppBarState(
                    AppBarState(
                        title = "Settings",
                        showBackButton = true,
                        onBack = { navController.popBackStack() }
                    )
                )
            }
            SettingsScreen()
        }

        // Budget Detail Screen
        composable(
            route = Screen.BudgetDetail.route,
            arguments = listOf(
                navArgument("budgetId") { type = NavType.StringType },
                navArgument("accountId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val budgetIdArg = backStackEntry.arguments?.getString("budgetId")
            val accountIdArg = backStackEntry.arguments?.getString("accountId")

            val budgetId = if (budgetIdArg.isNullOrEmpty()) null else budgetIdArg
            val accountId = if (accountIdArg.isNullOrEmpty()) null else accountIdArg

            LaunchedEffect(budgetId) {
                onUpdateAppBarState(
                    AppBarState(
                        title = if (budgetId == null) "Add Budget" else "Edit Budget",
                        showBackButton = true,
                        onBack = { navController.popBackStack() }
                    )
                )
            }

            BudgetDetailScreen(
                budgetId = budgetId,
                accountId = accountId,
                onSave = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // Login Screen
        composable(Screen.Login.route) {
            LaunchedEffect(Unit) {
                onUpdateAppBarState(AppBarState(title = "Login"))
            }
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Transaction List Screen
        composable(
            route = "transaction_list/{accountId}?isIncome={isIncome}",
            arguments = listOf(
                navArgument("accountId") { type = NavType.StringType },
                navArgument("isIncome") {
                    type = NavType.StringType
                    defaultValue = null
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: return@composable
            val isIncomeStr = backStackEntry.arguments?.getString("isIncome")
            val isIncome: Boolean? = isIncomeStr?.toBooleanStrictOrNull()

            LaunchedEffect(Unit) {
                onUpdateAppBarState(
                    AppBarState(
                        title = when (isIncome) {
                            true -> "Income Transactions"
                            false -> "Expense Transactions"
                            null -> "All Transactions"
                        },
                        showBackButton = true,
                        onBack = { navController.popBackStack() }
                    )
                )
            }

            TransactionListScreen(accountId = accountId, isIncome = isIncome)
        }
    }
}