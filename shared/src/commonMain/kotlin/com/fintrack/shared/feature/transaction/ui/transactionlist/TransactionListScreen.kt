package com.fintrack.shared.feature.transaction.ui.transactionlist

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.compose.transactionBackground
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.navigation.LocalSharedTransitionScope
import com.fintrack.shared.feature.summary.domain.model.TransactionCountSummary
import com.fintrack.shared.feature.summary.ui.StatisticsViewModel
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import com.fintrack.shared.feature.transaction.ui.TransactionViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TransactionListScreen(
    accountId: String,
    isIncome: Boolean? = null,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onEditTransaction: (String) -> Unit,
    transactionsViewModel: TransactionViewModel = koinViewModel(),
    statisticsViewModel: StatisticsViewModel = koinViewModel()
) {
    val transactionCounts by statisticsViewModel.transactionCounts.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val sharedTransitionScope = LocalSharedTransitionScope.current

    val transactions = remember(accountId, isIncome) {
        transactionsViewModel.getTransactionsPagingData(accountId, isIncome)
    }.collectAsLazyPagingItems()

    LaunchedEffect(accountId, isIncome) {
        statisticsViewModel.loadTransactionCounts(accountId, isIncome)
    }

    TransactionListContent(
        transactionCounts = transactionCounts,
        transactions = transactions,
        isIncome = isIncome,
        listState = listState,
        animatedVisibilityScope = animatedVisibilityScope,
        sharedTransitionScope = sharedTransitionScope,
        onTransactionClick = onEditTransaction
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun TransactionListContent(
    transactionCounts: Result<TransactionCountSummary>,
    transactions: LazyPagingItems<Transaction>,
    isIncome: Boolean?,
    listState: androidx.compose.foundation.lazy.LazyListState,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope?,
    onTransactionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (sharedTransitionScope != null) {
                    with(sharedTransitionScope) {
                        Modifier.sharedElement(
                            sharedContentState = rememberSharedContentState(
                                key = when (isIncome) {
                                    true -> "income_card"
                                    false -> "expense_card"
                                    null -> "all_transactions_card"
                                }
                            ),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                } else Modifier
            )
            .background(transactionBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        state = listState
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
                    TransactionLoadingItem(
                        padding = PaddingValues(14.dp)
                    )
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
                            transactions[index]?.id ?: "loading_$index"
                        }
                    ) { index ->
                        val transaction = transactions[index]
                        if (transaction != null) {
                            TransactionItem(
                                transaction = transaction,
                                onClick = { transaction.id?.let { id -> onTransactionClick(id) } }
                            )
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