package com.fintrack.shared.feature.transaction.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.transaction.model.Category
import com.fintrack.shared.feature.transaction.model.Transaction
import kotlinx.datetime.LocalDate

@Composable
fun TransactionsListCard(transactionsResult: Result<List<Transaction>>) {
    when (transactionsResult) {
        is Result.Loading -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        is Result.Error -> {
            val message = transactionsResult.exception.message ?: "Unknown error"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: $message", color = Color.Red)
                }
            }
        }

        is Result.Success -> {
            val transactions = transactionsResult.data
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    RecentTransactionsHeader()

                    Spacer(modifier = Modifier.height(8.dp))

                    transactions.forEachIndexed { index, transaction ->
                        TransactionRow(transaction)

                        if (index < transactions.lastIndex) {
                            HorizontalDivider(
                                Modifier.padding(horizontal = 16.dp),
                                0.5.dp,
                                Color.LightGray
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun RecentTransactionsHeader() {
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
        Text(
            text = "View All",
            fontSize = 14.sp,
            color = GreenIncome
        )
    }
}

@Composable
fun TransactionRow(transaction: Transaction) {
    // Map string name to Category object
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = category.toIcon(),
                contentDescription = category.name,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(12.dp))
                    .padding(12.dp),
                tint = Color.Black
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(text = category.name, fontWeight = FontWeight.SemiBold)
                transaction.description?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = it, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${if (isExpense) "-" else "+"}${transaction.amount.toInt()}",
                color = if (isExpense) PinkExpense else GreenIncome,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = transaction.dateTime.date.toShortMonthDay(),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

