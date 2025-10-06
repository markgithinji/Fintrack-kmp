package com.fintrack.shared.feature.summary.ui

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
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.summary.domain.model.CategorySummary
import com.fintrack.shared.feature.summary.domain.model.DistributionSummary
import com.fintrack.shared.feature.transaction.ui.addtransaction.AnimatedShimmerBox
import com.fintrack.shared.feature.transaction.ui.home.GreenIncome

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
        is Result.Loading -> {
            LoadingCategoryContent(
                selectedPeriod = period,
                availableWeeks = availableWeeks,
                availableMonths = availableMonths,
                availableYears = availableYears,
                onWeekSelected = onWeekSelected,
                onMonthSelected = onMonthSelected,
                onYearSelected = onYearSelected,
                onPeriodSelected = onPeriodSelected
            )
        }

        is Result.Error -> {
            val message = distributionResult.exception.message ?: "Failed to load distribution"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = "Error",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Unable to Load Data",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = message,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { /* Add retry logic */ },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenIncome),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            text = "Try Again",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
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
                yearlySummary = yearlyMap,
                selectedPeriod = period,
                availableWeeks = availableWeeks,
                availableMonths = availableMonths,
                availableYears = availableYears,
                onWeekSelected = onWeekSelected,
                onMonthSelected = onMonthSelected,
                onYearSelected = onYearSelected,
                onPeriodSelected = onPeriodSelected,
                title = when (tabType) {
                    is TabType.Income -> "Income Distribution"
                    is TabType.Expense -> "Expense Distribution"
                }
            )
        }
    }
}

@Composable
fun LoadingCategoryContent(
    selectedPeriod: Period,
    availableWeeks: List<String> = emptyList(),
    availableMonths: List<String> = emptyList(),
    availableYears: List<String> = emptyList(),
    onWeekSelected: (String) -> Unit = {},
    onMonthSelected: (String) -> Unit = {},
    onYearSelected: (String) -> Unit = {},
    onPeriodSelected: (Period) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Text(
            text = "Distribution",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column {
            Spacer(modifier = Modifier.height(8.dp))
            LoadingPeriodSelector()
            Spacer(modifier = Modifier.height(16.dp))
        }

        LoadingDonutChartSection()
        Spacer(Modifier.height(16.dp))
        LoadingCategoryList()
    }
}

@Composable
fun LoadingDonutChartSection() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Create mock data for 4 segments + others
            val mockSegments = listOf(
                "Shopping" to 1500f,
                "Food" to 1200f,
                "Transport" to 800f,
                "Entertainment" to 600f,
                "Others" to 400f
            )
            val mockTotal = mockSegments.sumOf { it.second.toDouble() }.toFloat()
            val mockChartColors = mockSegments.mapIndexed { index, _ ->
                if (index < 4) SegmentColors[index] else SegmentColors.last()
            }

            LoadingInteractiveDonutWithText(
                mockSegments = mockSegments,
                mockTotal = mockTotal,
                segmentColors = mockChartColors
            )
        }
    }
}

@Composable
fun LoadingInteractiveDonutWithText(
    mockSegments: List<Pair<String, Float>>,
    mockTotal: Float,
    segmentColors: List<Color>,
    modifier: Modifier = Modifier,
    chartSize: Dp = 250.dp
) {
    Box(modifier = modifier.size(chartSize), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 40.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            var startAngle = -90f

            mockSegments.forEachIndexed { index, (_, amount) ->
                val sweep = (((amount / mockTotal) * 360.0).toFloat()) - 360f * 0.02f
                val shimmerAlpha = (0.3f + (index * 0.1f)).coerceAtMost(0.7f)
                drawArc(
                    color = segmentColors[index].copy(alpha = shimmerAlpha),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
                startAngle += sweep + 360f * 0.02f
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedShimmerBox(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedShimmerBox(
                modifier = Modifier
                    .width(80.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
            AnimatedShimmerBox(
                modifier = Modifier
                    .width(100.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun LoadingCategoryList() {
    Column(modifier = Modifier.fillMaxWidth()) {
        repeat(4) { index ->
            LoadingCategoryListItem(
                color = SegmentColors.getOrElse(index) { Color.LightGray }
            )
            if (index < 3) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun LoadingCategoryListItem(color: Color = Color.LightGray) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color dot
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))

        // Category name - using original fixed width
        AnimatedShimmerBox(
            modifier = Modifier
                .width(160.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.weight(1f))

        // Amount - original fixed width
        AnimatedShimmerBox(
            modifier = Modifier
                .width(100.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Percentage - original fixed width
        AnimatedShimmerBox(
            modifier = Modifier
                .width(40.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
fun LoadingPeriodSelector() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(3) {
                AnimatedShimmerBox(
                    modifier = Modifier
                        .width(60.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
        AnimatedShimmerBox(
            modifier = Modifier
                .width(120.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    }
}

@Composable
fun ErrorCategoryContent(
    message: String,
    selectedPeriod: Period,
    availableWeeks: List<String> = emptyList(),
    availableMonths: List<String> = emptyList(),
    availableYears: List<String> = emptyList(),
    onWeekSelected: (String) -> Unit = {},
    onMonthSelected: (String) -> Unit = {},
    onYearSelected: (String) -> Unit = {},
    onPeriodSelected: (Period) -> Unit = {},
    onRetry: () -> Unit
) {
    Column {
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

        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.PieChart,
                    contentDescription = "Error",
                    tint = Color.Gray,
                    modifier = Modifier.size(48.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Unable to Load Data",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        message,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenIncome),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(
                        "Try Again",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
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


enum class TimeSpan(val displayName: String) {
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}

