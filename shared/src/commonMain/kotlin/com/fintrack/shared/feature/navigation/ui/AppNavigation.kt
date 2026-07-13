package com.fintrack.shared.feature.navigation.ui

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
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.fintrack.shared.feature.auth.ui.AuthViewModel
import com.fintrack.shared.feature.auth.ui.LoginScreen
import com.fintrack.shared.feature.auth.ui.RegisterScreen
import com.fintrack.shared.feature.budget.ui.BudgetDetailScreen
import com.fintrack.shared.feature.budget.ui.BudgetScreen
import com.fintrack.shared.feature.account.ui.AccountsScreen
import com.fintrack.shared.feature.user.ui.EditProfileScreen
import com.fintrack.shared.feature.user.ui.ProfileScreen
import com.fintrack.shared.feature.settings.ui.SettingsScreen
import com.fintrack.shared.feature.summary.ui.StatisticsScreen
import com.fintrack.shared.feature.transaction.ui.addtransaction.AddTransactionScreen
import com.fintrack.shared.feature.transaction.ui.home.HomeScreen
import com.fintrack.shared.feature.transaction.ui.transactionlist.TransactionListScreen
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fintrack.shared.feature.category.ui.CategoryManagementScreen
import com.fintrack.shared.feature.navigation.model.Screen

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    isAuthenticated: Boolean,
    paddingValues: PaddingValues,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel = koinViewModel(),
    onLogout: () -> Unit = {},
) {
    val navController = LocalNavController.current
    val selectedAccountId by mainViewModel.selectedAccountId.collectAsStateWithLifecycle()

    val startDestination: Any = remember(isAuthenticated) {
        if (isAuthenticated) Screen.Home() else Screen.Login
    }

    // Navigation Guard: Kick user to Login if session expires or unauthorized access
    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) {
            val currentDestination = navController.currentBackStackEntry?.destination
            val isAuthRoute = currentDestination?.hasRoute<Screen.Login>() == true ||
                    currentDestination?.hasRoute<Screen.Register>() == true

            if (!isAuthRoute) {
                navController.navigate(Screen.Login) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    // Navigation Guard: Prevent navigation to protected routes when not authenticated
    DisposableEffect(navController, isAuthenticated) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            val isAuthRoute = destination.hasRoute<Screen.Login>() || destination.hasRoute<Screen.Register>()
            if (!isAuthRoute && !isAuthenticated) {
                navController.navigate(Screen.Login) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    val isToAuth = (targetState.destination.hasRoute<Screen.Login>() || 
                                 targetState.destination.hasRoute<Screen.Register>())
                    val isFromAuth = (initialState.destination.hasRoute<Screen.Login>() || 
                                   initialState.destination.hasRoute<Screen.Register>())
                    
                    val isToMorphScreen = targetState.destination.hasRoute<Screen.BudgetDetail>() ||
                                         targetState.destination.hasRoute<Screen.TransactionList>() ||
                                         targetState.destination.hasRoute<Screen.AddTransaction>()

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
                    val isToAuth = targetState.destination.hasRoute<Screen.Login>() || 
                                 targetState.destination.hasRoute<Screen.Register>()
                    val isFromAuth = initialState.destination.hasRoute<Screen.Login>() || 
                                   initialState.destination.hasRoute<Screen.Register>()
                    
                    val isFromMorphScreen = initialState.destination.hasRoute<Screen.BudgetDetail>() ||
                                           initialState.destination.hasRoute<Screen.TransactionList>() ||
                                           initialState.destination.hasRoute<Screen.AddTransaction>()

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
                    val isToMorphScreen = targetState.destination.hasRoute<Screen.BudgetDetail>() ||
                                         targetState.destination.hasRoute<Screen.TransactionList>() ||
                                         targetState.destination.hasRoute<Screen.AddTransaction>()
                    
                    if (isToMorphScreen) {
                        fadeIn(animationSpec = tween(400))
                    } else {
                        EnterTransition.None
                    }
                },
                popExitTransition = {
                    val isFromMorphScreen = initialState.destination.hasRoute<Screen.BudgetDetail>() ||
                                           initialState.destination.hasRoute<Screen.TransactionList>() ||
                                           initialState.destination.hasRoute<Screen.AddTransaction>()
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
                composable<Screen.Home> { backStackEntry ->
                    val route: Screen.Home = backStackEntry.toRoute()
                    val accountId = route.accountId ?: selectedAccountId

                    HomeScreen(
                        selectedAccountId = accountId,
                        onAccountSelected = { mainViewModel.onAccountSelected(it) },
                        paddingValues = paddingValues,
                        animatedVisibilityScope = this,
                        onEditTransaction = { transactionId ->
                            navController.navigate(Screen.AddTransaction(transactionId))
                        },
                        onCardClick = { accountIdParam, isIncome ->
                            navController.navigate(
                                Screen.TransactionList(
                                    accountId = accountIdParam ?: accountId ?: "",
                                    isIncome = isIncome
                                )
                            )
                        }
                    )
                }

                composable<Screen.AddTransaction> { backStackEntry ->
                    val route: Screen.AddTransaction = backStackEntry.toRoute()
                    val transactionId = route.transactionId

                    AddTransactionScreen(
                        transactionId = transactionId,
                        paddingValues = paddingValues,
                        animatedVisibilityScope = this,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable<Screen.Statistics> { backStackEntry ->
                    val route: Screen.Statistics = backStackEntry.toRoute()
                    val accountId = route.accountId ?: selectedAccountId

                    StatisticsScreen(
                        selectedAccountId = accountId,
                        paddingValues = paddingValues,
                        animatedVisibilityScope = this,
                        onCategoryClick = { category, isIncome, startDate, endDate, accountIdParam ->
                            val isTransactionCost = category == "Transaction Fees"
                            navController.navigate(
                                Screen.TransactionList(
                                    accountId = accountIdParam ?: accountId ?: "",
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

                composable<Screen.Budget> { 
                    BudgetScreen(
                        paddingValues = paddingValues,
                        animatedVisibilityScope = this,
                        onAddBudget = {
                            navController.navigate(Screen.BudgetDetail(null))
                        },
                        onBudgetClick = { budgetWithStatus ->
                            navController.navigate(
                                Screen.BudgetDetail(
                                    budgetWithStatus.budget.id
                                )
                            )
                        }
                    )
                }

                composable<Screen.Profile> { 
                    ProfileScreen(
                        paddingValues = paddingValues,
                        onNavigateToAccounts = { navController.navigate(Screen.Accounts) },
                        onNavigateToCategories = { navController.navigate(Screen.Categories) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings) },
                        onNavigateToEditProfile = { navController.navigate(Screen.EditProfile) },
                        onLogout = onLogout
                    )
                }

                composable<Screen.EditProfile> { 
                    EditProfileScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<Screen.Accounts> { 
                    AccountsScreen(paddingValues = paddingValues)
                }

                composable<Screen.Categories> { 
                    CategoryManagementScreen(
                        paddingValues = paddingValues,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<Screen.Settings> { 
                    SettingsScreen(
                        paddingValues = paddingValues
                    )
                }

                composable<Screen.BudgetDetail> { backStackEntry ->
                    val route: Screen.BudgetDetail = backStackEntry.toRoute()
                    val budgetId = route.budgetId

                    BudgetDetailScreen(
                        budgetId = budgetId,
                        paddingValues = paddingValues,
                        animatedVisibilityScope = this,
                        onSave = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable<Screen.Login> { 
                    LoginScreen(
                        viewModel = authViewModel,
                        onLoginSuccess = {
                            // Handled by AuthOrchestrator and AppNavigation startDestination logic
                        },
                        onSignUp = {
                            navController.navigate(Screen.Register) {
                                popUpTo(Screen.Login) { inclusive = true }
                            }
                        },
                        onForgotPassword = {
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }

                composable<Screen.Register> { 
                    RegisterScreen(
                        viewModel = authViewModel,
                        onRegisterSuccess = {
                            // Handled by AuthOrchestrator and AppNavigation startDestination logic
                        },
                        onLogin = {
                            navController.navigate(Screen.Login) {
                                popUpTo(Screen.Register) { inclusive = true }
                            }
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }

                composable<Screen.TransactionList> { backStackEntry ->
                    val route: Screen.TransactionList = backStackEntry.toRoute()
                    val accountId = route.accountId
                    val isIncome = route.isIncome
                    val category = route.category
                    val startDate = route.startDate
                    val endDate = route.endDate
                    val hasTransactionCost = route.hasTransactionCost

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
                            navController.navigate(Screen.AddTransaction(transactionId))
                        }
                    )
                }
            }
        }
    }
}
