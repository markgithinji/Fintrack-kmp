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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintrack.shared.feature.account.domain.Account
import com.fintrack.shared.feature.account.ui.AccountsViewModel
import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.summary.domain.CategoryComparison
import com.fintrack.shared.feature.summary.domain.DaySummary
import com.fintrack.shared.feature.summary.domain.StatisticsSummary
import com.fintrack.shared.feature.summary.domain.OverviewSummary
import com.fintrack.shared.feature.summary.ui.StatisticsViewModel
import com.fintrack.shared.feature.transaction.model.AccountIcon
import com.fintrack.shared.feature.transaction.model.Category
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import kotlinx.datetime.until
import network.chaintech.chartsLib.ui.linechart.model.IntersectionPoint
import network.chaintech.cmpcharts.axis.AxisProperties
import network.chaintech.cmpcharts.common.components.Legends
import network.chaintech.cmpcharts.common.extensions.formatNumber
import network.chaintech.cmpcharts.common.extensions.formatToSinglePrecision
import network.chaintech.cmpcharts.common.extensions.getMaxElementInYAxis
import network.chaintech.cmpcharts.common.model.LegendLabel
import network.chaintech.cmpcharts.common.model.LegendsConfig
import network.chaintech.cmpcharts.common.model.PlotType
import network.chaintech.cmpcharts.common.model.Point
import network.chaintech.cmpcharts.common.ui.GridLinesUtil
import network.chaintech.cmpcharts.common.ui.SelectionHighlightPoint
import network.chaintech.cmpcharts.common.ui.SelectionHighlightPopUp
import network.chaintech.cmpcharts.common.ui.ShadowUnderLine
import network.chaintech.cmpcharts.ui.barchart.StackedBarChart
import network.chaintech.cmpcharts.ui.barchart.config.BarChartStyle
import network.chaintech.cmpcharts.ui.barchart.config.BarData
import network.chaintech.cmpcharts.ui.barchart.config.BarPlotData
import network.chaintech.cmpcharts.ui.barchart.config.GroupBar
import network.chaintech.cmpcharts.ui.barchart.config.GroupBarChartData
import network.chaintech.cmpcharts.ui.barchart.config.SelectionHighlightData
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
    accountsViewModel: AccountsViewModel = viewModel(),
    transactionsViewModel: TransactionListViewModel = viewModel(),
    statsViewModel: StatisticsViewModel = viewModel(),
    onCardClick: (accountId: Int, isIncome: Boolean?) -> Unit
) {
    val accountsResult by accountsViewModel.accounts.collectAsStateWithLifecycle()
    val selectedAccountResult by accountsViewModel.selectedAccount.collectAsStateWithLifecycle()
    val transactionsResult by transactionsViewModel.recentTransactions.collectAsStateWithLifecycle()
    val overviewResult by statsViewModel.overview.collectAsStateWithLifecycle()
    val categoryComparisonResult by statsViewModel.categoryComparisons.collectAsStateWithLifecycle()

    // Reload initial accounts
    LaunchedEffect(Unit) {
        accountsViewModel.reloadAccounts()
    }

    // Reload dependent data whenever the selected account changes
    LaunchedEffect(selectedAccountResult) {
        val accountId = (selectedAccountResult as? Result.Success)?.data?.id
        if (accountId != null) {
            transactionsViewModel.loadRecentTransactions(accountId)
            statsViewModel.loadOverview(accountId)
            statsViewModel.loadCategoryComparisons(accountId)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGray),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CurrentBalanceCardWrapper(
                accountsResult = accountsResult,
                selectedAccountResult = selectedAccountResult,
                onAccountSelected = { accountId ->
                    accountsViewModel.selectAccount(accountId)
                }
            )
        }

        item {
            val accountId = (selectedAccountResult as? Result.Success)?.data?.id ?: return@item
            IncomeExpenseCards(
                accountResult = selectedAccountResult,
                onCardClick = { isIncome ->
                    onCardClick(accountId, isIncome)
                }
            )
        }

        item { IncomeExpensesOverview(overviewResult) }
        item { CategoryComparisonCard(categoryComparisonResult) }
        item {
            TransactionsListCard(
                transactionsResult = transactionsResult,
                onViewAllClick = {
                    val accountId = (selectedAccountResult as? Result.Success)?.data?.id ?: return@TransactionsListCard
                    onCardClick(accountId, null)
                }
            )
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
fun MonthlyLineChartDefault(
    monthly: List<DaySummary>,
    modifier: Modifier = Modifier
) {
    if (monthly.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val steps = 5

    // --- Ensure correct order by full date ---
    val sorted = monthly.sortedBy { LocalDate.parse(it.date) }
    val baseDate = LocalDate.parse(sorted.first().date)

    // Convert to "days since start" → x coordinate
    val incomePoints = sorted.map { day ->
        val date = LocalDate.parse(day.date)
        val x = baseDate.until(date, DateTimeUnit.DAY).toFloat()
        Point(x = x, y = day.income.toFloat())
    }
    val expensePoints = sorted.map { day ->
        val date = day.date.toLocalDate()
        val x = baseDate.until(date, DateTimeUnit.DAY).toFloat()
        Point(x = x, y = day.expense.toFloat())
    }

    val xAxisProperties = AxisProperties(
        font = FontFamily.SansSerif,
        stepSize = 30.dp,
        topPadding = 105.dp,
        labelColor = Color.Black,
        lineColor = Color.Black,
        stepCount = sorted.size - 1,
        labelFormatter = { i ->
            val safeIndex = i.coerceAtMost(sorted.lastIndex)
            // just day-of-month for label
            sorted[safeIndex].date.split("-").last()
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
            val value = ((i * yScale) + yMin)

            if (value >= 1000f) {
                "${(value / 1000f).formatToSinglePrecision()}k"
            } else {
                value.formatToSinglePrecision()
            }
        }
    )

    val lineChartProperties = LineChartProperties(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = incomePoints,
                    lineStyle = LineStyle(
                        color = GreenIncome,
                        width = 3f
                    ),
                    intersectionPoint = IntersectionPoint(color = GreenIncome),
                    selectionHighlightPoint = SelectionHighlightPoint(color = GreenIncome),
                    shadowUnderLine = ShadowUnderLine(GreenIncome.copy(alpha = 0.2f)),
                    selectionHighlightPopUp = SelectionHighlightPopUp(
                        textMeasurer = textMeasurer,
                        backgroundColor = GreenIncome,
                        labelColor = Color.White,
                        labelTypeface = FontWeight.Bold
                    )
                ),
                Line(
                    dataPoints = expensePoints,
                    lineStyle = LineStyle(
                        color = PinkExpense,
                        width = 3f
                    ),
                    intersectionPoint = IntersectionPoint(color = PinkExpense),
                    selectionHighlightPoint = SelectionHighlightPoint(color = PinkExpense),
                    shadowUnderLine = ShadowUnderLine(PinkExpense.copy(alpha = 0.2f)),
                    selectionHighlightPopUp = SelectionHighlightPopUp(
                        textMeasurer = textMeasurer,
                        backgroundColor = PinkExpense,
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
fun CategoryComparisonCard(
    categoryComparisonResult: Result<List<CategoryComparison>>?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Category Comparison",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(12.dp))

            when (categoryComparisonResult) {
                is Result.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF6200EE))
                    }
                }

                is Result.Error -> {
                    Text(
                        text = "Error: ${categoryComparisonResult.exception.message ?: "Unknown error"}",
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }

                is Result.Success -> {
                    categoryComparisonResult.data.forEachIndexed { index, comparison ->
                        val category = Category.fromName(
                            comparison.category,
                            comparison.currentTotal < 0 || comparison.previousTotal < 0
                        )
                        val icon = category.toIcon()
                        val bgColor = category.toColor().copy(alpha = 0.15f)
                        val iconTint = category.toColor()

                        val positive = comparison.changePercentage >= 0
                        val arrow = if (positive) "↑" else "↓"
                        val changeColor = if (positive) Color(0xFF4CAF50) else Color.Red

                        val periodLabel = when (comparison.period.lowercase()) {
                            "weekly" -> "week"
                            "monthly" -> "month"
                            "yearly" -> "year"
                            else -> comparison.period
                        }

                        val changeText = if (positive) {
                            "${comparison.changePercentage.formatToSinglePrecision()}% more than last $periodLabel"
                        } else {
                            "${(comparison.changePercentage * -1).formatToSinglePrecision()}% less than last $periodLabel"
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.width(150.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(bgColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = comparison.category,
                                        tint = iconTint,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = comparison.category,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Box(
                                modifier = Modifier.width(180.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = arrow,
                                        color = changeColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = changeText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = changeColor
                                    )
                                }
                            }
                        }

                        if (index != categoryComparisonResult.data.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .fillMaxWidth(0.85f)
                                    .align(Alignment.CenterHorizontally),
                                thickness = 0.6.dp,
                                color = Color(0xFFE0E0E0)
                            )
                        }
                    }
                }

                null -> {
                    Text("No data", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }
}





fun Double.formatToSinglePrecision(): String =
    if (this % 1.0 == 0.0) {
        this.toInt().toString()
    } else {
        val multiplied = (this * 10).toInt()
        val rounded = multiplied.toDouble() / 10
        rounded.toString()
    }


@Composable
fun IncomeExpenseCards(
    accountResult: Result<Account>?,
    onCardClick: (isIncome: Boolean) -> Unit
) {
    when (accountResult) {
        null, is Result.Loading -> {
            Row(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        is Result.Error -> {
            val message = accountResult.exception.message ?: "Unknown error"
            Box(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: $message", color = Color.Red)
            }
        }

        is Result.Success -> {
            val account = accountResult.data
            val totalIncome = account.income ?: 0.0
            val totalExpense = account.expense ?: 0.0

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoCard(
                    title = "Total Income",
                    amount = "KSh ${formatAmount(totalIncome)}",
                    isIncomeCard = true,
                    onClick = onCardClick,
                    modifier = Modifier.weight(1f)
                )

                InfoCard(
                    title = "Total Expense",
                    amount = "KSh ${formatAmount(totalExpense)}",
                    isIncomeCard = false,
                    onClick = onCardClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}



@Composable
fun InfoCard(
    title: String,
    amount: String,
    isIncomeCard: Boolean,
    onClick: ((isIncome: Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(70.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke(isIncomeCard) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isIncomeCard) GreenIncome else PinkExpense),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncomeCard) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = if (isIncomeCard) "Income" else "Expense",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp).rotate(if (isIncomeCard) 135f else -135f)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

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