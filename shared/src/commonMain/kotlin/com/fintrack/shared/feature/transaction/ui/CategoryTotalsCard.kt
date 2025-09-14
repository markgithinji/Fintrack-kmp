package com.fintrack.shared.feature.transaction.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    availableYears: List<String> = emptyList(),
    onWeekSelected: (String) -> Unit = {},
    onMonthSelected: (String) -> Unit = {},
    onYearSelected: (String) -> Unit = {},
    onPeriodSelected: (Period) -> Unit = {}
) {
    when (distributionResult) {
        is Result.Loading -> Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        is Result.Error -> {
            val message = distributionResult.exception.message ?: "Unknown error"
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: $message", color = Color.Red)
            }
        }

        is Result.Success -> {
            val categories = when (tabType) {
                is TabType.Income -> distributionResult.data.incomeCategories
                is TabType.Expense -> distributionResult.data.expenseCategories
            }

            // Create a map depending on the period type
            val weeklyMap =
                if (period is Period.Week) mapOf(period.code to categories) else emptyMap()
            val monthlyMap =
                if (period is Period.Month) mapOf(period.code to categories) else emptyMap()
            val yearlyMap =
                if (period is Period.Year) mapOf(period.code to categories) else emptyMap()

            CategoryContent(
                weeklySummary = weeklyMap,
                monthlySummary = monthlyMap,
                yearlySummary = yearlyMap,          // added support for year
                selectedPeriod = period,
                availableWeeks = availableWeeks,
                availableMonths = availableMonths,
                availableYears = availableYears,    // pass available years
                onWeekSelected = onWeekSelected,
                onMonthSelected = onMonthSelected,
                onYearSelected = onYearSelected,    // new callback
                onPeriodSelected = onPeriodSelected
            )
        }
    }
}

@Composable
private fun CategoryContent(
    weeklySummary: Map<String, List<CategorySummary>>,
    monthlySummary: Map<String, List<CategorySummary>>,
    yearlySummary: Map<String, List<CategorySummary>> = emptyMap(),
    selectedPeriod: Period,
    availableWeeks: List<String> = emptyList(),
    availableMonths: List<String> = emptyList(),
    availableYears: List<String> = emptyList(),
    onWeekSelected: (String) -> Unit = {},
    onMonthSelected: (String) -> Unit = {},
    onYearSelected: (String) -> Unit = {},
    onPeriodSelected: (Period) -> Unit = {},
    title: String = "Spending Distribution"
) {
    val categories = when (selectedPeriod) {
        is Period.Week -> weeklySummary[selectedPeriod.code] ?: emptyList()
        is Period.Month -> monthlySummary[selectedPeriod.code] ?: emptyList()
        is Period.Year -> yearlySummary[selectedPeriod.code] ?: emptyList()
    }

    val totalAmount = categories.sumOf { it.total }.toFloat()
    val categorySums = categories.map { it.category to it.total.toFloat() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        CategoryHeader(title = title)

        Column {
            Spacer(modifier = Modifier.height(8.dp))
            PeriodSelector(
                selectedPeriod = selectedPeriod,
                availableWeeks = availableWeeks,
                availableMonths = availableMonths,
                availableYears = availableYears,
                onWeekSelected = onWeekSelected,
                onMonthSelected = onMonthSelected,
                onYearSelected = onYearSelected,
                onPeriodSelected = onPeriodSelected
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        DonutChartSection(categorySums, totalAmount)
        Spacer(Modifier.height(16.dp))
        CategoryList(
            categories = categorySums,
            totalAmount = totalAmount,
            segmentColors = SegmentColors
        )
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
fun PeriodSelector(
    selectedPeriod: Period,
    availableWeeks: List<String> = emptyList(),
    availableMonths: List<String> = emptyList(),
    availableYears: List<String> = emptyList(),
    onPeriodSelected: (Period) -> Unit = {},
    onWeekSelected: (String) -> Unit = {},
    onMonthSelected: (String) -> Unit = {},
    onYearSelected: (String) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // --- Tabs ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TimeSpan.entries.forEach { span ->
                    val isSelected = when (span) {
                        TimeSpan.WEEK -> selectedPeriod is Period.Week
                        TimeSpan.MONTH -> selectedPeriod is Period.Month
                        TimeSpan.YEAR -> selectedPeriod is Period.Year
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF2D2D2D) else Color(0xFFE0E0E0))
                            .clickable {
                                when (span) {
                                    TimeSpan.WEEK -> availableWeeks.firstOrNull()
                                        ?.let { onPeriodSelected(Period.Week(it)) }

                                    TimeSpan.MONTH -> availableMonths.firstOrNull()
                                        ?.let { onPeriodSelected(Period.Month(it)) }

                                    TimeSpan.YEAR -> availableYears.firstOrNull()
                                        ?.let { onPeriodSelected(Period.Year(it)) }
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = span.displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isSelected) Color.White else Color.Black
                        )
                    }
                }
            }

            // --- Custom Dropdown ---
            val (options, selectedCode, onSelected, placeholder) = when (selectedPeriod) {
                is Period.Week -> Quad(
                    availableWeeks,
                    selectedPeriod.code,
                    onWeekSelected,
                    "Select Week"
                )

                is Period.Month -> Quad(
                    availableMonths,
                    selectedPeriod.code,
                    onMonthSelected,
                    "Select Month"
                )

                is Period.Year -> Quad(
                    availableYears,
                    selectedPeriod.code,
                    onYearSelected,
                    "Select Year"
                )
            }

            if (options.isNotEmpty()) {
                SexyDropdown(
                    options = options,
                    selected = selectedCode,
                    onSelected = onSelected,
                    placeholder = placeholder,
                    modifier = Modifier.width(120.dp)
                )
            }
        }
    }
}

// Sexy dropdown
@Composable
fun SexyDropdown(
    options: List<String>,
    selected: String?,
    onSelected: (String) -> Unit,
    placeholder: String = "Select",
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // Button
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF0F0F0))
                .clickable { expanded = !expanded }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selected ?: placeholder,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = Color.DarkGray
            )
        }

        // Dropdown content
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color.White)
                .clip(RoundedCornerShape(12.dp))
                .shadow(4.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontSize = 12.sp) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Helper data class
private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)


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

