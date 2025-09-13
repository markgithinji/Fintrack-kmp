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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintrack.shared.feature.transaction.data.CategorySummary
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
    tabType: String,               // "Income" or "Expenses"
    period: String,                // "week" or "month"
    value: String,                 // week code or month id
    viewModel: TransactionViewModel = viewModel()
) {

    // Load distribution on parameters change
    LaunchedEffect(tabType, period, value) {
        // For "Income"/"Expenses" you can optionally pass type if needed
        val type = when (tabType) {
            "Income" -> "income"
            "Expenses" -> "expense"
            else -> null
        }
        viewModel.loadDistribution(
            weekOrMonthCode = value,
            type = type
            // Optionally: start = ..., end = ...
        )
    }

    val distributionResult by viewModel.distribution.collectAsStateWithLifecycle()

    when (distributionResult) {
        is Result.Loading -> Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        is Result.Error -> {
            val message = (distributionResult as Result.Error).exception.message ?: "Unknown error"
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) { Text("Error: $message", color = Color.Red) }
        }

        is Result.Success -> {
            val data = (distributionResult as Result.Success).data

            // Prepare categories based on selected tab
            val categories = when (tabType) {
                "Income" -> data.incomeCategories
                "Expenses" -> data.expenseCategories
                else -> data.incomeCategories + data.expenseCategories
            }

            val weeklyMap = if (period == "week") mapOf(data.period to categories) else emptyMap()
            val monthlyMap = if (period == "month") mapOf(data.period to categories) else emptyMap()

            CategoryTotalsCardContent(
                weeklySummary = weeklyMap,
                monthlySummary = monthlyMap,
                initialSelectedWeek = weeklyMap.keys.firstOrNull(),
                initialSelectedMonth = monthlyMap.keys.firstOrNull()
            )
        }
    }
}



@Composable
private fun CategoryTotalsCardContent(
    weeklySummary: Map<String, List<CategorySummary>>,
    monthlySummary: Map<String, List<CategorySummary>>,
    initialSelectedWeek: String? = null,
    initialSelectedMonth: String? = null,
    title: String = "Spending Distribution"
) {
    var selectedTab by remember { mutableStateOf(TimeSpan.WEEK) }
    var selectedWeek by remember { mutableStateOf(initialSelectedWeek) }
    var selectedMonth by remember { mutableStateOf(initialSelectedMonth) }

    // --- LOGGING ---
    LaunchedEffect(weeklySummary, monthlySummary, selectedWeek, selectedMonth) {
        println("CategoryTotalsCardContent: weeklySummary=$weeklySummary")
        println("CategoryTotalsCardContent: monthlySummary=$monthlySummary")
        println("CategoryTotalsCardContent: selectedWeek=$selectedWeek")
        println("CategoryTotalsCardContent: selectedMonth=$selectedMonth")
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // --- Title ---
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // --- Tabs ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TimeSpan.entries.forEach { span ->
                val isSelected = span == selectedTab
                val backgroundColor = if (isSelected) Color(0xFF2D2D2D) else Color.Transparent
                val textColor = if (isSelected) Color.White else Color.Black

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(backgroundColor)
                        .clickable { selectedTab = span }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = span.displayName,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                }
            }
        }

        // --- Period selector ---
        if (selectedTab == TimeSpan.WEEK && weeklySummary.isNotEmpty()) {
            WeekSelector(
                weeks = weeklySummary.keys.toList(),
                selectedWeek = selectedWeek,
                onWeekSelected = { selectedWeek = it }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (selectedTab == TimeSpan.MONTH && monthlySummary.isNotEmpty()) {
            WeekSelector(
                weeks = monthlySummary.keys.toList(),
                selectedWeek = selectedMonth,
                onWeekSelected = { selectedMonth = it }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // --- Prepare category data ---
        val categories: List<CategorySummary> = when (selectedTab) {
            TimeSpan.WEEK -> selectedWeek?.let { weeklySummary[it] } ?: emptyList()
            TimeSpan.MONTH -> selectedMonth?.let { monthlySummary[it] } ?: emptyList()
            TimeSpan.YEAR -> monthlySummary.values.flatten()
        }

        val totalAmount = categories.sumOf { it.total }.toFloat()
        val categorySums = categories.map { it.category to it.total.toFloat() }

        // --- Top 4 + "Others" for chart ---
        val sortedForChart = categorySums.sortedByDescending { it.second }
        val topForChart = sortedForChart.take(4).toMutableList()
        val othersTotal = sortedForChart.drop(4).sumOf { it.second.toDouble() }.toFloat()
        if (othersTotal > 0f) topForChart.add("Others" to othersTotal)

        // --- Donut chart ---
        if (categorySums.isNotEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val chartColors = topForChart.mapIndexed { index, _ ->
                    if (index < 4) SegmentColors[index] else SegmentColors.last()
                }

                SimpleDonutChart(
                    categorySums = topForChart.map { it.first to it.second.toDouble() },
                    totalAmount = totalAmount.toDouble(),
                    chartSize = 250.dp,
                    gapPercentage = 0.02f,
                    segmentColors = chartColors
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Category list ---
        CategoryList(
            categories = categorySums,
            totalAmount = totalAmount,
            segmentColors = SegmentColors
        )
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



@Composable
fun WeekSelector(
    weeks: List<String>,
    selectedWeek: String?,
    onWeekSelected: (String) -> Unit
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
            Text(selectedWeek ?: "Select Week")
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            weeks.forEach { week ->
                DropdownMenuItem(
                    text = { Text(week) },
                    onClick = {
                        onWeekSelected(week)
                        expanded = false
                    }
                )
            }
        }
    }
}

enum class TimeSpan(val displayName: String) {
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}



@Composable
fun SimpleDonutChart(
    categorySums: List<Pair<String, Double>>,
    totalAmount: Double,
    modifier: Modifier = Modifier,
    chartSize: Dp = 200.dp,
    gapPercentage: Float = 0.02f,
    segmentColors: List<Color> = SegmentColors
) {
    if (categorySums.isEmpty() || totalAmount <= 0.0) return

    Box(
        modifier = modifier
            .size(chartSize)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = 40.dp.toPx()
            val diameter = size.minDimension - strokeWidthPx // ensures perfect circle

            val gapAngle = 360f * gapPercentage
            var startAngle = -90f

            categorySums.forEachIndexed { index, (_, amount) ->
                val sweep = ((amount / totalAmount) * 360f).toFloat() - gapAngle
                drawArc(
                    color = segmentColors[index % segmentColors.size],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
                )
                startAngle += sweep + gapAngle
            }

        }
    }
}

