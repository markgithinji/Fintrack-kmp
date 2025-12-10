package com.fintrack.shared.feature.transaction.ui.transactionlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.GreenIncome
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.summary.domain.model.TransactionCountSummary


@Composable
fun TransactionCountHeaderCard(
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
            is Result.Loading -> TransactionCountLoadingState()
            is Result.Error -> TransactionCountErrorState()
            is Result.Success -> TransactionCountSuccessState(
                counts = transactionCounts.data,
                isIncome = isIncome
            )
        }
    }
}

@Composable
private fun TransactionCountLoadingState() {
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

@Composable
private fun TransactionCountErrorState() {
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

@Composable
private fun TransactionCountSuccessState(
    counts: TransactionCountSummary,
    isIncome: Boolean?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val text = remember(counts, isIncome) {
            when (isIncome) {
                true -> "${counts.totalIncomeTransactions} Transactions"
                false -> "${counts.totalExpenseTransactions} Transactions"
                null -> "${counts.totalTransactions} Transactions"
            }
        }

        Text(
            text = text,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}