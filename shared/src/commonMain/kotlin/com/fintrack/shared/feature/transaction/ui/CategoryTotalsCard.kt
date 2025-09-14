package com.fintrack.shared.feature.transaction.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.shared.feature.transaction.data.CategorySummary
import com.fintrack.shared.feature.transaction.data.DistributionSummary
import com.fintrack.shared.feature.transaction.data.Result

// --- Segment Colors ---
val SegmentColors = listOf(
    Color(0xFF4E79A7), // Blue
    Color(0xFFF28E2B), // Orange
    Color(0xFFE15759), // Red
    Color(0xFF76B7B2), // Teal
    Color(0xFFFF9DA7)  // Pink / Others
)

@Composable
fun CategoryTotalsCardWithTabs(
    tabType: TabType,
    period: Period,
    distributionResult: Result<DistributionSummary>,
    availableWeeks: List<String> = emptyList(),
    availableMonths: List<String> = emptyList(),
    onWeekSelected: (String) -> Unit = {},
    onMonthSelected: (String) -> Unit = {},
    onPeriodSelected: (Period) -> Unit = {}
) {
    when (distributionResult) {
        is Result.Loading -> Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        is Result.Error -> {
            val message = distributionResult.exception.message ?: "Unknown error"
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) { Text("Error: $message", color = Color.Red) }
        }

        is Result.Success -> {
            val data = distributionResult.data
            val categories = when (tabType) {
                is TabType.Income -> data.incomeCategories
                is TabType.Expense -> data.expenseCategories
            }

            val weeklyMap =
                if (period is Period.Week) mapOf(period.code to categories) else emptyMap()
            val monthlyMap =
                if (period is Period.Month) mapOf(period.code to categories) else emptyMap()

            CategoryContent(
                weeklySummary = weeklyMap,
                monthlySummary = monthlyMap,
                selectedPeriod = period,
                availableWeeks = availableWeeks,
                availableMonths = availableMonths,
                onWeekSelected = onWeekSelected,
                onMonthSelected = onMonthSelected,
                onPeriodSelected = onPeriodSelected
            )
        }
    }
}

@Composable
private fun CategoryContent(
    weeklySummary: Map<String, List<CategorySummary>>,
    monthlySummary: Map<String, List<CategorySummary>>,
    selectedPeriod: Period,
    availableWeeks: List<String>,
    availableMonths: List<String>,
    onWeekSelected: (String) -> Unit,
    onMonthSelected: (String) -> Unit,
    onPeriodSelected: (Period) -> Unit,
    title: String = "Spending Distribution"
) {
    val categories = when (selectedPeriod) {
        is Period.Week -> weeklySummary[selectedPeriod.code] ?: emptyList()
        is Period.Month -> monthlySummary[selectedPeriod.code] ?: emptyList()
    }

    val totalAmount = categories.sumOf { it.total }.toFloat()
    val categorySums = categories.map { it.category to it.total.toFloat() }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        CategoryHeader(title = title)

        PeriodSelector(
            selectedPeriod = selectedPeriod,
            availableWeeks = availableWeeks,
            availableMonths = availableMonths,
            onPeriodSelected = onPeriodSelected,
            onWeekSelected = onWeekSelected,
            onMonthSelected = onMonthSelected
        )

        DonutChartSection(categorySums, totalAmount)
        Spacer(Modifier.height(16.dp))
        CategoryList(categories = categorySums, totalAmount = totalAmount, segmentColors = SegmentColors)
    }
}


@Composable
private fun CategoryHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun PeriodSwitcher(
    selectedPeriod: Period,
    availableWeeks: List<String>,
    availableMonths: List<String>,
    onPeriodSelected: (Period) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TimeSpan.entries.forEach { span ->
            val isSelected = (span == TimeSpan.WEEK && selectedPeriod is Period.Week) ||
                    (span == TimeSpan.MONTH && selectedPeriod is Period.Month)
            val bg = if (isSelected) Color(0xFF2D2D2D) else Color.Transparent
            val fg = if (isSelected) Color.White else Color.Black

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(bg)
                    .clickable {
                        when (span) {
                            TimeSpan.WEEK -> availableWeeks.firstOrNull()
                                ?.let { onPeriodSelected(Period.Week(it)) }

                            TimeSpan.MONTH -> availableMonths.firstOrNull()
                                ?.let { onPeriodSelected(Period.Month(it)) }

                            TimeSpan.YEAR -> TODO()
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) { Text(text = span.displayName, fontWeight = FontWeight.SemiBold, color = fg) }
        }
    }
}

@Composable
fun PeriodSelector(
    selectedPeriod: Period,
    availableWeeks: List<String>,
    availableMonths: List<String>,
    onPeriodSelected: (Period) -> Unit,
    onWeekSelected: (String) -> Unit,
    onMonthSelected: (String) -> Unit
) {
    Column {
        // --- Switcher Row (Week / Month) ---
        PeriodSwitcher(
            selectedPeriod = selectedPeriod,
            availableWeeks = availableWeeks,
            availableMonths = availableMonths,
            onPeriodSelected = onPeriodSelected
        )

        Spacer(Modifier.height(8.dp))

        // --- Dropdown for the selected period ---
        when (selectedPeriod) {
            is Period.Week -> if (availableWeeks.isNotEmpty()) {
                DropdownSelector(
                    options = availableWeeks,
                    selected = selectedPeriod.code,
                    onSelected = onWeekSelected,
                    placeholder = "Select Week"
                )
            }

            is Period.Month -> if (availableMonths.isNotEmpty()) {
                DropdownSelector(
                    options = availableMonths,
                    selected = selectedPeriod.code,
                    onSelected = onMonthSelected,
                    placeholder = "Select Month"
                )
            }
        }
    }
}
@Composable
fun DropdownSelector(
    options: List<String>,
    selected: String?,
    onSelected: (String) -> Unit,
    placeholder: String = "Select"
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray.copy(alpha = 0.2f))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(selected ?: placeholder)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
fun CategoryList(
    categories: List<Pair<String, Float>>,
    totalAmount: Float,
    segmentColors: List<Color>
) {
    val sortedCategorySums = categories.sortedByDescending { it.second }
    val categoryColors = sortedCategorySums.map { category ->
        val chartIndex = categories.indexOfFirst { it.first == category.first }
        if (chartIndex in 0..3) segmentColors[chartIndex] else segmentColors.last()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F4F4))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            sortedCategorySums.forEachIndexed { index, (categoryName, amount) ->
                val percent = if (totalAmount > 0) (amount / totalAmount * 100).toInt() else 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = categoryColors[index],
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = categoryName,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.width(160.dp)
                    )

                    Box(
                        modifier = Modifier.width(100.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = formatCurrencyKmp(amount.toDouble()),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "($percent%)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.DarkGray.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}


enum class TimeSpan(val displayName: String) {
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}

