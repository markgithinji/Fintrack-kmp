package com.fintrack.shared.feature.transaction.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.compose.backgroundGray
import com.fintrack.shared.feature.account.ui.AccountsViewModel
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.summary.ui.StatisticsViewModel
import com.fintrack.shared.feature.transaction.ui.TransactionViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    accountsViewModel: AccountsViewModel = koinViewModel(),
    transactionsViewModel: TransactionViewModel = koinViewModel(),
    statsViewModel: StatisticsViewModel = koinViewModel(),
    onCardClick: (accountId: String, isIncome: Boolean?) -> Unit
) {
    val accountsResult by accountsViewModel.accounts.collectAsStateWithLifecycle()
    val selectedAccountResult by accountsViewModel.selectedAccount.collectAsStateWithLifecycle()
    val transactionsResult by transactionsViewModel.recentTransactions.collectAsStateWithLifecycle()
    val overviewResult by statsViewModel.overview.collectAsStateWithLifecycle()
    val categoryComparisonResult by statsViewModel.categoryComparisons.collectAsStateWithLifecycle()

    val onAccountSelected = remember(accountsViewModel) {
        { accountId: String ->
            accountsViewModel.selectAccount(accountId)
        }
    }

    val onRetry = remember(accountsViewModel) {
        {
            accountsViewModel.reloadAccounts()
        }
    }

    val onIncomeExpenseCardClick = remember(onCardClick, selectedAccountResult) {
        { isIncome: Boolean ->
            val accountId = (selectedAccountResult as? Result.Success)?.data?.id
            if (accountId != null) {
                onCardClick(accountId, isIncome)
            }
        }
    }

    val onViewAllTransactionsClick = remember(onCardClick, selectedAccountResult) {
        {
            val accountId = (selectedAccountResult as? Result.Success)?.data?.id
            if (accountId != null) {
                onCardClick(accountId, null)
            }
        }
    }

    // Reload dependent data whenever the selected account changes
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
            .background(backgroundGray),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CurrentBalanceCardWrapper(
                accountsResult = accountsResult,
                selectedAccountResult = selectedAccountResult,
                onAccountSelected = onAccountSelected,
                onRetry = onRetry
            )
        }

        item {
            IncomeExpenseCards(
                accountResult = selectedAccountResult,
                onCardClick = onIncomeExpenseCardClick
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
                onViewAllClick = onViewAllTransactionsClick
            )
        }
    }
}