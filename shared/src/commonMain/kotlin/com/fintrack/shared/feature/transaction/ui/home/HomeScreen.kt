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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import com.example.compose.backgroundGray
import com.fintrack.shared.feature.account.ui.AccountsViewModel
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.navigation.LocalSharedTransitionScope
import com.fintrack.shared.feature.settings.ui.SettingsViewModel
import com.fintrack.shared.feature.summary.ui.StatisticsViewModel
import com.fintrack.shared.feature.transaction.ui.SmsPermissionLauncher
import com.fintrack.shared.feature.transaction.ui.TransactionViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
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
    val isMpesaListenerEnabled by settingsViewModel.isMpesaListenerEnabled.collectAsStateWithLifecycle()
    val importState by transactionsViewModel.importState.collectAsStateWithLifecycle()
    val importProgress by transactionsViewModel.importProgress.collectAsStateWithLifecycle()
    
    var showSmsPermissionRequest by remember { mutableStateOf(false) }

    LaunchedEffect(importState) {
        if (importState is Result.Success) {
            delay(1500)
            transactionsViewModel.resetImportState()
        }
    }

    LaunchedEffect(Unit) {
        if (accountsResult is Result.Error || (accountsResult is Result.Success && (accountsResult as Result.Success).data.isEmpty())) {
            accountsViewModel.reloadAccounts(force = true)
        }
        
        // Auto-sync M-Pesa if enabled
        if (isMpesaListenerEnabled) {
            transactionsViewModel.autoSyncMpesaTransactions()
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
                isBalanceHidden = isBalanceHidden,
                isMpesaAutoSyncEnabled = isMpesaListenerEnabled,
                isSyncing = importState is Result.Loading,
                syncProgress = importProgress,
                onAccountSelected = { accountId -> accountsViewModel.selectAccount(accountId) },
                onToggleBalanceVisibility = { settingsViewModel.setBalanceHidden(it) },
                onSyncMpesa = { showSmsPermissionRequest = true },
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
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            TransactionsListCard(
                transactionsResult = transactionsResult,
                animatedVisibilityScope = animatedVisibilityScope,
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
                transactionsViewModel.importMpesaTransactions()
            }
        },
        onDismissTrigger = { showSmsPermissionRequest = false }
    )
}