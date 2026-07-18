package com.fintrack.shared.feature.transaction.ui.home

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import com.fintrack.shared.feature.account.ui.AccountsViewModel
import com.fintrack.shared.feature.core.logger.KMPLogger
import com.fintrack.shared.feature.core.ui.MaterialToast
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.navigation.ui.SmsSyncSignal
import com.fintrack.shared.feature.settings.ui.SettingsViewModel
import com.fintrack.shared.feature.summary.ui.StatisticsViewModel
import com.fintrack.shared.feature.transaction.ui.TransactionViewModel
import com.fintrack.shared.feature.transaction.ui.home.components.CategoryComparisonCard
import com.fintrack.shared.feature.transaction.ui.home.components.CurrentBalanceCardWrapper
import com.fintrack.shared.feature.transaction.ui.home.components.IncomeExpenseCards
import com.fintrack.shared.feature.transaction.ui.home.components.IncomeExpensesOverview
import com.fintrack.shared.feature.transaction.ui.home.components.TransactionsListCard
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    selectedAccountId: String?,
    onAccountSelected: (String) -> Unit,
    refreshTrigger: Int,
    smsSyncSignal: SmsSyncSignal? = null,
    onGlobalRefresh: () -> Unit,
    onSmsPermissionRequired: () -> Unit,
    onShowToast: (String, Boolean) -> Unit,
    accountsViewModel: AccountsViewModel = koinViewModel(),
    transactionsViewModel: TransactionViewModel = koinViewModel(),
    statsViewModel: StatisticsViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
    paddingValues: PaddingValues = PaddingValues(0.dp),
    animatedVisibilityScope: AnimatedVisibilityScope,
    onEditTransaction: (String) -> Unit,
    onCardClick: (accountId: String, isIncome: Boolean?) -> Unit
) {
    val accountsResult by accountsViewModel.accounts.collectAsStateWithLifecycle()
    val selectedAccountResult by accountsViewModel.selectedAccount.collectAsStateWithLifecycle()
    val transactionsResult by transactionsViewModel.recentTransactions.collectAsStateWithLifecycle()
    val overviewResult by statsViewModel.overview.collectAsStateWithLifecycle()
    val categoryComparisonResult by statsViewModel.categoryComparisons.collectAsStateWithLifecycle()
    val isBalanceHidden by settingsViewModel.isBalanceHidden.collectAsStateWithLifecycle()
    val defaultAccountId by settingsViewModel.defaultAccountId.collectAsStateWithLifecycle()
    val isMpesaListenerEnabled by settingsViewModel.isMpesaListenerEnabled.collectAsStateWithLifecycle()
    val isEquityListenerEnabled by settingsViewModel.isEquityListenerEnabled.collectAsStateWithLifecycle()
    val importState by transactionsViewModel.importState.collectAsStateWithLifecycle()
    val importProgress by transactionsViewModel.importProgress.collectAsStateWithLifecycle()

    LaunchedEffect(selectedAccountId) {
        selectedAccountId?.let { accountsViewModel.selectAccount(it) }
    }

    if (selectedAccountId == null && (accountsResult !is Result.Success || (accountsResult as Result.Success).data.isEmpty())) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (accountsResult is Result.Loading) {
                CircularProgressIndicator()
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No account found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Create your first account to start tracking your finances.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { onAccountSelected("") },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add First Account")
                    }
                }
            }
        }
        return
    }


    LaunchedEffect(smsSyncSignal) {
        if (smsSyncSignal != null) {
            transactionsViewModel.importTransactions(smsSyncSignal.accountId)
        }
    }

    LaunchedEffect(importState) {
        if (importState is Result.Success) {
            onGlobalRefresh()
            delay(1500)
            transactionsViewModel.resetImportState()
        } else if (importState is Result.Error) {
            val message = (importState as Result.Error).exception.message ?: ""
            if (message.contains("permission", ignoreCase = true)) {
                onSmsPermissionRequired()
                // We don't reset state here, let the rationale/scaffold handle it
            }
        }
    }

    // Keep track of the last processed refresh trigger to avoid redundant refreshes on re-entry
    // We use rememberSaveable to ensure it persists across navigation
    var lastProcessedRefreshTrigger by rememberSaveable { mutableIntStateOf(refreshTrigger) }

    val logger = remember { KMPLogger() }
    SideEffect {
        logger.error("HomeScreen", "TransactionViewModel INSTANCE: ${transactionsViewModel.hashCode()}")
    }

    LaunchedEffect(refreshTrigger, selectedAccountResult) {
        val accountId = (selectedAccountResult as? Result.Success)?.data?.id
        if (accountId != null && refreshTrigger > lastProcessedRefreshTrigger) {
            accountsViewModel.reloadAccounts(showLoading = false)
            transactionsViewModel.loadRecentTransactions(accountId, force = true)
            statsViewModel.loadOverview(accountId, force = true)
            statsViewModel.loadCategoryComparisons(accountId, force = true)
            statsViewModel.loadHighlights(accountId, force = true)
            transactionsViewModel.refreshCategories()
            lastProcessedRefreshTrigger = refreshTrigger
        }
    }

    LaunchedEffect(Unit) {
        if (accountsResult is Result.Error || (accountsResult is Result.Success && (accountsResult as Result.Success).data.isEmpty())) {
            accountsViewModel.reloadAccounts()
        }
    }

    LaunchedEffect(selectedAccountResult) {
        val accountId = (selectedAccountResult as? Result.Success)?.data?.id
        accountId?.let { id ->
            transactionsViewModel.loadRecentTransactions(id)
            statsViewModel.loadOverview(id)
            statsViewModel.loadCategoryComparisons(id)
            // Trigger auto-sync when account changes
            transactionsViewModel.autoSyncTransactions(id)
        }
    }

    val bottomPadding = paddingValues.calculateBottomPadding()
    var lastBottomPadding by remember { mutableStateOf(80.dp) }
    SideEffect {
        if (bottomPadding > 0.dp) {
            lastBottomPadding = bottomPadding
        }
    }
    
    val transition = animatedVisibilityScope.transition
    val isTransitionRunning = transition.isRunning
    val isExiting = transition.targetState == androidx.compose.animation.EnterExitState.PostExit || 
                   transition.targetState == androidx.compose.animation.EnterExitState.PreEnter
                   
    val stableBottomPadding = if (isTransitionRunning || isExiting || (bottomPadding == 0.dp && lastBottomPadding > 0.dp)) {
        lastBottomPadding
    } else {
        bottomPadding
    }

    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = stableBottomPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = paddingValues.calculateTopPadding() + 16.dp,
                end = 16.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CurrentBalanceCardWrapper(
                    accountsResult = accountsResult,
                    selectedAccountResult = selectedAccountResult,
                    defaultAccountId = defaultAccountId,
                    isBalanceHidden = isBalanceHidden,
                    isMpesaAutoSyncEnabled = isMpesaListenerEnabled,
                    isEquityAutoSyncEnabled = isEquityListenerEnabled,
                    importState = importState,
                    syncProgress = importProgress,
                    onAccountSelected = { accountId ->
                        onAccountSelected(accountId)
                        // Cancel existing sync and clear progress if we switch accounts
                        transactionsViewModel.cancelImport()
                    },
                    onToggleBalanceVisibility = { settingsViewModel.setBalanceHidden(it) },
                    onManualSync = { 
                        val accountId = (selectedAccountResult as? Result.Success)?.data?.id
                        transactionsViewModel.importTransactions(accountId) 
                    },
                    onSyncErrorClick = { message -> onShowToast(message, true) },
                    onRetry = {
                        accountsViewModel.reloadAccounts()
                        selectedAccountId?.let { accountsViewModel.selectAccount(it) }
                    }
                )
            }

            item {
                IncomeExpenseCards(
                    accountResult = selectedAccountResult,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onCardClick = { isIncome ->
                        val accountId = (selectedAccountResult as? Result.Success)?.data?.id
                        accountId?.let { onCardClick(it, isIncome) }
                    }
                )
            }

            item { IncomeExpensesOverview(overviewResult) }
            item {
                CategoryComparisonCard(
                    categoryComparisonResult = categoryComparisonResult,
                    accountId = (selectedAccountResult as? Result.Success)?.data?.id,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                TransactionsListCard(
                    transactionsResult = transactionsResult,
                    animatedVisibilityScope = animatedVisibilityScope,
                    accountId = (selectedAccountResult as? Result.Success)?.data?.id,
                    onViewAllClick = {
                        val accountId = (selectedAccountResult as? Result.Success)?.data?.id
                        accountId?.let { onCardClick(it, null) }
                    },
                    onTransactionClick = { transaction ->
                        transaction.id?.let { id -> onEditTransaction(id) }
                    }
                )
            }
        }
    }
}
