package com.fintrack.shared.feature.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fintrack.shared.feature.auth.ui.AuthViewModel
import com.fintrack.shared.feature.auth.ui.LoginScreen
import com.fintrack.shared.feature.auth.ui.RegisterScreen
import com.fintrack.shared.feature.budget.ui.BudgetDetailScreen
import com.fintrack.shared.feature.budget.ui.BudgetScreen
import com.fintrack.shared.feature.profile.AccountsScreen
import com.fintrack.shared.feature.profile.CategoriesScreen
import com.fintrack.shared.feature.profile.ProfileScreen
import com.fintrack.shared.feature.profile.SettingsScreen
import com.fintrack.shared.feature.summary.ui.StatisticsScreen
import com.fintrack.shared.feature.transaction.ui.addtransaction.AddTransactionScreen
import com.fintrack.shared.feature.transaction.ui.home.HomeScreen
import com.fintrack.shared.feature.transaction.ui.transactionlist.TransactionListScreen

@Composable
fun AppNavigation(
    isAuthenticated: Boolean,
    navController: NavHostController,
    paddingValues: PaddingValues,
    onUpdateAppBarState: (AppBarState) -> Unit,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit = {}
) {
    println("LOGIN_DEBUG: AppNavigation recomposing. isAuthenticated: $isAuthenticated")
    
    // We remember the start destination when the NavHost is first created or when authStatus changes 
    // significantly (e.g. from Loading to Success). This prevents the NavHost from resetting its 
    // backstack when isAuthenticated changes during a login/logout transition, 
    // allowing the UI to handle the transition (e.g. success animation) before navigating.
    val startDestination = remember { 
        if (isAuthenticated) Screen.Home.route else Screen.Login.route 
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(paddingValues),
        enterTransition = {
            val isFromAuth = initialState.destination.route == Screen.Login.route || 
                           initialState.destination.route == Screen.Register.route
            val isToHome = targetState.destination.route == Screen.Home.route
            val isToLogin = targetState.destination.route == Screen.Login.route

            if (isFromAuth && isToHome) {
                // Fancy scale + fade when entering app
                scaleIn(initialScale = 0.9f, animationSpec = tween(600)) + 
                fadeIn(animationSpec = tween(600))
            } else if (isToLogin) {
                // Smooth fade when logging out
                fadeIn(animationSpec = tween(600))
            } else {
                // Default slide for internal navigation
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400))
            }
        },
        exitTransition = {
            val isFromAuth = initialState.destination.route == Screen.Login.route || 
                           initialState.destination.route == Screen.Register.route
            val isToHome = targetState.destination.route == Screen.Home.route
            val isToLogin = targetState.destination.route == Screen.Login.route

            if (isFromAuth && isToHome) {
                // Login screen fades and scales out slightly
                scaleOut(targetScale = 1.1f, animationSpec = tween(600)) + 
                fadeOut(animationSpec = tween(600))
            } else if (isToLogin) {
                // Current screen scales down and fades out during logout
                scaleOut(targetScale = 0.9f, animationSpec = tween(600)) + 
                fadeOut(animationSpec = tween(600))
            } else {
                // Default slide out
                slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400))
            }
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(400))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400))
        }
    ) {
        // Home Screen
        composable(Screen.Home.route) {
            LaunchedEffect(Unit) {
                onUpdateAppBarState(AppBarState(title = "Home"))
            }
            HomeScreen(
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
            AddTransactionScreen(
                onBack = { navController.popBackStack() }
            )
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
                            budgetWithStatus.budget.id
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
                onNavigateToBudgets = { navController.navigate(Screen.Budget.route) },
                onLogout = onLogout
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
                navArgument("budgetId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val budgetIdArg = backStackEntry.arguments?.getString("budgetId")
            val budgetId = if (budgetIdArg.isNullOrEmpty()) null else budgetIdArg

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
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        // Clear the entire back stack including login
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onSignUp = {
                    // Replace login with register (no back stack buildup)
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onForgotPassword = {
                    // Handle forgot password
                }
            )
        }

        // Register Screen
        composable(Screen.Register.route) {
            LaunchedEffect(Unit) {
                onUpdateAppBarState(
                    AppBarState(
                        title = "Create Account",
                        showBackButton = true,
                        onBack = {
                            // Go back to login, but clear any existing login instances
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        }
                    )
                )
            }
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        // Clear the entire back stack
                        popUpTo(0) { inclusive = true }
                    }
                },
                onLogin = {
                    // Go back to login, but clear any existing login instances
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
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