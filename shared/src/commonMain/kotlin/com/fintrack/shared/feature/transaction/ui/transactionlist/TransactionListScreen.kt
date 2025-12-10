package com.fintrack.shared.feature.transaction.ui.transactionlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.summary.domain.model.TransactionCountSummary
import com.fintrack.shared.feature.summary.ui.StatisticsViewModel
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import com.fintrack.shared.feature.transaction.ui.TransactionViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TransactionListScreen(
    accountId: String,
    isIncome: Boolean? = null,
    transactionsViewModel: TransactionViewModel = koinViewModel(),
    statisticsViewModel: StatisticsViewModel = koinViewModel()
) {
    val transactionCounts by statisticsViewModel.transactionCounts.collectAsStateWithLifecycle()

    val transactionsFlow = remember(accountId, isIncome) {
        transactionsViewModel.getTransactionsPagingData(accountId, isIncome)
    }
    val transactions = transactionsFlow.collectAsLazyPagingItems()

    LaunchedEffect(accountId, isIncome) {
        statisticsViewModel.loadTransactionCounts(accountId, isIncome)
    }

    TransactionListContent(
        transactionCounts = transactionCounts,
        transactions = transactions,
        isIncome = isIncome
    )
}

@Composable
private fun TransactionListContent(
    transactionCounts: Result<TransactionCountSummary>,
    transactions: LazyPagingItems<Transaction>,
    isIncome: Boolean?
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TransactionCountHeaderCard(
                transactionCounts = transactionCounts,
                isIncome = isIncome
            )
        }

        when (val refreshState = transactions.loadState.refresh) {
            is LoadState.Loading -> {
                items(5) { index ->
                    LoadingTransactionItem()
                }
            }

            is LoadState.Error -> {
                item {
                    TransactionListErrorState(
                        message = refreshState.error.message ?: "Failed to load transactions",
                        onRetry = { transactions.retry() }
                    )
                }
            }

            else -> {
                if (transactions.itemCount == 0) {
                    item {
                        TransactionListEmptyState(isIncome = isIncome)
                    }
                } else {
                    items(
                        count = transactions.itemCount,
                        key = { index ->
                            val transaction = transactions.peek(index)
                            // Combine ID + index for uniqueness
                            transaction?.let { "${it.id}_$index" } ?: "loading_$index"
                        }
                    ) { index ->
                        val transaction = transactions[index]
                        if (transaction != null) {
                            TransactionItem(transaction = transaction)
                        }
                    }
                }
            }
        }

        when (val appendState = transactions.loadState.append) {
            is LoadState.Loading -> {
                item {
                    TransactionListLoadingMoreState()
                }
            }

            is LoadState.Error -> {
                item {
                    TransactionListErrorState(
                        message = appendState.error.message ?: "Failed to load more transactions",
                        onRetry = { transactions.retry() }
                    )
                }
            }

            else -> Unit
        }
    }
}