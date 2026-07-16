package com.fintrack.shared.feature.navigation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.shared.feature.core.ui.LocalSharedTransitionScope
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.fintrack.shared.feature.auth.ui.AuthViewModel
import com.fintrack.shared.feature.core.ui.MaterialToast
import com.fintrack.shared.feature.core.ui.permission.SmsPermissionLauncher
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.navigation.model.AppBarState
import com.fintrack.shared.feature.navigation.model.Screen
import com.fintrack.shared.feature.navigation.ui.components.AddTransactionFAB
import com.fintrack.shared.feature.navigation.ui.components.AppTopBar
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
    val visibleEntries by navController.visibleEntries.collectAsStateWithLifecycle()
    val currentDestination = navBackStackEntry?.destination
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

    val appBarState = remember(navBackStackEntry) {
        currentDestination?.getAppBarState(navBackStackEntry, navController) ?: AppBarState(title = "Home")
    }

    // Show bars only if any of the visible entries requires them (avoids layout jumps during transitions)
    val showTopBar = remember(visibleEntries) {
        visibleEntries.any { entry -> !entry.destination.isAuthScreen() }
    }

    // Logic for animating the bars away immediately upon navigation
    val showBottomBarNow = remember(currentDestination) {
        currentDestination?.shouldShowBottomBar() ?: true
    }

    // Keep the composable in the hierarchy during transitions to stabilize content area
    val keepBottomBarInHierarchy = remember(visibleEntries) {
        visibleEntries.any { entry -> entry.destination.shouldShowBottomBar() }
    }

    val showFABNow = remember(currentDestination) {
        currentDestination?.shouldShowFAB() ?: true
    }

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val suiteType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (showTopBar) {
                    AppTopBar(
                        appBarState = appBarState
                    )
                }
            },
            bottomBar = {
                if (suiteType == NavigationSuiteType.NavigationBar && keepBottomBarInHierarchy) {
                    Box(modifier = Modifier.height(80.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())) {
                        AnimatedVisibility(
                            visible = showBottomBarNow,
                            enter = slideInVertically(
                                animationSpec = spring(stiffness = Spring.StiffnessMedium)
                            ) { it },
                            exit = slideOutVertically(
                                animationSpec = spring(stiffness = Spring.StiffnessMedium)
                            ) { it }
                        ) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ) {
                                NavigationBarItem(
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
                                NavigationBarItem(
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
                                NavigationBarItem(
                                    selected = false,
                                    icon = { Box(Modifier.size(1.dp)) },
                                    label = { Text("") },
                                    enabled = false,
                                    onClick = {}
                                )

                                NavigationBarItem(
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
                                NavigationBarItem(
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
                        }
                    }
                }
            }
        ) { paddingValues ->
            Row(modifier = Modifier.fillMaxSize()) {
                if (suiteType == NavigationSuiteType.NavigationRail) {
                    AnimatedVisibility(
                        visible = showBottomBarNow,
                        enter = slideInHorizontally { -it },
                        exit = slideOutHorizontally { -it }
                    ) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            NavigationRailItem(
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
                            NavigationRailItem(
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

                            NavigationRailItem(
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
                            NavigationRailItem(
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
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
                        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                            AppNavigation(
                                isAuthenticated = isAuthenticated,
                                paddingValues = paddingValues,
                                authViewModel = authViewModel,
                                mainViewModel = mainViewModel,
                                onLogout = onLogout
                            )
                        }
                    }
                }
            }
        }

        toastMessage?.let { (message, isError) ->
            MaterialToast(
                message = message,
                isError = isError,
                onDismiss = { toastMessage = null },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (showBottomBarNow) 72.dp else 24.dp)
            )
        }

        // Place FAB over everything
        AnimatedVisibility(
            visible = showFABNow,
            enter = slideInVertically(
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            ) { it * 2 } + fadeIn(),
            exit = slideOutVertically(
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            ) { it * 2 } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(100f)
        ) {
            AddTransactionFAB(
                onClick = { navController.navigate(Screen.AddTransaction()) },
                modifier = Modifier.padding(bottom = 85.dp)
            )
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
