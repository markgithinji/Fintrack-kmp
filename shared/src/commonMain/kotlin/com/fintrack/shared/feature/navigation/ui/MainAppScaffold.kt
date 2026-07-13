package com.fintrack.shared.feature.navigation.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.fintrack.shared.feature.auth.ui.AuthViewModel
import com.fintrack.shared.feature.core.ui.MaterialToast
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.navigation.model.AppBarState
import com.fintrack.shared.feature.navigation.model.Screen
import com.fintrack.shared.feature.transaction.ui.SmsPermissionLauncher
import com.fintrack.shared.feature.transaction.ui.TransactionViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainAppScaffold(
    isAuthenticated: Boolean,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel = koinViewModel(),
    transactionsViewModel: TransactionViewModel = koinViewModel(),
    onLogout: () -> Unit = {}
) {
    val navController = LocalNavController.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val appBarState by mainViewModel.appBarState.collectAsStateWithLifecycle()
    val importState by transactionsViewModel.importState.collectAsStateWithLifecycle()

    var toastMessage by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var showSmsPermissionRequest by remember { mutableStateOf(false) }

    LaunchedEffect(importState) {
        if (importState is Result.Success) {
            toastMessage = "Sync completed successfully" to false
            transactionsViewModel.resetImportState()
        } else if (importState is Result.Error) {
            toastMessage = ((importState as Result.Error).exception.message ?: "Sync failed") to true
            transactionsViewModel.resetImportState()
        }
    }

    // Automatically derive AppBarState from the current route
    LaunchedEffect(navBackStackEntry) {
        val entry = navBackStackEntry ?: return@LaunchedEffect
        val destination = entry.destination
        
        val newState = when {
            destination.hasRoute<Screen.Home>() -> AppBarState(title = "Home")
            destination.hasRoute<Screen.Statistics>() -> AppBarState(title = "Statistics")
            destination.hasRoute<Screen.Budget>() -> AppBarState(title = "Budget")
            destination.hasRoute<Screen.Profile>() -> AppBarState(title = "Profile")
            destination.hasRoute<Screen.EditProfile>() -> AppBarState(
                title = "Edit Profile", 
                showBackButton = true,
                onBack = { navController.popBackStack() }
            )
            destination.hasRoute<Screen.Accounts>() -> AppBarState(
                title = "Accounts", 
                showBackButton = true,
                onBack = { navController.popBackStack() }
            )
            destination.hasRoute<Screen.Categories>() -> AppBarState(
                title = "Categories", 
                showBackButton = true,
                onBack = { navController.popBackStack() }
            )
            destination.hasRoute<Screen.Settings>() -> AppBarState(
                title = "Settings", 
                showBackButton = true,
                onBack = { navController.popBackStack() }
            )
            destination.hasRoute<Screen.BudgetDetail>() -> {
                val route = entry.toRoute<Screen.BudgetDetail>()
                AppBarState(
                    title = if (route.budgetId == null) "Add Budget" else "Edit Budget",
                    showBackButton = true,
                    onBack = { navController.popBackStack() }
                )
            }
            destination.hasRoute<Screen.AddTransaction>() -> {
                val route = entry.toRoute<Screen.AddTransaction>()
                AppBarState(
                    title = if (route.transactionId == null) "Add Transaction" else "Edit Transaction",
                    showBackButton = true,
                    onBack = { navController.popBackStack() }
                )
            }
            destination.hasRoute<Screen.TransactionList>() -> {
                val route = entry.toRoute<Screen.TransactionList>()
                AppBarState(
                    title = when {
                        route.hasTransactionCost == true -> "Transaction Fees"
                        route.category?.contains(",") == true -> "Other Categories"
                        route.category != null -> route.category
                        route.isIncome == true -> "Income Transactions"
                        route.isIncome == false -> "Expense Transactions"
                        else -> "All Transactions"
                    },
                    showBackButton = true,
                    onBack = { navController.popBackStack() }
                )
            }
            destination.hasRoute<Screen.Login>() -> AppBarState(title = "Login")
            destination.hasRoute<Screen.Register>() -> AppBarState(
                title = "Create Account", 
                showBackButton = true,
                onBack = { 
                    navController.navigate(Screen.Login) {
                        popUpTo(Screen.Register) { inclusive = true }
                    }
                }
            )
            else -> AppBarState(title = "Fintrack")
        }
        mainViewModel.updateAppBarState(newState)
    }

    // Show bars only if not on login/register screens
    val showTopBar = remember(currentDestination) {
        if (currentDestination == null) return@remember true
        !(currentDestination.hasRoute<Screen.Login>() || currentDestination.hasRoute<Screen.Register>())
    }

    val showBottomBar = remember(currentDestination) {
        if (currentDestination == null) return@remember true
        currentDestination.hasRoute<Screen.Home>() ||
        currentDestination.hasRoute<Screen.Statistics>() ||
        currentDestination.hasRoute<Screen.Budget>() ||
        currentDestination.hasRoute<Screen.Profile>()
    }

    val showFAB = remember(currentDestination) {
        if (currentDestination == null) return@remember true
        currentDestination.hasRoute<Screen.Home>() ||
        currentDestination.hasRoute<Screen.Statistics>() ||
        currentDestination.hasRoute<Screen.Budget>() ||
        currentDestination.hasRoute<Screen.Profile>()
    }

    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            Box(modifier = Modifier.fillMaxSize()) {
                NavigationSuiteScaffold(
                    navigationSuiteItems = {
                        if (showBottomBar) {
                            item(
                                selected = currentDestination?.hierarchy?.any { it.hasRoute<Screen.Home>() } == true,
                                onClick = {
                                    navController.navigate(Screen.Home()) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Home") }
                            )
                            item(
                                selected = currentDestination?.hierarchy?.any { it.hasRoute<Screen.Statistics>() } == true,
                                onClick = {
                                    navController.navigate(Screen.Statistics()) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(Icons.Default.BarChart, contentDescription = "Stats") },
                                label = { Text("Stats") }
                            )

                            // Spacing for the FAB
                            item(
                                selected = false,
                                icon = { Box(Modifier.size(1.dp)) },
                                label = { Text("") },
                                enabled = false,
                                onClick = {}
                            )

                            item(
                                selected = currentDestination?.hierarchy?.any { it.hasRoute<Screen.Budget>() } == true,
                                onClick = {
                                    navController.navigate(Screen.Budget) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(Icons.Default.Info, contentDescription = "Budget") },
                                label = { Text("Budget") }
                            )
                            item(
                                selected = currentDestination?.hierarchy?.any { it.hasRoute<Screen.Profile>() } == true,
                                onClick = {
                                    navController.navigate(Screen.Profile) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                label = { Text("Profile") }
                            )
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ) {
                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.background,
                        topBar = {
                            if (showTopBar) {
                                AppTopBar(
                                    appBarState = appBarState
                                )
                            }
                        }
                    ) { paddingValues ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            AppNavigation(
                                isAuthenticated = isAuthenticated,
                                paddingValues = paddingValues,
                                authViewModel = authViewModel,
                                mainViewModel = mainViewModel,
                                onLogout = onLogout
                            )

                            toastMessage?.let { (message, isError) ->
                                MaterialToast(
                                    message = message,
                                    isError = isError,
                                    onDismiss = { toastMessage = null },
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = paddingValues.calculateBottomPadding() + 84.dp)
                                )
                            }
                        }
                    }
                }

                // Place FAB over everything
                if (showFAB) {
                    AddTransactionFAB(
                        onClick = { navController.navigate(Screen.AddTransaction()) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 85.dp)
                            .zIndex(100f)
                    )
                }
            }
        }
    }

    SmsPermissionLauncher(
        trigger = showSmsPermissionRequest,
        onResult = { granted ->
            if (granted) {
                transactionsViewModel.importTransactions()
            }
            showSmsPermissionRequest = false
        },
        onDismissTrigger = { showSmsPermissionRequest = false }
    )
}
