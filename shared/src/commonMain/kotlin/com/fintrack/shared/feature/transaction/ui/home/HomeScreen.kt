package com.fintrack.shared.feature.transaction.ui.home

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.compose.backgroundGray
import com.fintrack.shared.feature.account.ui.AccountsViewModel
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.navigation.LocalSharedTransitionScope
import com.fintrack.shared.feature.settings.ui.SettingsViewModel
import com.fintrack.shared.feature.summary.ui.StatisticsViewModel
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
                onAccountSelected = { accountId -> accountsViewModel.selectAccount(accountId) },
                onToggleBalanceVisibility = { settingsViewModel.setBalanceHidden(it) },
                onRetry = { accountsViewModel.reloadAccounts() }
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
}