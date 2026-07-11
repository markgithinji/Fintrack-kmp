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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
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
import com.fintrack.shared.feature.transaction.ui.home.HomeScreen
import com.fintrack.shared.feature.transaction.ui.transactionlist.TransactionListScreen
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.shared.feature.category.ui.CategoryManagementScreen

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
    
    val startDestination: Any = remember(isAuthenticated) {
        if (isAuthenticated) Screen.Home else Screen.Login 
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
                            navController.navigate(Screen.AddTransaction(transactionId))
                        },
                        onCardClick = { accountId, isIncome ->
                            navController.navigate(
                                Screen.TransactionList(
                                    accountId = accountId,
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
                        onBack = { navController.popBackStack() },
                        onUpdateAppBarState = onUpdateAppBarState
                    )
                }

                composable<Screen.Statistics> { backStackEntry ->
                    LaunchedEffect(backStackEntry.lifecycle.currentState) {
                        if (backStackEntry.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                            onUpdateAppBarState(AppBarState(title = "Statistics"))
                        }
                    }
                    StatisticsScreen(
                        selectedAccountId = selectedAccountId,
                        paddingValues = paddingValues,
                        animatedVisibilityScope = this,
                        onCategoryClick = { category, isIncome, startDate, endDate, accountId ->
                            val isTransactionCost = category == "Transaction Fees"
                            navController.navigate(
                                Screen.TransactionList(
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

                composable<Screen.Budget> { backStackEntry ->
                    LaunchedEffect(backStackEntry.lifecycle.currentState) {
                        if (backStackEntry.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                            onUpdateAppBarState(AppBarState(title = "Budget"))
                        }
                    }
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

                composable<Screen.Profile> { backStackEntry ->
                    LaunchedEffect(backStackEntry.lifecycle.currentState) {
                        if (backStackEntry.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                            onUpdateAppBarState(AppBarState(title = "Profile"))
                        }
                    }
                    ProfileScreen(
                        paddingValues = paddingValues,
                        onNavigateToAccounts = { navController.navigate(Screen.Accounts) },
                        onNavigateToCategories = { navController.navigate(Screen.Categories) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings) },
                        onNavigateToEditProfile = { navController.navigate(Screen.EditProfile) },
                        onLogout = onLogout
                    )
                }

                composable<Screen.EditProfile> { backStackEntry ->
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

                composable<Screen.Accounts> { backStackEntry ->
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

                composable<Screen.Categories> { backStackEntry ->
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

                composable<Screen.Settings> { backStackEntry ->
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

                composable<Screen.BudgetDetail> { backStackEntry ->
                    val route: Screen.BudgetDetail = backStackEntry.toRoute()
                    val budgetId = route.budgetId

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

                composable<Screen.Login> { backStackEntry ->
                    LaunchedEffect(backStackEntry.lifecycle.currentState) {
                        if (backStackEntry.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                            onUpdateAppBarState(AppBarState(title = "Login"))
                        }
                    }
                    LoginScreen(
                        viewModel = authViewModel,
                        onLoginSuccess = {
                            navController.navigate(Screen.Home) {
                                popUpTo(Screen.Login) { inclusive = true }
                            }
                        },
                        onSignUp = {
                            navController.navigate(Screen.Register) {
                                popUpTo(Screen.Login) { inclusive = true }
                            }
                        },
                        onForgotPassword = {
                        }
                    )
                }

                composable<Screen.Register> { backStackEntry ->
                    LaunchedEffect(backStackEntry.lifecycle.currentState) {
                        if (backStackEntry.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                            onUpdateAppBarState(
                                AppBarState(
                                    title = "Create Account",
                                    showBackButton = true,
                                    onBack = {
                                        navController.navigate(Screen.Login) {
                                            popUpTo(Screen.Register) { inclusive = true }
                                        }
                                    }
                                )
                            )
                        }
                    }
                    RegisterScreen(
                        viewModel = authViewModel,
                        onRegisterSuccess = {
                            navController.navigate(Screen.Home) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onLogin = {
                            navController.navigate(Screen.Login) {
                                popUpTo(Screen.Register) { inclusive = true }
                            }
                        }
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
                            navController.navigate(Screen.AddTransaction(transactionId))
                        }
                    )
                }
            }
        }
    }
}
