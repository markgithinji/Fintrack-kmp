package com.fintrack.shared.feature.transaction.ui.transactionlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.GreenIncome
import com.example.compose.PinkExpense
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.settings.ui.toCurrencyString
import com.fintrack.shared.feature.summary.domain.model.TransactionCountSummary


@Composable
fun TransactionCountHeaderCard(
    transactionCounts: Result<TransactionCountSummary>,
    isIncome: Boolean?,
    hasTransactionCost: Boolean? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        when (transactionCounts) {
            is Result.Loading -> TransactionCountLoadingState()
            is Result.Error -> TransactionCountErrorState()
            is Result.Success -> TransactionCountSuccessState(
                counts = transactionCounts.data,
                isIncome = isIncome,
                hasTransactionCost = hasTransactionCost
            )
        }
    }
}

@Composable
private fun TransactionCountLoadingState() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = GreenIncome
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Loading summary...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TransactionCountErrorState() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Failed to load counts",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun TransactionCountSuccessState(
    counts: TransactionCountSummary,
    isIncome: Boolean?,
    hasTransactionCost: Boolean? = null
) {
    val themeColor = when {
        hasTransactionCost == true -> MaterialTheme.colorScheme.tertiary
        isIncome == true -> GreenIncome
        isIncome == false -> PinkExpense
        else -> MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(themeColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                contentDescription = null,
                tint = themeColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Column {
            Text(
                text = when {
                    hasTransactionCost == true -> "Transaction Fees"
                    isIncome == true -> "Income Overview"
                    isIncome == false -> "Expense Overview"
                    else -> "All Transactions"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            val countText = remember(counts, isIncome, hasTransactionCost) {
                when {
                    hasTransactionCost == true -> "${counts.totalTransactions} transactions with fees"
                    isIncome == true -> "${counts.totalIncomeTransactions} total items"
                    isIncome == false -> "${counts.totalExpenseTransactions} total items"
                    else -> "${counts.totalTransactions} total items"
                }
            }

            Text(
                text = countText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (counts.totalTransactionCost > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Total Fees: ${counts.totalTransactionCost.toCurrencyString()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = themeColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}