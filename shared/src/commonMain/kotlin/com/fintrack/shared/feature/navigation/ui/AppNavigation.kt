package com.fintrack.shared.feature.navigation.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.fintrack.shared.feature.auth.domain.model.AuthState
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
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fintrack.shared.feature.category.ui.CategoryManagementScreen
import com.fintrack.shared.feature.navigation.model.Screen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    isAuthenticated: Boolean,
    paddingValues: PaddingValues,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel = koinInject(),
    onLogout: () -> Unit = {},
) {
    val navController = LocalNavController.current
    val selectedAccountId by mainViewModel.selectedAccountId.collectAsStateWithLifecycle()
    val refreshTrigger by mainViewModel.refreshTrigger.collectAsStateWithLifecycle()
    val onGlobalRefresh = remember { { mainViewModel.triggerGlobalRefresh() } }

    val startDestination: Any = remember(isAuthenticated) {
        if (isAuthenticated) Screen.Home() else Screen.Login
    }

    // Navigation Guard: Kick user to Login if session expires or unauthorized access
    LaunchedEffect(isAuthenticated) {
        val currentRoute = navController.currentBackStackEntry?.destination
        val isAuthRoute = currentRoute?.isAuthScreen() == true

        if (!isAuthenticated) {
            if (!isAuthRoute) {
                navController.navigate(Screen.Login) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        } else {
            // If authenticated and still on an auth screen, move to Home
            if (isAuthRoute) {
                navController.navigate(Screen.Home()) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    // Navigation Guard: Prevent navigation to protected routes when not authenticated
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            val isAuthRoute = destination.isAuthScreen()
            val currentStatus = authViewModel.authStatus.value
            val isCurrentlyAuthenticated = (currentStatus as? AuthState.Success)?.data ?: false
            
            if (!isAuthRoute && !isCurrentlyAuthenticated) {
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

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize(),
        enterTransition = {
            val isToAuth = targetState.destination.isAuthScreen()
            val isFromAuth = initialState.destination.isAuthScreen()
            val isToMorphScreen = targetState.destination.isMorphScreen()
            val isToProfileDetail = targetState.destination.isProfileDetailScreen()

            if (isFromAuth && !isToAuth) { // Login success
                scaleIn(
                    initialScale = 0.85f, 
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(500)) +
                slideInVertically(
                    initialOffsetY = { it / 10 }, 
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                )
            } else if (isToAuth && !isFromAuth) { // Logout
                fadeIn(animationSpec = tween(300))
            } else if (isToAuth && isFromAuth) { // Between Login/Register
                fadeIn(animationSpec = tween(250))
            } else if (isToMorphScreen) {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300))
            } else if (isToProfileDetail) {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
            } else {
                EnterTransition.None
            }
        },
        exitTransition = {
            val isToAuth = targetState.destination.isAuthScreen()
            val isFromAuth = initialState.destination.isAuthScreen()
            val isFromMorphScreen = initialState.destination.isMorphScreen()
            val isFromProfileDetail = initialState.destination.isProfileDetailScreen()
            val isToProfileDetail = targetState.destination.isProfileDetailScreen()

            if (isFromAuth && !isToAuth) { // Login success
                scaleOut(targetScale = 1.1f, animationSpec = tween(500)) + fadeOut(animationSpec = tween(500))
            } else if (isToAuth && !isFromAuth) { // Logout
                scaleOut(targetScale = 0.9f, animationSpec = tween(500)) + fadeOut(animationSpec = tween(500))
            } else if (isToAuth && isFromAuth) { // Between Login/Register
                fadeOut(animationSpec = tween(300))
            } else if (isFromMorphScreen) {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300))
            } else if (isFromProfileDetail) {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
            } else if (isToProfileDetail) {
                // When Profile is being covered by a detail screen, slide it slightly left
                slideOutHorizontally(
                    targetOffsetX = { -it / 4 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
            } else {
                ExitTransition.None
            }
        },
        popEnterTransition = { 
            val isToMorphScreen = targetState.destination.isMorphScreen()
            val isToProfileDetail = targetState.destination.isProfileDetailScreen()
            val isToProfile = targetState.destination.hasRoute<Screen.Profile>()

            if (isToMorphScreen || targetState.destination.isMainScreen()) {
                fadeIn(animationSpec = tween(250))
            } else if (isToProfileDetail) {
                fadeIn(animationSpec = tween(250))
            } else if (isToProfile) {
                // When popping BACK to Profile from a detail screen
                slideInHorizontally(
                    initialOffsetX = { -it / 4 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
            } else {
                EnterTransition.None
            }
        },
        popExitTransition = {
            val isFromMorphScreen = initialState.destination.isMorphScreen()
            val isFromProfileDetail = initialState.destination.isProfileDetailScreen()

            if (isFromMorphScreen) {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300))
            } else if (isFromProfileDetail) {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
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
                refreshTrigger = refreshTrigger,
                onGlobalRefresh = onGlobalRefresh,
                onAccountSelected = { mainViewModel.onAccountSelected(it) },
                paddingValues = paddingValues,
                animatedVisibilityScope = this,
                onEditTransaction = { transactionId ->
                    navController.navigate(Screen.AddTransaction(transactionId))
                },
                onCardClick = { accountIdParam, isIncome ->
                    navController.navigate(
                        Screen.TransactionList(
                            accountId = accountIdParam,
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
                onGlobalRefresh = onGlobalRefresh,
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
                refreshTrigger = refreshTrigger,
                paddingValues = paddingValues,
                animatedVisibilityScope = this,
                onCategoryClick = { categoryName: String, categoryId: String, isIncome: Boolean, startDate: String?, endDate: String?, accountIdParam: String ->
                    val isTransactionCost = categoryId == "transaction_cost" || categoryName == "Transaction Fees"
                    navController.navigate(
                        Screen.TransactionList(
                            accountId = accountIdParam.ifEmpty { accountId ?: "" },
                            isIncome = if (isTransactionCost) null else isIncome,
                            categoryId = if (isTransactionCost) null else categoryId,
                            categoryName = if (isTransactionCost) null else categoryName,
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
                refreshTrigger = refreshTrigger,
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
            AccountsScreen(
                refreshTrigger = refreshTrigger,
                onGlobalRefresh = onGlobalRefresh,
                paddingValues = paddingValues
            )
        }

        composable<Screen.Categories> { 
            CategoryManagementScreen(
                refreshTrigger = refreshTrigger,
                paddingValues = paddingValues,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Settings> { 
            SettingsScreen(
                refreshTrigger = refreshTrigger,
                onGlobalRefresh = onGlobalRefresh,
                paddingValues = paddingValues
            )
        }

        composable<Screen.BudgetDetail> { backStackEntry ->
            val route: Screen.BudgetDetail = backStackEntry.toRoute()
            val budgetId = route.budgetId

            BudgetDetailScreen(
                budgetId = budgetId,
                onGlobalRefresh = onGlobalRefresh,
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
                    authViewModel.clearAuthStates()
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
                    authViewModel.clearAuthStates()
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
            val categoryId = route.categoryId
            val categoryName = route.categoryName
            val startDate = route.startDate
            val endDate = route.endDate
            val hasTransactionCost = route.hasTransactionCost

            TransactionListScreen(
                accountId = accountId, 
                isIncome = isIncome,
                categoryId = categoryId,
                categoryName = categoryName,
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
