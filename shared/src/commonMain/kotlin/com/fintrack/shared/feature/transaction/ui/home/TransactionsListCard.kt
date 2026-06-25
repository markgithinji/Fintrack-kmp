package com.fintrack.shared.feature.transaction.ui.home

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.compose.GreenIncome
import com.example.compose.PinkExpense
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.util.formatAsShortDate
import com.fintrack.shared.feature.core.util.formatToCurrency
import com.fintrack.shared.feature.navigation.LocalSharedTransitionScope
import com.fintrack.shared.feature.transaction.domain.model.Category
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import com.fintrack.shared.feature.transaction.ui.transactionlist.TransactionLoadingItem
import com.fintrack.shared.feature.transaction.ui.util.toColor
import com.fintrack.shared.feature.transaction.ui.util.toIcon

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TransactionsListCard(
    transactionsResult: Result<List<Transaction>>,
    onViewAllClick: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onTransactionClick: (Transaction) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (sharedTransitionScope != null) {
                    with(sharedTransitionScope) {
                        Modifier.sharedElement(
                            sharedContentState = rememberSharedContentState(key = "all_transactions_card"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                } else Modifier
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            RecentTransactionsHeader(
                transactionsResult = transactionsResult,
                onViewAllClick = onViewAllClick
            )

            when (transactionsResult) {
                is Result.Loading -> TransactionsLoadingState()
                is Result.Error -> TransactionsErrorState()
                is Result.Success -> {
                    val transactions = transactionsResult.data
                    if (transactions.isEmpty()) {
                        TransactionsEmptyState()
                    } else {
                        TransactionsListContent(
                            transactions = transactions,
                            onTransactionClick = onTransactionClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentTransactionsHeader(
    transactionsResult: Result<List<Transaction>>,
    onViewAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Recent Transactions",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        when (transactionsResult) {
            is Result.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = GreenIncome
                )
            }

            is Result.Success -> {
                if (transactionsResult.data.isNotEmpty()) {
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelLarge,
                        color = GreenIncome,
                        modifier = Modifier.clickable { onViewAllClick() }
                    )
                }
            }

            else -> {
                // Don't show anything for Error state
            }
        }
    }
}

@Composable
private fun TransactionsLoadingState() {
    Column {
        repeat(3) { index ->
            TransactionLoadingItem(
                padding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            )
            if (index < 2) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    thickness = 0.5.dp,
                    color = Color.LightGray.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
private fun TransactionsErrorState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(32.dp)
        )

        Text(
            text = "Failed to load transactions",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = { /* Add retry logic */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text("Try Again")
        }
    }
}

@Composable
private fun TransactionsEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
            contentDescription = "No Transactions",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(32.dp)
        )

        Text(
            text = "No recent transactions",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TransactionsListContent(
    transactions: List<Transaction>,
    onTransactionClick: (Transaction) -> Unit
) {
    Column {
        transactions.forEachIndexed { index, transaction ->
            TransactionRow(
                transaction = transaction,
                onClick = { onTransactionClick(transaction) }
            )
            if (index < transactions.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 80.dp, end = 20.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun TransactionRow(
    transaction: Transaction,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val category = Category.fromName(transaction.category, isExpense = !transaction.isIncome)
    val isExpense = !transaction.isIncome
    val amountColor = if (isExpense) PinkExpense else GreenIncome

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = category.toColor().copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.toIcon(),
                contentDescription = category.name,
                tint = category.toColor(),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            transaction.description?.let { description ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${if (isExpense) "-" else "+"}${transaction.amount.formatToCurrency()}",
                style = MaterialTheme.typography.bodyLarge,
                color = amountColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = transaction.dateTime.date.formatAsShortDate(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}