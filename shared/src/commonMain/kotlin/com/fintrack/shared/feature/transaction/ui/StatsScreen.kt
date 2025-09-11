package com.fintrack.shared.feature.transaction.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintrack.shared.feature.transaction.data.CategorySummary
import com.fintrack.shared.feature.transaction.data.Highlight

val DarkCardBackground = Color(0xFF1B1B1B)
val LightGreenCardBackground = Color(0xFFE8FFB5)
val LightGreenText = Color(0xFF80A23F)
val DarkGreenSegment = Color(0xFF5B6C3F)
val LightGreenSegment1 = Color(0xFF80A23F)
val LightGreenSegment2 = Color(0xFFBCC26F)
val LightGreenSegment3 = Color(0xFFD6DBA5)
val LightGreenSegment4 = Color(0xFFF1F6D4)
val CategoryTextColor = Color(0xFF4A4A4A)

@Composable
fun StatisticsScreen(
    viewModel: TransactionViewModel = viewModel()
) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadSummary() }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader()

        summary?.let { data ->
            // Pass domain Highlight objects directly
            data.highestMonth?.let { highestMonth ->
                data.highestCategory?.let { highestCategory ->
                    data.highestDay?.let { highestDay ->
                        SpendingHighlightsSection(
                            highestMonth = highestMonth,
                            highestCategory = highestCategory,
                            highestDay = highestDay,
                            averagePerDay = data.averagePerDay
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SpendingDistributionSection(
                categories = data.monthlyCategorySummary["2025-09"] ?: emptyList()
            )
        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun ScreenHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Statistics",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            TabItem(text = "All", isSelected = false)
            TabItem(text = "Income", isSelected = false)
            TabItem(text = "Expenses", isSelected = true)
        }
    }
}

@Composable
fun TabItem(text: String, isSelected: Boolean) {
    val backgroundColor = if (isSelected) Color.Black else Color.Transparent
    val textColor = if (isSelected) Color.White else Color.Black
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .clip(shape)
            .background(backgroundColor)
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        Text(text = text, color = textColor, fontWeight = FontWeight.SemiBold)
    }
}

fun formatCurrency(amount: Double): String {
    val whole = amount.toLong()
    val fraction = ((amount - whole) * 100).toInt()
    return "Rp ${
        whole.toString().reversed().chunked(3).joinToString(".") { it.reversed() }
    }.${fraction.toString().padStart(2, '0')}"
}

fun formatPercentage(percentage: Double): String {
    val rounded = kotlin.math.round(percentage * 10) / 10
    return "$rounded%"
}


// ---- Highlight Section ----
@Composable
fun SpendingHighlightsSection(
    highestMonth: Highlight,
    highestCategory: Highlight,
    highestDay: Highlight,
    averagePerDay: Double
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Spending Highlights",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HighlightCard(
                modifier = Modifier.weight(1f),
                title = "Highest Month",
                value = highestMonth.value,
                description = "${formatCurrency(highestMonth.amount)} spent",
                backgroundColor = DarkCardBackground,
                titleColor = Color.White,
                valueColor = Color.White
            )
            HighlightCard(
                modifier = Modifier.weight(1f),
                title = "Most Spent Category",
                value = highestCategory.value,
                description = "${formatCurrency(highestCategory.amount)} total",
                backgroundColor = LightGreenCardBackground,
                titleColor = Color.Black,
                valueColor = LightGreenText
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HighlightCard(
                modifier = Modifier.weight(1f),
                title = "Highest Daily Spending",
                value = highestDay.value,
                description = "${formatCurrency(highestDay.amount)} spent",
                backgroundColor = LightGreenCardBackground,
                titleColor = Color.Black,
                valueColor = LightGreenText
            )
            HighlightCard(
                modifier = Modifier.weight(1f),
                title = "Average Per Day",
                value = formatCurrency(averagePerDay),
                description = "Daily Spending",
                backgroundColor = LightGreenCardBackground,
                titleColor = Color.Black,
                valueColor = LightGreenText
            )
        }
    }
}

@Composable
fun HighlightCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    description: String,
    backgroundColor: Color,
    titleColor: Color,
    valueColor: Color
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, fontSize = 14.sp, color = titleColor)
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = valueColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 12.sp, color = titleColor.copy(alpha = 0.7f))
        }
    }
}

// ---- Distribution Section ----
@Composable
fun SpendingDistributionSection(categories: List<CategorySummary>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Spending Distribution by Category",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(16.dp))

        DonutChart(categories)
        Spacer(modifier = Modifier.height(24.dp))
        CategoryList(categories)
    }
}

@Composable
fun DonutChart(categories: List<CategorySummary>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 50.dp.toPx()
            var startAngle = 0f
            val colors = listOf(
                DarkGreenSegment,
                LightGreenSegment1,
                LightGreenSegment2,
                LightGreenSegment3,
                LightGreenSegment4
            )

            val totalPercentage = categories.sumOf { it.percentage }

            categories.forEachIndexed { index, cat ->
                val sweep =
                    ((cat.percentage / totalPercentage) * 360f).toFloat()
                drawArc(
                    color = colors.getOrElse(index) { LightGreenSegment4 },
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
                startAngle += sweep
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Top Category", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CategoryList(categories: List<CategorySummary>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        categories.forEachIndexed { index, cat ->
            val color = listOf(
                DarkGreenSegment,
                LightGreenSegment1,
                LightGreenSegment2,
                LightGreenSegment3,
                LightGreenSegment4
            ).getOrElse(index) { LightGreenSegment4 }

            CategoryItem(
                color = color,
                name = cat.category,
                amount = formatCurrency(cat.total),
                percentage = formatPercentage(cat.percentage)
            )
        }
    }
}

@Composable
fun CategoryItem(color: Color, name: String, amount: String, percentage: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = name, color = CategoryTextColor, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(text = amount, color = Color.Black, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = percentage, color = color, fontWeight = FontWeight.SemiBold)
    }
}