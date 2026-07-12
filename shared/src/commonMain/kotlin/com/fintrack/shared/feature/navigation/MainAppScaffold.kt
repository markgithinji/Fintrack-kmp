package com.fintrack.shared.feature.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.fintrack.shared.feature.auth.ui.AuthViewModel
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.ui.MaterialToast
import com.fintrack.shared.feature.transaction.ui.SmsPermissionLauncher
import com.fintrack.shared.feature.transaction.ui.TransactionViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainAppScaffold(
    isAuthenticated: Boolean,
    currentDestination: NavDestination?,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel = koinViewModel(),
    transactionsViewModel: TransactionViewModel = koinViewModel(),
    onLogout: () -> Unit = {}
) {
    val navController = LocalNavController.current
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

    // State to update AppBar per screen
    var appBarState by remember { mutableStateOf(AppBarState(title = "Home")) }

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
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    if (showTopBar) {
                        AppTopBar(
                            appBarState = appBarState,
                            onUpdateAppBarState = { newState -> appBarState = newState }
                        )
                    }
                },
                bottomBar = {
                    if (showBottomBar) {
                        BottomBar()
                    }
                },
                floatingActionButton = {
                    if (showFAB) {
                        AddTransactionFAB(
                            onClick = { navController.navigate(Screen.AddTransaction()) }
                        )
                    }
                },
                floatingActionButtonPosition = FabPosition.Center
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        isAuthenticated = isAuthenticated,
                        paddingValues = paddingValues,
                        onUpdateAppBarState = { newState -> appBarState = newState },
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
                                .align(androidx.compose.ui.Alignment.BottomCenter)
                                .padding(bottom = paddingValues.calculateBottomPadding() + 84.dp)
                        )
                    }
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
