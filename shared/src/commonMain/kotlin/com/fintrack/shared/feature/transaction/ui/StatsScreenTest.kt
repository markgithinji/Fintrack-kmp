package com.fintrack.shared.feature.transaction.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

//@Composable
//fun SimpleStatisticsScreen(viewModel: TransactionViewModel = viewModel()) {
//    val summary by viewModel.summary.collectAsStateWithLifecycle()
//
//    LaunchedEffect(Unit) { viewModel.loadSummary() }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//
//        Text(
//            text = "Statistics",
//            fontSize = 24.sp,
//            fontWeight = FontWeight.Bold
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//
//        summary?.let { data ->
//
//            Text("Highest Month: ${data.highestMonth?.value ?: "-"} (${formatCurrency(data.highestMonth?.amount ?: 0.0)})")
//            Spacer(modifier = Modifier.height(8.dp))
//            Text("Most Spent Category: ${data.highestCategory?.value ?: "-"} (${formatCurrency(data.highestCategory?.amount ?: 0.0)})")
//            Spacer(modifier = Modifier.height(8.dp))
//            Text("Highest Daily Spending: ${data.highestDay?.value ?: "-"} (${formatCurrency(data.highestDay?.amount ?: 0.0)})")
//            Spacer(modifier = Modifier.height(8.dp))
//            Text("Average Per Day: ${formatCurrency(data.averagePerDay)}")
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Text("Monthly Category Summary:")
//            data.monthlyCategorySummary.forEach { (month, categories) ->
//                Text("Month: $month")
//                categories.forEach { cat ->
//                    Text("- ${cat.category}: ${formatCurrency(cat.total)} (${formatPercentage(cat.percentage)})")
//                }
//                Spacer(modifier = Modifier.height(8.dp))
//            }
//
//        } ?: run {
//            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                CircularProgressIndicator()
//            }
//        }
//    }
//}