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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.GreenIncome
import com.example.compose.PinkExpense
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.summary.domain.model.OverviewSummary
import com.fintrack.shared.feature.core.util.shortDayName
import kotlinx.datetime.LocalDate

@Composable
fun IncomeExpensesOverview(overviewResult: Result<OverviewSummary>) {
    var selectedPeriod by remember { mutableStateOf(OverviewPeriod.Weekly) }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
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
            .background(Color.Transparent)
    ) {
        OverviewHeader(
            selectedPeriod = selectedPeriod,
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            onPeriodSelected = onPeriodSelected
        )

        Spacer(modifier = Modifier.height(8.dp))

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
            .background(Color.Transparent)
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
            .background(Color.Transparent)
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
                BarChart(data = weeklyData, modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp))
            }

            OverviewPeriod.Monthly -> {
                CustomLineChart(data = overview.monthlyOverview, modifier = Modifier.padding(bottom = 16.dp))
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
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
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
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable { onExpandedChange(true) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    selectedPeriod.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
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
    val totalBarHeight = 180.dp
    val barWidth = 16.dp
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
        // Y-axis labels
        Column(
            modifier = Modifier
                .height(totalBarHeight)
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
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Bars
        Row(
            modifier = Modifier.weight(1f),
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
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    ) {
                        // Income bar
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxHeight(incomeHeightFraction.coerceAtLeast(0.01f))
                                .width(barWidth)
                                .background(GreenIncome, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        )

                        // Expense stacked on income
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxHeight(expenseHeightFraction.coerceAtLeast(0.01f))
                                .width(barWidth)
                                .offset(y = -totalBarHeight * incomeHeightFraction)
                                .background(PinkExpense, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
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

enum class OverviewPeriod {
    Weekly, Monthly
}
