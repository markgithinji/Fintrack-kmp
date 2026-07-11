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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import com.fintrack.shared.feature.auth.ui.AuthViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainAppScaffold(
    isAuthenticated: Boolean,
    currentDestination: NavDestination?,
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit = {}
) {
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
                        BottomBar(navController)
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
                AppNavigation(
                    isAuthenticated = isAuthenticated,
                    navController = navController,
                    paddingValues = paddingValues,
                    onUpdateAppBarState = { newState -> appBarState = newState },
                    authViewModel = authViewModel,
                    onLogout = onLogout
                )
            }
        }
    }
}
