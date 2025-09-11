package com.fintrack.shared.feature.transaction.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintrack.shared.feature.transaction.data.Result
import com.fintrack.shared.feature.transaction.model.Transaction
import kotlinx.datetime.LocalDate


val backgroundGray = Color(0xFFEFEFEF)
val GreenIncome = Color(0xFF1FC287) // green for income
val PinkExpense = Color(0xFFE27C94) // pinkish-red for expense

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeTrackerScreen(
    viewModel: TransactionViewModel = viewModel(),
    onAddClicked: () -> Unit,
    onStatisticsClicked: () -> Unit
) {
    val transactionsResult by viewModel.transactions.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    when (transactionsResult) {
        is Result.Loading -> {
            // Show a loading indicator
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundGray),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is Result.Error -> {
            // Show an error message
            val message = (transactionsResult as Result.Error).exception.message ?: "Unknown error"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundGray),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: $message", color = Color.Red)
            }
        }

        is Result.Success -> {
            val transactions = (transactionsResult as Result.Success).data
            val totalIncome = transactions.filter { it.isIncome }.sumOf { it.amount }
            val totalExpense = transactions.filter { !it.isIncome }.sumOf { it.amount }
            val currentBalance = totalIncome - totalExpense

            Scaffold(
                topBar = { TopBar() },
                bottomBar = { BottomBar() },
                floatingActionButton = {
                    FloatingActionButton(
                        modifier = Modifier
                            .size(60.dp)
                            .offset(y = 60.dp),
                        onClick = onAddClicked,
                        containerColor = Color.Black,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Transaction",
                            tint = Color.White
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
                    item { TransactionsListCard(transactions) }
                }
            }
        }
    }
}


@Composable
fun CurrentBalanceCard(balance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            LowerRightWavesBackground(modifier = Modifier.matchParentSize())
            // Top row with label + button
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = "Bank",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Bank Account",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                Button(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.buttonColors(containerColor = LightGray),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(26.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(
                        text = "Change Account",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Bottom section
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 8.dp)
            ) {
                val formattedBalance = remember(balance) {
                    balance.toLong()
                        .toString()
                        .reversed()
                        .chunked(3)
                        .joinToString(",")
                        .reversed()
                }
                Text(
                    text = "Current Balance",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "KSh $formattedBalance",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun IncomeExpenseCards(totalIncome: Double, totalExpense: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InfoCard(
            title = "Total Income",
            amount = "KSh ${formatAmount(totalIncome)}",
            modifier = Modifier.weight(1f),
            icon = {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(GreenIncome),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Income",
                        tint = Color.White,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(135f)
                    )
                }
            }
        )

        InfoCard(
            title = "Total Expense",
            amount = "KSh ${formatAmount(totalExpense)}",
            modifier = Modifier.weight(1f),
            icon = {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(PinkExpense),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Expense",
                        tint = Color.White,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(-135f)

                    )
                }
            }
        )
    }
}


fun LocalDate.shortDayName(): String {
    // 0 = Monday ... 6 = Sunday
    return when (this.dayOfWeek.ordinal) {
        0 -> "Mon"; 1 -> "Tue"; 2 -> "Wed"; 3 -> "Thu"
        4 -> "Fri"; 5 -> "Sat"; 6 -> "Sun"
        else -> ""
    }
}

@Composable
fun IncomeExpensesOverview(transactions: List<Transaction>) {
    // Group transactions by date and compute daily income/expense
    val weeklyData = transactions
        .groupBy { it.date }
        .map { (date, txs) ->
            val income = txs.filter { it.isIncome }.sumOf { it.amount }
            val expense = txs.filter { !it.isIncome }.sumOf { it.amount }
            date.shortDayName() to (income to expense)
        }
        .sortedBy { (label, _) ->
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").indexOf(label)
        }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "Overview",
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

            Row(verticalAlignment = Alignment.Top) {
                Text(text = "Weekly", color = Color.Gray, fontSize = 12.sp)
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select period",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        BarChart(
            data = weeklyData,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}


@Composable
fun BarChart(
    data: List<Pair<String, Pair<Double, Double>>>,
    modifier: Modifier = Modifier
) {
    val totalBarHeight = 200.dp
    val barWidth = 24.dp
    val maxTotal = data.maxOfOrNull { it.second.first + it.second.second } ?: 1.0

    // number of Y-axis levels
    val levels = 5
    val step = maxTotal / levels

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        // Y-axis
        Column(
            modifier = Modifier
                .height(totalBarHeight) // same height as bars
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            for (i in levels downTo 0) {
                val value = step * i
                val text = if (value >= 1000) {
                    val kValue = (value / 100).toInt() / 10.0  // divide by 1000 and keep 1 decimal
                    "${kValue}k"
                } else {
                    value.toInt().toString()
                }
                Text(
                    text = text,
                    fontSize = 10.sp
                )
            }
        }

        // Bars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { (label, values) ->
                val incomeHeightFraction = (values.first / maxTotal).toFloat()
                val expenseHeightFraction = (values.second / maxTotal).toFloat()

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .width(barWidth)
                            .height(totalBarHeight)
                    ) {
                        // Income bar
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxHeight(incomeHeightFraction)
                                .width(barWidth)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(GreenIncome)
                        )

                        // Expense stacked on income
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxHeight(expenseHeightFraction)
                                .width(barWidth)
                                .offset(y = -totalBarHeight * incomeHeightFraction)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(PinkExpense)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = label, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    amount: String,
    icon: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.height(70.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Column for title + amount
            Column {
                Text(text = title, fontSize = 12.sp)
                Text(text = amount, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
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