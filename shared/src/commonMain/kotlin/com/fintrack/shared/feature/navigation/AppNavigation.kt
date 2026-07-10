package com.fintrack.shared.feature.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.savedstate.read
import com.fintrack.shared.feature.auth.ui.AuthViewModel
import com.fintrack.shared.feature.auth.ui.LoginScreen
import com.fintrack.shared.feature.auth.ui.RegisterScreen
import com.fintrack.shared.feature.budget.ui.BudgetDetailScreen
import com.fintrack.shared.feature.budget.ui.BudgetScreen
import com.fintrack.shared.feature.account.ui.AccountsScreen
import com.fintrack.shared.feature.profile.EditProfileScreen
import com.fintrack.shared.feature.profile.ProfileScreen
import com.fintrack.shared.feature.settings.ui.SettingsScreen
import com.fintrack.shared.feature.summary.ui.StatisticsScreen
import com.fintrack.shared.feature.transaction.ui.addtransaction.AddTransactionScreen
import com.fintrack.shared.feature.transaction.ui.category.CategoryManagementScreen
import com.fintrack.shared.feature.transaction.ui.home.HomeScreen
import com.fintrack.shared.feature.transaction.ui.transactionlist.TransactionListScreen
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
    mainViewModel: MainViewModel = koinViewModel(),
    onLogout: () -> Unit = {},
) {
    val selectedAccountId by mainViewModel.selectedAccountId.collectAsStateWithLifecycle()

    println("LOGIN_DEBUG: AppNavigation recomposing. isAuthenticated: $isAuthenticated")
    
    val startDestination = remember(isAuthenticated) {
        if (isAuthenticated) Screen.Home.route else Screen.Login.route 
    }

    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    val isToAuth = (targetState.destination.route == Screen.Login.route || 
                                 targetState.destination.route == Screen.Register.route)
                    val isFromAuth = (initialState.destination.route == Screen.Login.route || 
                                   initialState.destination.route == Screen.Register.route)
                    
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
                        slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(500, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(500))
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
                        slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(500, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(400))
                    } else {
                        ExitTransition.None
                    }
                },
                popEnterTransition = { 
                    val isToMorphScreen = targetState.destination.route?.contains("budget_detail") == true ||
                                         targetState.destination.route?.contains("transaction_list") == true ||
                                         targetState.destination.route?.contains("add_transaction") == true
                    
                    if (isToMorphScreen) {
                        fadeIn(animationSpec = tween(400))
                    } else {
                        EnterTransition.None
                    }
                },
                popExitTransition = {
                    val isFromMorphScreen = initialState.destination.route?.contains("budget_detail") == true ||
                                           initialState.destination.route?.contains("transaction_list") == true ||
                                           initialState.destination.route?.contains("add_transaction") == true
                    if (isFromMorphScreen) {
                        slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(500, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(400))
                    } else {
                        ExitTransition.None
                    }
                }
            ) {
                composable(Screen.Home.route) { backStackEntry ->
                    LaunchedEffect(backStackEntry.lifecycle.currentState) {
                        if (backStackEntry.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                            onUpdateAppBarState(AppBarState(title = "Home"))
                        }
                    }
                    HomeScreen(
                        selectedAccountId = selectedAccountId,
                        onAccountSelected = { mainViewModel.onAccountSelected(it) },
                        paddingValues = paddingValues,
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
                    val transactionId = backStackEntry.arguments?.read {
                        if (contains("transactionId")) getString("transactionId") else null
                    }

                    AddTransactionScreen(
                        transactionId = transactionId,
                        paddingValues = paddingValues,
                        animatedVisibilityScope = this,
                        onBack = { navController.popBackStack() },
                        onUpdateAppBarState = onUpdateAppBarState
                    )
                }

                composable(Screen.Statistics.route) { backStackEntry ->
                    LaunchedEffect(backStackEntry.lifecycle.currentState) {
                        if (backStackEntry.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                            onUpdateAppBarState(AppBarState(title = "Statistics"))
                        }
                    }
                    StatisticsScreen(
                        paddingValues = paddingValues,
                        animatedVisibilityScope = this,
                        onCategoryClick = { category, isIncome, startDate, endDate, accountId ->
                            val isTransactionCost = category == "Transaction Fees"
                            navController.navigate(
                                Screen.TransactionList.createRoute(
                                    accountId = accountId,
                                    isIncome = if (isTransactionCost) null else isIncome,
                                    category = if (isTransactionCost) null else category,
                                    startDate = startDate,
                                    endDate = endDate,
                                    hasTransactionCost = if (isTransactionCost) true else null
                                )
                            )
                        }
                    )
                }

                composable(Screen.Budget.route) { backStackEntry ->
                    LaunchedEffect(backStackEntry.lifecycle.currentState) {
                        if (backStackEntry.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                            onUpdateAppBarState(AppBarState(title = "Budget"))
                        }
                    }
                    BudgetScreen(
                        paddingValues = paddingValues,
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

                composable(Screen.Profile.route) { backStackEntry ->
                    LaunchedEffect(backStackEntry.lifecycle.currentState) {
                        if (backStackEntry.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                            onUpdateAppBarState(AppBarState(title = "Profile"))
                        }
                    }
                    ProfileScreen(
                        paddingValues = paddingValues,
                        onNavigateToAccounts = { navController.navigate(Screen.Accounts.route) },
                        onNavigateToCategories = { navController.navigate(Screen.Categories.route) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                        onLogout = onLogout
                    )
                }

                composable(Screen.EditProfile.route) { backStackEntry ->
                    LaunchedEffect(backStackEntry.lifecycle.currentState) {
                        if (backStackEntry.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                            onUpdateAppBarState(
                                AppBarState(
                                    title = "Edit Profile",
                                    showBackButton = true,
                                    onBack = { navController.popBackStack() }
                                )
                            )
                        }
                    }
                    EditProfileScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Accounts.route) { backStackEntry ->
                    LaunchedEffect(backStackEntry.lifecycle.currentState) {
                        if (backStackEntry.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                            onUpdateAppBarState(
                                AppBarState(
                                    title = "Accounts",
                                    showBackButton = true,
                                    onBack = { navController.popBackStack() }
                                )
                            )
                        }
                    }
                    AccountsScreen(paddingValues = paddingValues)
                }

                composable(Screen.Categories.route) { backStackEntry ->
                    LaunchedEffect(backStackEntry.lifecycle.currentState) {
                        if (backStackEntry.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                            onUpdateAppBarState(
                                AppBarState(
                                    title = "Categories",
                                    showBackButton = true,
                                    onBack = { navController.popBackStack() }
                                )
                            )
                        }
                    }
                    CategoryManagementScreen(
                        paddingValues = paddingValues,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Settings.route) { backStackEntry ->
                    LaunchedEffect(backStackEntry.lifecycle.currentState) {
                        if (backStackEntry.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                            onUpdateAppBarState(
                                AppBarState(
                                    title = "Settings",
                                    showBackButton = true,
                                    onBack = { navController.popBackStack() }
                                )
                            )
                        }
                    }
                    SettingsScreen(
                        paddingValues = paddingValues
                    )
                }

                composable(
                    route = Screen.BudgetDetail.route,
                    arguments = listOf(
                        navArgument("budgetId") {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val budgetIdArg = backStackEntry.arguments?.read {
                        if (contains("budgetId")) getString("budgetId") else null
                    }
                    val budgetId = if (budgetIdArg.isNullOrEmpty()) null else budgetIdArg

                    LaunchedEffect(budgetId, backStackEntry.lifecycle.currentState) {
                        if (backStackEntry.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                            onUpdateAppBarState(
                                AppBarState(
                                    title = if (budgetId == null) "Add Budget" else "Edit Budget",
                                    showBackButton = true,
                                    onBack = { navController.popBackStack() }
                                )
                            )
                        }
                    }

                    BudgetDetailScreen(
                        budgetId = budgetId,
                        paddingValues = paddingValues,
                        animatedVisibilityScope = this,
                        onSave = { navController.popBackStack() },
                        onBack = { navController.popBackStack() },
                        onUpdateAppBarState = onUpdateAppBarState
                    )
                }

                composable(Screen.Login.route) { backStackEntry ->
                    LaunchedEffect(backStackEntry.lifecycle.currentState) {
                        if (backStackEntry.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                            onUpdateAppBarState(AppBarState(title = "Login"))
                        }
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

                composable(Screen.Register.route) { backStackEntry ->
                    LaunchedEffect(backStackEntry.lifecycle.currentState) {
                        if (backStackEntry.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
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
                    route = Screen.TransactionList.route,
                    arguments = listOf(
                        navArgument("accountId") {
                            type = NavType.StringType
                            defaultValue = null
                            nullable = true
                        },
                        navArgument("isIncome") {
                            type = NavType.StringType
                            defaultValue = null
                            nullable = true
                        },
                        navArgument("category") {
                            type = NavType.StringType
                            defaultValue = null
                            nullable = true
                        },
                        navArgument("startDate") {
                            type = NavType.StringType
                            defaultValue = null
                            nullable = true
                        },
                        navArgument("endDate") {
                            type = NavType.StringType
                            defaultValue = null
                            nullable = true
                        },
                        navArgument("hasTransactionCost") {
                            type = NavType.StringType
                            defaultValue = null
                            nullable = true
                        }
                    )
                ) { backStackEntry ->
                    val accountId = backStackEntry.arguments?.read {
                        if (contains("accountId")) getString("accountId") else null
                    }
                    val isIncomeStr = backStackEntry.arguments?.read {
                        if (contains("isIncome")) getString("isIncome") else null
                    }
                    val isIncome: Boolean? = isIncomeStr?.toBooleanStrictOrNull()
                    
                    val category = backStackEntry.arguments?.read {
                        if (contains("category")) getString("category") else null
                    }
                    val startDate = backStackEntry.arguments?.read {
                        if (contains("startDate")) getString("startDate") else null
                    }
                    val endDate = backStackEntry.arguments?.read {
                        if (contains("endDate")) getString("endDate") else null
                    }
                    val hasTransactionCostStr = backStackEntry.arguments?.read {
                        if (contains("hasTransactionCost")) getString("hasTransactionCost") else null
                    }
                    val hasTransactionCost: Boolean? = hasTransactionCostStr?.toBooleanStrictOrNull()

                    LaunchedEffect(backStackEntry.lifecycle.currentState) {
                        if (backStackEntry.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                            onUpdateAppBarState(
                                AppBarState(
                                    title = when {
                                        hasTransactionCost == true -> "Transaction Fees"
                                        category?.contains(",") == true -> "Other Categories"
                                        category != null -> category
                                        isIncome == true -> "Income Transactions"
                                        isIncome == false -> "Expense Transactions"
                                        else -> "All Transactions"
                                    },
                                    showBackButton = true,
                                    onBack = { navController.popBackStack() }
                                )
                            )
                        }
                    }

                    TransactionListScreen(
                        accountId = accountId, 
                        isIncome = isIncome,
                        category = category,
                        startDate = startDate,
                        endDate = endDate,
                        hasTransactionCost = hasTransactionCost,
                        paddingValues = paddingValues,
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