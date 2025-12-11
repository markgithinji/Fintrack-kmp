package com.fintrack.shared.feature.transaction.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.example.compose.GreenIncome
import com.example.compose.PinkExpense
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.summary.domain.model.DaySummary
import com.fintrack.shared.feature.summary.domain.model.OverviewSummary
import com.fintrack.shared.feature.core.util.shortDayName
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import kotlinx.datetime.until
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

@Composable
fun IncomeExpensesOverview(overviewResult: Result<OverviewSummary>) {
    var selectedPeriod by remember { mutableStateOf(OverviewPeriod.Weekly) }
    var expanded by remember { mutableStateOf(false) }

    when (overviewResult) {
        is Result.Loading -> {
            OverviewLoadingState(
                selectedPeriod = selectedPeriod,
                expanded = expanded,
                onExpandedChange = { expanded = it },
                onPeriodSelected = { period -> selectedPeriod = period }
            )
        }

        is Result.Error -> {
            OverviewErrorState(
                selectedPeriod = selectedPeriod,
                expanded = expanded,
                onExpandedChange = { expanded = it },
                onPeriodSelected = { period -> selectedPeriod = period }
            )
        }

        is Result.Success -> {
            OverviewSuccessState(
                overview = overviewResult.data,
                selectedPeriod = selectedPeriod,
                expanded = expanded,
                onExpandedChange = { expanded = it },
                onPeriodSelected = { period -> selectedPeriod = period }
            )
        }
    }
}

@Composable
private fun OverviewLoadingState(
    selectedPeriod: OverviewPeriod,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onPeriodSelected: (OverviewPeriod) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        OverviewHeader(
            selectedPeriod = selectedPeriod,
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            onPeriodSelected = onPeriodSelected
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Show loading chart based on selected period
        when (selectedPeriod) {
            OverviewPeriod.Weekly -> LoadingBarChart()
            OverviewPeriod.Monthly -> LoadingLineChart()
        }
    }
}

@Composable
private fun OverviewErrorState(
    selectedPeriod: OverviewPeriod,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onPeriodSelected: (OverviewPeriod) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        OverviewHeader(
            selectedPeriod = selectedPeriod,
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            onPeriodSelected = onPeriodSelected
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Failed to load overview",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun OverviewSuccessState(
    overview: OverviewSummary,
    selectedPeriod: OverviewPeriod,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onPeriodSelected: (OverviewPeriod) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        OverviewHeader(
            selectedPeriod = selectedPeriod,
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            onPeriodSelected = onPeriodSelected
        )

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedPeriod) {
            OverviewPeriod.Weekly -> {
                val weeklyData = overview.weeklyOverview.map {
                    val dayName = LocalDate.parse(it.date).shortDayName()
                    dayName to (it.income to it.expense)
                }
                BarChart(data = weeklyData, modifier = Modifier.padding(16.dp))
            }

            OverviewPeriod.Monthly -> {
                MonthlyLineChartDefault(monthly = overview.monthlyOverview)
            }
        }
    }
}

@Composable
private fun OverviewHeader(
    selectedPeriod: OverviewPeriod,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onPeriodSelected: (OverviewPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                "Overview",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).background(GreenIncome))
                Text(
                    " Income",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Box(Modifier.size(10.dp).background(PinkExpense))
                Text(
                    " Expenses",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box {
            Row(
                modifier = Modifier.clickable { onExpandedChange(true) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    selectedPeriod.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select period",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Weekly",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onPeriodSelected(OverviewPeriod.Weekly)
                        onExpandedChange(false)
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            "Monthly",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onPeriodSelected(OverviewPeriod.Monthly)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
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
    val fullData = remember(data) {
        weekDays.map { day ->
            data.find { it.first == day } ?: (day to (0.0 to 0.0))
        }
    }

    val maxTotal = remember(fullData) {
        fullData.maxOfOrNull { it.second.first + it.second.second } ?: 1.0
    }

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
                val text = remember(value) {
                    if (value >= 1000) {
                        val kValue = (value / 100).toInt() / 10.0
                        "${kValue}k"
                    } else {
                        value.toInt().toString()
                    }
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                val incomeHeightFraction = remember(values.first, maxTotal) {
                    (values.first / maxTotal).toFloat()
                }
                val expenseHeightFraction = remember(values.second, maxTotal) {
                    (values.second / maxTotal).toFloat()
                }

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
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
    val sorted = remember(monthly) {
        monthly.sortedBy { LocalDate.parse(it.date) }
    }
    val baseDate = remember(sorted) {
        LocalDate.parse(sorted.first().date)
    }

    // Convert to "days since start" → x coordinate
    val incomePoints = remember(sorted, baseDate) {
        sorted.map { day ->
            val date = LocalDate.parse(day.date)
            val x = baseDate.until(date, DateTimeUnit.DAY).toFloat()
            Point(x = x, y = day.income.toFloat())
        }
    }
    val expensePoints = remember(sorted, baseDate) {
        sorted.map { day ->
            val date = day.date.toLocalDate()
            val x = baseDate.until(date, DateTimeUnit.DAY).toFloat()
            Point(x = x, y = day.expense.toFloat())
        }
    }

    val xAxisProperties = remember(sorted) {
        AxisProperties(
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
    }

    val yAxisProperties = remember(incomePoints, expensePoints) {
        AxisProperties(
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
    }

    val lineChartProperties =
        remember(xAxisProperties, yAxisProperties, incomePoints, expensePoints, textMeasurer) {
            LineChartProperties(
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
        }

    LineChart(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        lineChartProperties = lineChartProperties
    )
}

enum class OverviewPeriod {
    Weekly, Monthly
}
