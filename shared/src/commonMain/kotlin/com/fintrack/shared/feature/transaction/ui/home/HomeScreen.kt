package com.fintrack.shared.feature.transaction.ui.home

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import com.fintrack.shared.ui.theme.backgroundGray
import com.fintrack.shared.feature.account.domain.model.AccountType
import com.fintrack.shared.feature.account.ui.AccountsViewModel
import com.fintrack.shared.feature.core.logger.KMPLogger
import com.fintrack.shared.feature.core.ui.MaterialToast
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.navigation.LocalSharedTransitionScope
import com.fintrack.shared.feature.navigation.MainViewModel
import com.fintrack.shared.feature.settings.ui.SettingsViewModel
import com.fintrack.shared.feature.summary.ui.StatisticsViewModel
import com.fintrack.shared.feature.transaction.ui.SmsPermissionLauncher
import com.fintrack.shared.feature.transaction.ui.TransactionViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    selectedAccountId: String?,
    onAccountSelected: (String) -> Unit,
    accountsViewModel: AccountsViewModel = koinViewModel(),
    transactionsViewModel: TransactionViewModel = koinViewModel(),
    statsViewModel: StatisticsViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel(),
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
    val isMpesaListenerEnabled by settingsViewModel.isMpesaListenerEnabled.collectAsStateWithLifecycle()
    val isEquityListenerEnabled by settingsViewModel.isEquityListenerEnabled.collectAsStateWithLifecycle()
    val importState by transactionsViewModel.importState.collectAsStateWithLifecycle()
    val importProgress by transactionsViewModel.importProgress.collectAsStateWithLifecycle()
    
    val logger = remember { KMPLogger() }

    var showSmsPermissionRequest by remember { mutableStateOf(false) }
    var syncErrorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedAccountId) {
        accountsViewModel.selectAccount(selectedAccountId)
    }


    LaunchedEffect(importState) {
        if (importState is Result.Success) {
            delay(1500)
            transactionsViewModel.resetImportState()
        }
    }

    LaunchedEffect(Unit) {
        mainViewModel.refreshEvent.collect {
            accountsViewModel.reloadAccounts(force = true, showLoading = false)
            val accountId = (selectedAccountResult as? Result.Success)?.data?.id
            accountId?.let { id ->
                transactionsViewModel.loadRecentTransactions(id, force = true)
                statsViewModel.loadOverview(id, force = true)
                statsViewModel.loadCategoryComparisons(id, force = true)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (accountsResult is Result.Error || (accountsResult is Result.Success && (accountsResult as Result.Success).data.isEmpty())) {
            accountsViewModel.reloadAccounts(force = true)
        }
        
        // Auto-sync transactions if enabled
        if (isMpesaListenerEnabled || isEquityListenerEnabled) {
            transactionsViewModel.autoSyncTransactions()
        }
    }

    LaunchedEffect(selectedAccountResult) {
        val accountId = (selectedAccountResult as? Result.Success)?.data?.id
        accountId?.let { id ->
            transactionsViewModel.loadRecentTransactions(id)
            statsViewModel.loadOverview(id)
            statsViewModel.loadCategoryComparisons(id)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = paddingValues.calculateTopPadding() + 16.dp,
                end = 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CurrentBalanceCardWrapper(
                    accountsResult = accountsResult,
                    selectedAccountResult = selectedAccountResult,
                    defaultAccountId = selectedAccountId,
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
                    onManualSync = { showSmsPermissionRequest = true },
                    onSyncErrorClick = { message -> syncErrorMessage = message },
                    onRetry = {
                        accountsViewModel.reloadAccounts(force = true)
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

        syncErrorMessage?.let { message ->
            MaterialToast(
                message = message,
                isError = true,
                onDismiss = { syncErrorMessage = null },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = paddingValues.calculateBottomPadding() + 16.dp)
            )
        }
    }
}