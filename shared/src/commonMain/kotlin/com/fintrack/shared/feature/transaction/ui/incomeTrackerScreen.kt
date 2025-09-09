package com.fintrack.shared.feature.transaction.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintrack.shared.feature.transaction.model.Transaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeTrackerScreen() {
    val viewModel: TransactionViewModel = viewModel()
    val transactions by viewModel.transactions.collectAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    val totalIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
    val currentBalance = totalIncome - totalExpense

    val backgroundGray = Color(0xFFEFEFEF)

    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO */ },
                containerColor = Color.Black,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(60.dp)
            ) {
                // Quick workaround: just show a "+" text instead of Material icon
                Text(
                    text = "+",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(backgroundGray)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { CurrentBalanceCard(currentBalance) }
            item { IncomeExpenseCards(totalIncome, totalExpense) }
            item { IncomeExpensesOverview(transactions) }
            item { RecentTransactionsSection() }
            items(transactions) { tx ->
                TransactionItem(tx)
            }
        }
    }
}


@Composable
fun IncomeExpensesOverview(transactions: List<Transaction>) {
    // Group transactions by day (or week) for the chart
    val weeklyData: List<Pair<String, Pair<Double, Double>>> = transactions
        .groupBy { it.date } // LocalDate
        .map { (date, txs) ->
            val income = txs.filter { it.type == "income" }.sumOf { it.amount }
            val expense = txs.filter { it.type == "expense" }.sumOf { it.amount }
            date.toString() to (income to expense) // convert LocalDate to String
        }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Income & Expenses Overview",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(GreenIncome)
                    )
                    Text(text = " Income", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(PinkExpense)
                    )
                    Text(text = " Expenses", fontSize = 12.sp)
                }
            }
            Text(text = "Weekly", color = Color.Gray) // placeholder for dropdown
        }

        Spacer(modifier = Modifier.height(16.dp))
        BarChart(weeklyData)
    }
}

@Composable
fun BarChart(data: List<Pair<String, Pair<Double, Double>>>) {
    val totalHeight = 200.dp
    val barWidth = 24.dp

    val maxAmount = data.maxOfOrNull { it.second.first + it.second.second } ?: 1.0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (label, values) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .height(totalHeight * ((values.first + values.second) / maxAmount).toFloat())
                        .width(barWidth)
                ) {
                    // Income Bar (bottom)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .height(totalHeight * (values.first / maxAmount).toFloat())
                            .width(barWidth)
                            .clip(RoundedCornerShape(4.dp))
                            .background(GreenIncome)
                    )
                    // Expense Bar (top)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .height(totalHeight * (values.second / maxAmount).toFloat())
                            .width(barWidth)
                            .clip(RoundedCornerShape(4.dp))
                            .background(PinkExpense)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = label, fontSize = 12.sp)
            }
        }
    }
}


@Composable
fun RecentTransactionsSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
            color = GreenIncome // highlight color for clickable text
        )
    }
}


// Updated card composables
@Composable
fun CurrentBalanceCard(balance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.align(Alignment.CenterStart)) {
                Text(
                    text = "Bank Account",
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = "Current Balance",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Format balance safely
                val formattedBalance = remember(balance) {
                    balance.toLong()
                        .toString()
                        .reversed()
                        .chunked(3)
                        .joinToString(",")
                        .reversed()
                }

                Text(
                    text = "KSh $formattedBalance",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = LightGray),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(text = "Change Account", color = Color.White)
            }
        }
    }
}


val GreenIncome = Color(0xFF1FC287) // green for income
val PinkExpense = Color(0xFFE27C94)

@Composable
fun IncomeExpenseCards(totalIncome: Double, totalExpense: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InfoCard(
            title = "Total Income",
            amount = "KSh ${formatAmount(totalIncome)}",
            iconColor = GreenIncome,
            modifier = Modifier.weight(1f),
            icon = {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(GreenIncome)
                )
            }
        )
        InfoCard(
            title = "Total Expense",
            amount = "KSh ${formatAmount(totalExpense)}",
            iconColor = PinkExpense,
            modifier = Modifier.weight(1f),
            icon = {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(PinkExpense)
                )
            }
        )
    }
}

@Composable
fun InfoCard(
    title: String,
    amount: String,
    icon: (@Composable () -> Unit)? = null,   // make it nullable slot
    iconColor: Color = Color.Black,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            icon?.let {
                it()
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(text = title, fontSize = 14.sp)
            Text(text = amount, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}


fun formatAmount(value: Double): String {
    return value.toLong()
        .toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
}
