package com.fintrack.shared.feature.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fintrack.shared.feature.auth.ui.AuthViewModel
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.auth.ui.LoginScreen
import com.fintrack.shared.feature.auth.ui.rememberAuthState
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
fun MainScreen() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = koinViewModel()
    val authStatus by authViewModel.authStatus.collectAsStateWithLifecycle()

    // State to update AppBar per screen
    var appBarState by remember { mutableStateOf(AppBarState(title = "Home")) }

    // Track current route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Decide when to show bars
    val hideBars = currentRoute == Screen.TransactionList.route ||
            currentRoute == Screen.BudgetDetail.route ||
            currentRoute == Screen.AddTransaction.route ||
            currentRoute == Screen.Accounts.route ||
            currentRoute == Screen.Categories.route ||
            currentRoute == Screen.Settings.route

    // Handle authentication state changes
    LaunchedEffect(authStatus) {
        when (authStatus) {
            is Result.Loading -> {
                // Show loading state or stay on current screen
            }
            is Result.Success -> {
                val isAuthenticated = (authStatus as Result.Success<Boolean>).data
                if (isAuthenticated) {
                    // User is authenticated, ensure we're not on login screen
                    if (currentRoute == Screen.Login.route) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                } else {
                    // User is not authenticated, redirect to login
                    if (currentRoute != Screen.Login.route) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
            is Result.Error -> {
                // On error, treat as unauthenticated
                if (currentRoute != Screen.Login.route) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    // Show loading screen while checking auth
    when (authStatus) {
        is Result.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Checking authentication...")
                }
            }
        }
        is Result.Success -> {
            val isAuthenticated = (authStatus as Result.Success<Boolean>).data
            Scaffold(
                topBar = {
                    if (isAuthenticated && !hideBars) {
                        TopBar(
                            title = appBarState.title,
                            showBackButton = appBarState.showBackButton,
                            onBack = appBarState.onBack
                        )
                    }
                },
                bottomBar = {
                    if (isAuthenticated && !hideBars) {
                        BottomBar(navController)
                    }
                },
                floatingActionButton = {
                    if (isAuthenticated &&
                        !hideBars &&
                        currentRoute == Screen.Home.route
                    ) {
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
                    startDestination = if (isAuthenticated) Screen.Home.route else Screen.Login.route,
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable(Screen.Home.route) {
                        LaunchedEffect(Unit) {
                            appBarState = AppBarState(title = "Home")
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

                    composable(Screen.Profile.route) {
                        LaunchedEffect(Unit) {
                            appBarState = AppBarState(title = "Profile")
                        }
                        ProfileScreen(
                            onNavigateToAccounts = { navController.navigate(Screen.Accounts.route) },
                            onNavigateToCategories = { navController.navigate(Screen.Categories.route) },
                            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                            onNavigateToBudgets = { navController.navigate(Screen.Budget.route) },
                            onLogout = {
                                authViewModel.logout()
                            }
                        )
                    }

                    composable(Screen.Accounts.route) {
                        LaunchedEffect(Unit) {
                            appBarState = AppBarState(
                                title = "Accounts",
                                showBackButton = true,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        AccountsScreen()
                    }

                    composable(Screen.Categories.route) {
                        LaunchedEffect(Unit) {
                            appBarState = AppBarState(
                                title = "Categories",
                                showBackButton = true,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        CategoriesScreen()
                    }

                    composable(Screen.Settings.route) {
                        LaunchedEffect(Unit) {
                            appBarState = AppBarState(
                                title = "Settings",
                                showBackButton = true,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        SettingsScreen()
                    }

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
                            appBarState = AppBarState(
                                title = if (budgetId == null) "Add Budget" else "Edit Budget",
                                showBackButton = true,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        BudgetDetailScreen(
                            budgetId = budgetId,
                            accountId = accountId,
                            onSave = { navController.popBackStack() },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.Login.route) {
                        LaunchedEffect(Unit) {
                            appBarState = AppBarState(title = "Login")
                        }
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
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
        is Result.Error -> {
            // Show error state or redirect to login
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Authentication error", color = Color.Red)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { authViewModel.checkAuthenticationStatus() }
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}