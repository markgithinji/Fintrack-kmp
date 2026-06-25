package com.fintrack.shared.feature.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
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

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
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
    
    val startDestination = remember { 
        if (isAuthenticated) Screen.Home.route else Screen.Login.route 
    }

    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(paddingValues),
                enterTransition = {
                    val isToAuth = targetState.destination.route == Screen.Login.route || 
                                 targetState.destination.route == Screen.Register.route
                    val isFromAuth = initialState.destination.route == Screen.Login.route || 
                                   initialState.destination.route == Screen.Register.route
                    
                    val isToMorphScreen = targetState.destination.route?.contains("budget_detail") == true ||
                                         targetState.destination.route?.contains("transaction_list") == true ||
                                         targetState.destination.route?.contains("add_transaction") == true

                    if (isFromAuth && !isToAuth) { // Login success
                        scaleIn(initialScale = 0.9f, animationSpec = tween(600)) + fadeIn(animationSpec = tween(600))
                    } else if (isToAuth && !isFromAuth) { // Logout
                        fadeIn(animationSpec = tween(600))
                    } else if (isToAuth && isFromAuth) { // Between Login/Register
                        fadeIn(animationSpec = tween(400))
                    } else if (isToMorphScreen) {
                        fadeIn(animationSpec = tween(500))
                    } else {
                        EnterTransition.None
                    }
                },
                exitTransition = {
                    val isToAuth = targetState.destination.route == Screen.Login.route || 
                                 targetState.destination.route == Screen.Register.route
                    val isFromAuth = initialState.destination.route == Screen.Login.route || 
                                   initialState.destination.route == Screen.Register.route
                    
                    val isFromMorphScreen = initialState.destination.route?.contains("budget_detail") == true ||
                                           initialState.destination.route?.contains("transaction_list") == true ||
                                           initialState.destination.route?.contains("add_transaction") == true

                    if (isFromAuth && !isToAuth) { // Login success
                        scaleOut(targetScale = 1.1f, animationSpec = tween(600)) + fadeOut(animationSpec = tween(600))
                    } else if (isToAuth && !isFromAuth) { // Logout
                        scaleOut(targetScale = 0.9f, animationSpec = tween(600)) + fadeOut(animationSpec = tween(600))
                    } else if (isToAuth && isFromAuth) { // Between Login/Register
                        fadeOut(animationSpec = tween(400))
                    } else if (isFromMorphScreen) {
                        fadeOut(animationSpec = tween(400))
                    } else {
                        ExitTransition.None
                    }
                },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = {
                    val isFromMorphScreen = initialState.destination.route?.contains("budget_detail") == true ||
                                           initialState.destination.route?.contains("transaction_list") == true ||
                                           initialState.destination.route?.contains("add_transaction") == true
                    if (isFromMorphScreen) {
                        fadeOut(animationSpec = tween(400))
                    } else {
                        ExitTransition.None
                    }
                }
            ) {
                composable(Screen.Home.route) {
                    LaunchedEffect(Unit) {
                        onUpdateAppBarState(AppBarState(title = "Home"))
                    }
                    HomeScreen(
                        animatedVisibilityScope = this,
                        onEditTransaction = { transactionId ->
                            navController.navigate(Screen.AddTransaction.createRoute(transactionId))
                        },
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

                composable(
                    Screen.AddTransaction.route,
                    arguments = listOf(
                        navArgument("transactionId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val transactionId = backStackEntry.arguments?.getString("transactionId")

                    LaunchedEffect(transactionId) {
                        onUpdateAppBarState(
                            AppBarState(
                                title = if (transactionId == null) "Add Transaction" else "Edit Transaction",
                                showBackButton = true,
                                onBack = { navController.popBackStack() }
                            )
                        )
                    }
                    AddTransactionScreen(
                        transactionId = transactionId,
                        animatedVisibilityScope = this,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Statistics.route) {
                    LaunchedEffect(Unit) {
                        onUpdateAppBarState(AppBarState(title = "Statistics"))
                    }
                    StatisticsScreen()
                }

                composable(Screen.Budget.route) {
                    LaunchedEffect(Unit) {
                        onUpdateAppBarState(AppBarState(title = "Budget"))
                    }
                    BudgetScreen(
                        animatedVisibilityScope = this,
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
                        animatedVisibilityScope = this,
                        onSave = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Login.route) {
                    LaunchedEffect(Unit) {
                        onUpdateAppBarState(AppBarState(title = "Login"))
                    }
                    LoginScreen(
                        viewModel = authViewModel,
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onSignUp = {
                            navController.navigate(Screen.Register.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onForgotPassword = {
                        }
                    )
                }

                composable(Screen.Register.route) {
                    LaunchedEffect(Unit) {
                        onUpdateAppBarState(
                            AppBarState(
                                title = "Create Account",
                                showBackButton = true,
                                onBack = {
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
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onLogin = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        }
                    )
                }

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

                    TransactionListScreen(
                        accountId = accountId, 
                        isIncome = isIncome,
                        animatedVisibilityScope = this,
                        onEditTransaction = { transactionId ->
                            navController.navigate(Screen.AddTransaction.createRoute(transactionId))
                        }
                    )
                }
            }
        }
    }
}