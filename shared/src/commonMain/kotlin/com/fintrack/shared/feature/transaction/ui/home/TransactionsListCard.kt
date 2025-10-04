package com.fintrack.shared.feature.transaction.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.transaction.model.Category
import com.fintrack.shared.feature.transaction.model.Transaction
import com.fintrack.shared.feature.transaction.ui.addtransaction.LoadingTransactionRow
import com.fintrack.shared.feature.transaction.ui.toColor
import com.fintrack.shared.feature.transaction.ui.toIcon
import kotlinx.datetime.LocalDate
@Composable
fun TransactionsListCard(
    transactionsResult: Result<List<Transaction>>,
    onViewAllClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header is always visible
            RecentTransactionsHeader(
                onViewAllClick = onViewAllClick,
                isLoading = transactionsResult is Result.Loading
            )

            Spacer(modifier = Modifier.height(8.dp))

            when (transactionsResult) {
                is Result.Loading -> {
                    // Show 2 loading transaction rows
                    repeat(3) { index ->
                        LoadingTransactionRow()
                        if (index < 2) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = Color.LightGray.copy(alpha = 0.4f)
                            )
                        }
                    }
                }

                is Result.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = Color.Red,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "Failed to load transactions",
                                color = Color.Red,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { /* You can add retry logic here */ },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenIncome),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = "Try Again",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                is Result.Success -> {
                    val transactions = transactionsResult.data
                    if (transactions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = "No transactions",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "No recent transactions",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        transactions.forEachIndexed { index, transaction ->
                            TransactionRow(transaction)

                            if (index < transactions.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    thickness = 0.5.dp,
                                    color = Color.LightGray.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentTransactionsHeader(
    onViewAllClick: () -> Unit,
    isLoading: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Recent Transactions",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        if (isLoading) {
            // Show small loading indicator instead of "View All" during loading
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = GreenIncome
            )
        } else {
            Text(
                text = "View All",
                fontSize = 14.sp,
                color = GreenIncome,
                modifier = Modifier.clickable { onViewAllClick() }
            )
        }
    }
}


@Composable
fun TransactionRow(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    val category = Category.fromName(transaction.category, isExpense = !transaction.isIncome)
    val isExpense = category.isExpense

    fun LocalDate.toShortMonthDay(): String {
        val month = when (this.monthNumber) {
            1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
            5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
            9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
            else -> ""
        }
        return "$month ${this.dayOfMonth}"
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = category.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    transaction.description?.let {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = it, fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isExpense) "-" else "+"}${transaction.amount}",
                    color = if (isExpense) PinkExpense else GreenIncome,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = transaction.dateTime.date.toShortMonthDay(),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        HorizontalDivider(
            color = Color.LightGray.copy(alpha = 0.4f),
            thickness = 0.7.dp,
            modifier = Modifier.padding(start = 80.dp)
        )
    }
}

