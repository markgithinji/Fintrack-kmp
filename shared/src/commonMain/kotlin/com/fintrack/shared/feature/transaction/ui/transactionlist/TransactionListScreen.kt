package com.fintrack.shared.feature.transaction.ui.transactionlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.summary.domain.model.TransactionCountSummary
import com.fintrack.shared.feature.summary.ui.StatisticsViewModel
import com.fintrack.shared.feature.transaction.domain.model.Category
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import com.fintrack.shared.feature.transaction.ui.TransactionViewModel
import com.fintrack.shared.feature.transaction.ui.addtransaction.AnimatedShimmerBox
import com.fintrack.shared.feature.transaction.ui.home.GreenIncome
import com.fintrack.shared.feature.transaction.ui.util.toColor
import com.fintrack.shared.feature.transaction.ui.util.toIcon
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel
import androidx.paging.compose.collectAsLazyPagingItems

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
        statisticsViewModel.loadTransactionCounts(accountId)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TransactionCountHeaderWithLoading(
                transactionCounts = transactionCounts,
                isIncome = isIncome
            )
        }

        // Initial loading state
        if (transactions.loadState.refresh is LoadState.Loading) {
            items(5) { index ->
                LoadingTransactionItem()
            }
        }

        // Error state for initial load
        transactions.loadState.refresh.let { loadState ->
            if (loadState is LoadState.Error) {
                item {
                    ErrorState(
                        message = loadState.error.message ?: "Failed to load transactions",
                        onRetry = { transactions.retry() }
                    )
                }
            }
        }

        // Transactions list
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


        // Loading more state
        transactions.loadState.append.let { loadState ->
            if (loadState is LoadState.Loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = GreenIncome
                        )
                    }
                }
            }

            if (loadState is LoadState.Error) {
                item {
                    ErrorState(
                        message = loadState.error.message ?: "Failed to load more transactions",
                        onRetry = { transactions.retry() }
                    )
                }
            }
        }

        // Empty state
        if (transactions.loadState.refresh is LoadState.NotLoading && transactions.itemCount == 0) {
            item {
                EmptyState(isIncome = isIncome)
            }
        }
    }
}

@Composable
fun TransactionCountHeaderWithLoading(
    transactionCounts: Result<TransactionCountSummary>,
    isIncome: Boolean?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        when (transactionCounts) {
            is Result.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = GreenIncome
                        )
                        Text(
                            text = "Loading transaction count...",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            is Result.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Failed to load count",
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            is Result.Success -> {
                val counts = transactionCounts.data
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    TransactionCountHeader(
                        isIncome = isIncome,
                        income = counts.totalIncomeTransactions,
                        expense = counts.totalExpenseTransactions,
                        total = counts.totalTransactions
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingTransactionItem() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Loading icon
                AnimatedShimmerBox(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    // Loading category name
                    AnimatedShimmerBox(
                        modifier = Modifier
                            .width(120.dp)
                            .height(16.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Loading description
                    AnimatedShimmerBox(
                        modifier = Modifier
                            .width(80.dp)
                            .height(12.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Loading date
                    AnimatedShimmerBox(
                        modifier = Modifier
                            .width(60.dp)
                            .height(10.dp)
                    )
                }
            }

            // Loading amount
            AnimatedShimmerBox(
                modifier = Modifier
                    .width(70.dp)
                    .height(16.dp)
            )
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Error",
                tint = Color.Red,
                modifier = Modifier.size(48.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Unable to Load Transactions",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = GreenIncome),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text(
                    text = "Try Again",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EmptyState(isIncome: Boolean?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                contentDescription = "No transactions",
                tint = Color.Gray,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = when (isIncome) {
                    true -> "No Income Transactions"
                    false -> "No Expense Transactions"
                    else -> "No Transactions Found"
                },
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = when (isIncome) {
                    true -> "Income transactions will appear here"
                    false -> "Expense transactions will appear here"
                    else -> "Transactions will appear here once you add them"
                },
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun TransactionCountHeader(
    isIncome: Boolean?,
    income: Int,
    expense: Int,
    total: Int
) {
    val text = when (isIncome) {
        true -> "$income Transactions"
        false -> "$expense Transactions"
        null -> "$total Transactions"
    }

    Text(
        text = text,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}


fun LocalDate.toShortFormat(): String {
    val month = when (this.monthNumber) {
        1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
        5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
        9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
        else -> ""
    }
    return "$month ${this.dayOfMonth}, ${this.year}"
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val category = Category.fromName(
        transaction.category,
        isExpense = !transaction.isIncome
    )

    val amountColor = if (transaction.isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)
    val prefix = if (transaction.isIncome) "+ " else "- "

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(category.toColor().copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.toIcon(),
                        contentDescription = category.name,
                        tint = category.toColor(),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = category.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    if (!transaction.description.isNullOrBlank()) {
                        Text(
                            text = transaction.description!!,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Text(
                        text = transaction.dateTime.date.toShortFormat(),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Text(
                text = prefix + "KSh ${transaction.amount}",
                color = amountColor,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}
