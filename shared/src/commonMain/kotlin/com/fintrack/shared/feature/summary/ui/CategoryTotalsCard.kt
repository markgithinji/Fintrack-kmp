package com.fintrack.shared.feature.summary.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.GreenIncome
import com.example.compose.PinkExpense
import com.example.compose.SegmentColor1
import com.example.compose.SegmentColor3
import com.example.compose.SegmentColor4
import com.example.compose.SegmentColor5
import com.fintrack.shared.feature.core.ui.AnimatedShimmerBox
import com.fintrack.shared.feature.core.ui.CommonErrorState
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.util.formatAsShortDate
import com.fintrack.shared.feature.settings.ui.toCurrencyString
import com.fintrack.shared.feature.summary.domain.model.DistributionSummary
import com.fintrack.shared.feature.summary.domain.model.Period
import com.fintrack.shared.feature.summary.domain.model.TabType
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

import androidx.compose.runtime.saveable.rememberSaveable

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
    onPeriodSelected: (Period) -> Unit = {},
    onCategoryClick: (String) -> Unit = {}
) {
    // Use rememberSaveable to maintain selection across navigation
    // Reset selection only when period or tab type changes
    var selectedIndex by rememberSaveable(period.toString(), tabType.toString()) { 
        mutableStateOf(-1) 
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                // Remove tap-to-deselect to maintain selection state
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                PeriodSelector(
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

            Spacer(modifier = Modifier.height(20.dp))

            Crossfade(
                targetState = distributionResult,
                animationSpec = tween(durationMillis = 300),
                label = "ChartContentFade"
            ) { result ->
                key(period.getDateRange()?.first ?: period.toString(), tabType) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 450.dp)
                    ) {
                        when (result) {
                            is Result.Loading -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    LoadingDonutChartSection()
                                    Spacer(Modifier.height(16.dp))
                                    LoadingCategoryList()
                                }
                            }

                            is Result.Error -> {
                                ErrorState(
                                    message = result.exception.message ?: "Failed to load distribution",
                                    onRetry = { /* distribution logic doesn't have an easy retry here, but we can pass one if needed */ }
                                )
                            }

                            is Result.Success -> {
                                val baseCategories = when (tabType) {
                                    is TabType.Income -> result.data.incomeCategories
                                    is TabType.Expense -> result.data.expenseCategories
                                }

                                if (baseCategories.isEmpty() && (tabType !is TabType.Expense || result.data.totalTransactionCost <= 0.0)) {
                                    EmptyDistributionState()
                                } else {
                                    val categorySums = mutableListOf<Pair<String, Float>>()
                                    categorySums.addAll(baseCategories.map { it.category to it.total.toFloat() })

                                    if (tabType is TabType.Expense && result.data.totalTransactionCost > 0) {
                                        categorySums.add("Transaction Cost" to result.data.totalTransactionCost.toFloat())
                                    }

                                    val totalAmount = categorySums.sumOf { it.second.toDouble() }.toFloat()

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        DonutChartSection(
                                            categorySums = categorySums,
                                            totalAmount = totalAmount,
                                            selectedIndex = selectedIndex,
                                            onSelectedIndexChange = { selectedIndex = it }
                                        )
                                        Spacer(Modifier.height(16.dp))
                                        CategoryList(
                                            categories = categorySums,
                                            totalAmount = totalAmount,
                                            selectedIndex = selectedIndex,
                                            onSelectedIndexChange = { selectedIndex = it },
                                            onCategoryClick = onCategoryClick,
                                            segmentColors = SegmentColors
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyDistributionState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 36.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No Data",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No transactions found for this period.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: (() -> Unit)? = null
) {
    CommonErrorState(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        title = "Failed to load distribution",
        errorMessage = message,
        onRetry = onRetry
    )
}

@Composable
fun LoadingDonutChartSection() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val mockSegments = listOf(
                "A" to 1500f,
                "B" to 1200f,
                "C" to 800f,
                "D" to 600f,
                "E" to 400f
            )
            val mockTotal = 4500f
            
            LoadingInteractiveDonutWithText(
                mockSegments = mockSegments,
                mockTotal = mockTotal,
                segmentColors = SegmentColors,
                chartSize = 250.dp
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
            val strokeWidth = 36.dp.toPx()
            val diameter = size.minDimension - 80.dp.toPx() // Match final chart diameter
            var startAngle = -90f

            mockSegments.forEachIndexed { index, (_, amount) ->
                val allocatedAngle = (((amount / mockTotal) * 360.0).toFloat())
                val sweep = (allocatedAngle - 360f * 0.03f).coerceAtLeast(0.5f)
                drawArc(
                    color = segmentColors[index % segmentColors.size].copy(alpha = 0.2f),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += allocatedAngle
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedShimmerBox(modifier = Modifier.size(24.dp).clip(CircleShape))
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedShimmerBox(modifier = Modifier.width(80.dp).height(14.dp).clip(RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.height(4.dp))
            AnimatedShimmerBox(modifier = Modifier.width(120.dp).height(24.dp).clip(RoundedCornerShape(4.dp)))
        }
    }
}

@Composable
fun LoadingCategoryList() {
    Column(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(4) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedShimmerBox(modifier = Modifier.size(10.dp).clip(CircleShape))
                Spacer(Modifier.width(12.dp))
                AnimatedShimmerBox(modifier = Modifier.width(120.dp).height(14.dp).clip(RoundedCornerShape(4.dp)))
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    AnimatedShimmerBox(modifier = Modifier.width(80.dp).height(14.dp).clip(RoundedCornerShape(4.dp)))
                    Spacer(Modifier.height(2.dp))
                    AnimatedShimmerBox(modifier = Modifier.width(40.dp).height(10.dp).clip(RoundedCornerShape(4.dp)))
                }
            }
        }
    }
}

@Composable
fun CategoryList(
    categories: List<Pair<String, Float>>,
    totalAmount: Float,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    onCategoryClick: (String) -> Unit = {},
    segmentColors: List<Color>
) {
    val sortedCategorySums = categories.sortedByDescending { it.second }
    val topCategories = sortedCategorySums.take(4).toMutableList()
    val othersTotal = sortedCategorySums.drop(4).sumOf { it.second.toDouble() }.toFloat()
    if (othersTotal > 0f) {
        topCategories.add("Others" to othersTotal)
    }

    Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        topCategories.forEachIndexed { index, (categoryName, amount) ->
            val percent = if (totalAmount > 0) (amount / totalAmount * 100).toInt() else 0
            val color = if (index < 4) segmentColors[index % segmentColors.size] else segmentColors.last()
            val isSelected = selectedIndex == index

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) color.copy(alpha = 0.1f) else Color.Transparent)
                    .clickable {
                        onSelectedIndexChange(index)
                        onCategoryClick(categoryName)
                    }
                    .padding(vertical = 6.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(color, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = amount.toDouble().toCurrencyString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$percent%",
                        style = MaterialTheme.typography.labelSmall,
                        color = color
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
    var expanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val (options, selectedCode, onSelected, currentType) = when (selectedPeriod) {
        is Period.Week -> Triple(availableWeeks, selectedPeriod.code, onWeekSelected).let { it.copy(fourth = TimeSpan.WEEK) }
        is Period.Month -> Triple(availableMonths, selectedPeriod.code, onMonthSelected).let { it.copy(fourth = TimeSpan.MONTH) }
        is Period.Year -> Triple(availableYears, selectedPeriod.code, onYearSelected).let { it.copy(fourth = TimeSpan.YEAR) }
    }

    val selectedIndex = remember(options, selectedCode) {
        options.indexOf(selectedCode).coerceAtLeast(0)
    }

    LaunchedEffect(expanded) {
        if (expanded && selectedIndex >= 0) {
            // DropdownMenuItem is roughly 48dp high.
            // We use a safe estimate to scroll the selected item into view.
            scrollState.scrollTo(selectedIndex * 120)
        }
    }

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatPeriodCode(selectedCode ?: "Select", currentType),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .width(150.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)), RoundedCornerShape(16.dp))
                .heightIn(max = 320.dp)
        ) {
            // TimeSpan Tabs inside Dropdown (Fixed at top)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TimeSpan.entries.forEach { span ->
                    val isSelected = when (span) {
                        TimeSpan.WEEK -> selectedPeriod is Period.Week
                        TimeSpan.MONTH -> selectedPeriod is Period.Month
                        TimeSpan.YEAR -> selectedPeriod is Period.Year
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable {
                                when (span) {
                                    TimeSpan.WEEK -> availableWeeks.firstOrNull()?.let { onPeriodSelected(Period.Week(it)) }
                                    TimeSpan.MONTH -> availableMonths.firstOrNull()?.let { onPeriodSelected(Period.Month(it)) }
                                    TimeSpan.YEAR -> availableYears.firstOrNull()?.let { onPeriodSelected(Period.Year(it)) }
                                }
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = span.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            
            val scrollbarColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

            // Scrollable options
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 250.dp)
                    .drawWithContent {
                        drawContent()
                        if (scrollState.maxValue > 0) {
                            val viewPortHeight = size.height
                            val contentHeight = viewPortHeight + scrollState.maxValue
                            val scrollOffset = scrollState.value.toFloat()
                            
                            val knobHeight = (viewPortHeight / contentHeight) * viewPortHeight
                            val knobStart = (scrollOffset / contentHeight) * viewPortHeight
                            
                            drawRoundRect(
                                color = scrollbarColor,
                                topLeft = Offset(size.width - 6.dp.toPx(), knobStart),
                                size = Size(4.dp.toPx(), knobHeight),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }
                    }
                    .verticalScroll(scrollState)
            ) {
                options.forEach { option ->
                    val isItemSelected = option == selectedCode
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = formatPeriodCode(option, currentType), 
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isItemSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isItemSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            ) 
                        },
                        modifier = Modifier.background(
                            if (isItemSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) 
                            else Color.Transparent
                        ),
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

private fun formatPeriodCode(code: String, type: TimeSpan): String {
    return try {
        when (type) {
            TimeSpan.WEEK -> {
                // code format: 2024-W25
                val parts = code.split("-W")
                if (parts.size == 2) {
                    val year = parts[0].toIntOrNull() ?: return code
                    val week = parts[1].toIntOrNull() ?: return code

                    // ISO week calculation (Simplified for UI)
                    // Monday is 0 in kotlinx-datetime ordinal
                    val jan1 = LocalDate(year, 1, 1)
                    val jan1DayOfWeek = jan1.dayOfWeek.ordinal + 1 // Mon=1, Sun=7
                    val daysToFirstMonday = (8 - jan1DayOfWeek) % 7
                    val firstMonday = jan1.plus(DatePeriod(days = daysToFirstMonday))
                    val weekStart = firstMonday.plus(DatePeriod(days = (week - 1) * 7))
                    val weekEnd = weekStart.plus(DatePeriod(days = 6))

                    if (weekStart.month == weekEnd.month) {
                        // Same month: "Jun 22 - 28, 2026"
                        "${weekStart.formatAsShortDate()} - ${weekEnd.dayOfMonth}, $year"
                    } else {
                        // Different months: "Jun 29 - Jul 5, 2026"
                        "${weekStart.formatAsShortDate()} - ${weekEnd.formatAsShortDate()}, $year"
                    }
                } else code
            }
            TimeSpan.MONTH -> {
                // code format: 2024-06
                val parts = code.split("-")
                if (parts.size == 2) {
                    val year = parts[0]
                    val month = parts[1].toIntOrNull() ?: return code
                    val monthName = when (month) {
                        1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
                        5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
                        9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
                        else -> ""
                    }
                    "$monthName $year"
                } else code
            }
            TimeSpan.YEAR -> code
        }
    } catch (_: Exception) {
        code
    }
}

private fun <A, B, C> Triple<A, B, C>.copy(fourth: TimeSpan): Quadruple<A, B, C, TimeSpan> {
    return Quadruple(first, second, third, fourth)
}

data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

val SegmentColors = listOf(
    SegmentColor3, // Blue
    SegmentColor4, // Orange
    SegmentColor1, // Red
    SegmentColor5, // Teal
    PinkExpense    // Pink
)

enum class TimeSpan(val displayName: String) {
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}
