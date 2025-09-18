package com.fintrack.shared.feature.transaction.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintrack.shared.feature.transaction.data.DaySummary
import com.fintrack.shared.feature.transaction.data.HighlightsSummary
import com.fintrack.shared.feature.transaction.data.OverviewSummary
import com.fintrack.shared.feature.transaction.data.Result
import kotlinx.datetime.LocalDate
import network.chaintech.chartsLib.ui.linechart.model.IntersectionPoint
import network.chaintech.cmpcharts.axis.AxisProperties
import network.chaintech.cmpcharts.common.extensions.formatToSinglePrecision
import network.chaintech.cmpcharts.common.model.Point
import network.chaintech.cmpcharts.common.ui.GridLinesUtil
import network.chaintech.cmpcharts.common.ui.SelectionHighlightPoint
import network.chaintech.cmpcharts.common.ui.SelectionHighlightPopUp
import network.chaintech.cmpcharts.common.ui.ShadowUnderLine
import network.chaintech.cmpcharts.ui.linechart.LineChart
import network.chaintech.cmpcharts.ui.linechart.model.Line
import network.chaintech.cmpcharts.ui.linechart.model.LineChartProperties
import network.chaintech.cmpcharts.ui.linechart.model.LinePlotData
import network.chaintech.cmpcharts.ui.linechart.model.LineStyle


val backgroundGray = Color(0xFFEFEFEF)
val GreenIncome = Color(0xFF1FC287) // green for income
val PinkExpense = Color(0xFFE27C94) // pinkish-red for expense

@Composable
fun IncomeTrackerContent(
    transactionsViewModel: TransactionListViewModel = viewModel(),
    statsViewModel: StatisticsViewModel = viewModel()
) {
    val summaryResult by statsViewModel.highlights.collectAsStateWithLifecycle()
    val transactionsResult by transactionsViewModel.recentTransactions.collectAsStateWithLifecycle()
    val overviewResult by statsViewModel.overview.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        statsViewModel.loadHighlights()
        statsViewModel.loadOverview()
        transactionsViewModel.loadRecentTransactions()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGray),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { CurrentBalanceCard(summaryResult) }
        item { IncomeExpenseCards(summaryResult) }
        item { IncomeExpensesOverview(overviewResult) } // <- new
        item { TransactionsListCard(transactionsResult) }
    }
}


@Composable
fun CurrentBalanceCard(summaryResult: Result<HighlightsSummary>) {
    when (summaryResult) {
        is Result.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(DarkGray),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        is Result.Error -> {
            val message = summaryResult.exception.message ?: "Unknown error"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: $message", color = Color.Red)
            }
        }

        is Result.Success -> {
            val balance = summaryResult.data.balance

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
fun IncomeExpensesOverview(overviewResult: Result<OverviewSummary>) {
    var selectedPeriod by remember { mutableStateOf(OverviewPeriod.Weekly) }
    var expanded by remember { mutableStateOf(false) }

    when (overviewResult) {
        is Result.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is Result.Error -> {
            Text("Error: ${overviewResult.exception.message}", color = Color.Red)
        }

        is Result.Success -> {
            val overview = overviewResult.data

            // --- DEBUG logs ---
            println("DEBUG: Weekly Overview:")
            overview.weeklyOverview.forEach {
                println("Date=${it.date}, Income=${it.income}, Expense=${it.expense}")
            }
            println("DEBUG: Monthly Overview:")
            overview.monthlyOverview.forEach {
                println("Date=${it.date}, Income=${it.income}, Expense=${it.expense}")
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
            ) {
                // Header with dropdown
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text("Overview", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(10.dp).background(GreenIncome))
                            Text(" Income", fontSize = 12.sp)
                            Spacer(Modifier.width(8.dp))
                            Box(Modifier.size(10.dp).background(PinkExpense))
                            Text(" Expenses", fontSize = 12.sp)
                        }
                    }

                    Box {
                        Row(
                            modifier = Modifier.clickable { expanded = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedPeriod.name, color = Color.Gray, fontSize = 12.sp)
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select period",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Weekly") },
                                onClick = {
                                    selectedPeriod = OverviewPeriod.Weekly
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Monthly") },
                                onClick = {
                                    selectedPeriod = OverviewPeriod.Monthly
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (selectedPeriod) {
                    OverviewPeriod.Weekly -> {
                        // Map date to weekday
                        val weeklyData = overview.weeklyOverview.map {
                            val dayName = LocalDate.parse(it.date).shortDayName()
                            dayName to (it.income to it.expense)
                        }
                        println("DEBUG: Weekly chart mapped data: $weeklyData")
                        BarChart(data = weeklyData, modifier = Modifier.padding(16.dp))
                    }

                    OverviewPeriod.Monthly -> {
                        println("DEBUG: Monthly chart data: ${overview.monthlyOverview}")
                        MonthlyLineChartDefault(monthly = overview.monthlyOverview)
                    }
                }
            }
        }
    }
}


enum class OverviewPeriod {
    Weekly, Monthly
}


@Composable
fun BarChart(
    data: List<Pair<String, Pair<Double, Double>>>,
    modifier: Modifier = Modifier
) {
    val totalBarHeight = 200.dp
    val barWidth = 24.dp

    // Ensure all days are present
    val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val fullData = weekDays.map { day ->
        data.find { it.first == day } ?: (day to (0.0 to 0.0))
    }

    val maxTotal = fullData.maxOfOrNull { it.second.first + it.second.second } ?: 1.0

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
                    val kValue = (value / 100).toInt() / 10.0
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
            fullData.forEach { (label, values) ->
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
fun MonthlyLineChartDefault(monthly: List<DaySummary>, modifier: Modifier = Modifier) {
    if (monthly.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val steps = 5

    // Map domain data to Points
    val incomePoints = monthly.mapIndexed { index, day -> Point(x = index.toFloat(), y = day.income.toFloat()) }
    val expensePoints = monthly.mapIndexed { index, day -> Point(x = index.toFloat(), y = day.expense.toFloat()) }

    val labelInterval = if (monthly.size > 15) 3 else 1

    val xAxisProperties = AxisProperties(
        font = FontFamily.SansSerif,
        stepSize = 30.dp,
        topPadding = 105.dp,
        labelColor = Color.Black,
        lineColor = Color.Black,
        stepCount = monthly.size - 1,
        labelFormatter = { i ->
            if (i % labelInterval == 0) monthly[i].date.take(5) else "" // e.g., "01-09"
        },
        labelPadding = 15.dp
    )


    val yAxisProperties = AxisProperties(
        font = FontFamily.SansSerif,
        stepCount = steps,
        labelColor = Color.Black,
        lineColor = Color.Black,
        labelPadding = 20.dp,
        labelFormatter = { i ->
            val yMin = (incomePoints + expensePoints).minOf { it.y }
            val yMax = (incomePoints + expensePoints).maxOf { it.y }
            val yScale = (yMax - yMin) / steps
            ((i * yScale) + yMin).formatToSinglePrecision()
        }
    )

    val lineChartProperties = LineChartProperties(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = incomePoints,
                    lineStyle = LineStyle(color = Color(0xFF4CAF50)), // green
                    intersectionPoint = IntersectionPoint(color = Color(0xFF388E3C)),
                    selectionHighlightPoint = SelectionHighlightPoint(color = Color(0xFFFF4081)), // pink
                    shadowUnderLine = ShadowUnderLine(),
                    selectionHighlightPopUp = SelectionHighlightPopUp(
                        textMeasurer = textMeasurer,
                        backgroundColor = Color(0xFFFF4081),
                        labelColor = Color.White,
                        labelTypeface = FontWeight.Bold
                    )
                ),
                Line(
                    dataPoints = expensePoints,
                    lineStyle = LineStyle(color = Color(0xFF9C27B0)), // purple
                    intersectionPoint = IntersectionPoint(color = Color(0xFF7B1FA2)),
                    selectionHighlightPoint = SelectionHighlightPoint(color = Color(0xFFFF4081)),
                    shadowUnderLine = ShadowUnderLine(),
                    selectionHighlightPopUp = SelectionHighlightPopUp(
                        textMeasurer = textMeasurer,
                        backgroundColor = Color(0xFFFF4081),
                        labelColor = Color.White,
                        labelTypeface = FontWeight.Bold
                    )
                )
            )
        ),
        xAxisProperties = xAxisProperties,
        yAxisProperties = yAxisProperties,
        gridLines = GridLinesUtil(color = Color.LightGray)
    )

    LineChart(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        lineChartProperties = lineChartProperties
    )
}



@Composable
fun IncomeExpenseCards(summaryResult: Result<HighlightsSummary>) {
    when (summaryResult) {
        is Result.Loading -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        is Result.Error -> {
            val message = summaryResult.exception.message ?: "Unknown error"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: $message", color = Color.Red)
            }
        }

        is Result.Success -> {
            val totalIncome = summaryResult.data.income
            val totalExpense = summaryResult.data.expense

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