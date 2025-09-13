package com.fintrack.shared.feature.transaction.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintrack.shared.feature.transaction.data.Highlight
import com.fintrack.shared.feature.transaction.data.Result

// Chart segments
val SegmentColor1 = Color(0xFFE63946)   // Strong red
val SegmentColor2 = Color(0xFFF1FAEE)   // Soft off-white / mint
val SegmentColor3 = Color(0xFF457B9D)   // Vibrant blue
val SegmentColor4 = Color(0xFFF4A261)   // Warm orange
val SegmentColor5 = Color(0xFF2A9D8F)   // Teal / turquoise


@Composable
fun StatisticsScreen(
    viewModel: TransactionViewModel = viewModel()
) {
    val summaryResult by viewModel.summary.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf("Expenses") } // default tab

    LaunchedEffect(Unit) { viewModel.loadSummary() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        // --- Tabs at top ---
        ScreenHeader(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

        Spacer(modifier = Modifier.height(16.dp))

        when (summaryResult) {
            is Result.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Result.Error -> {
                val message = (summaryResult as Result.Error).exception.message ?: "Unknown error"
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: $message", color = Color.Red)
                }
            }
            is Result.Success -> {
                val data = (summaryResult as Result.Success).data

                // --- Select highlights dynamically based on selected tab ---
                val highlightData = when (selectedTab) {
                    "Income" -> data.copy(
                        highestMonth = data.highestIncomeMonth,
                        highestCategory = data.highestIncomeCategory,
                        highestDay = data.highestIncomeDay,
                        averagePerDay = data.averageIncomePerDay,
                        weeklyCategorySummary = data.weeklyIncomeCategorySummary,
                        monthlyCategorySummary = data.monthlyIncomeCategorySummary
                    )
                    "Expenses" -> data
                    "All" -> data.copy(
                        highestMonth = data.highestMonth,
                        highestCategory = data.highestCategory,
                        highestDay = data.highestDay,
                        averagePerDay = data.averagePerDay,
                        weeklyCategorySummary = data.weeklyCategorySummary,
                        monthlyCategorySummary = data.monthlyCategorySummary
                    )
                    else -> data
                }

                // ---- Highlights ----
                highlightData.highestMonth?.let { highestMonth ->
                    highlightData.highestCategory?.let { highestCategory ->
                        highlightData.highestDay?.let { highestDay ->
                            SpendingHighlightsSection(
                                highestMonth = highlightData.highestMonth,
                                highestCategory = highlightData.highestCategory,
                                highestDay = highlightData.highestDay,
                                averagePerDay = highlightData.averagePerDay,
                                tabType = selectedTab
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ---- Category Totals Card (donut chart + list) ----
                CategoryTotalsCardWithTabs(
                    weeklySummary = highlightData.weeklyCategorySummary,
                    monthlySummary = highlightData.monthlyCategorySummary
                )
            }
        }
    }
}

@Composable
fun ScreenHeader(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    val tabs = listOf("All", "Income", "Expenses")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        tabs.forEach { tab ->
            TabItem(
                text = tab,
                isSelected = tab == selectedTab,
                onClick = { onTabSelected(tab) }
            )
        }
    }
}

@Composable
fun TabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color.Black else Color.Transparent
    val textColor = if (isSelected) Color.White else Color.Black
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .clip(shape)
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        Text(text = text, color = textColor, fontWeight = FontWeight.SemiBold)
    }
}



// ---- Highlight Section ----

@Composable
fun SpendingHighlightsSection(
    highestMonth: Highlight,
    highestCategory: Highlight,
    highestDay: Highlight,
    averagePerDay: Double,
    tabType: String // "Income", "Expenses", or "All"
) {
    val amountSuffix = when (tabType) {
        "Income" -> "received"
        "Expenses" -> "spent"
        "All" -> "total"
        else -> ""
    }
    val dailyLabel = when (tabType) {
        "Income" -> "Daily Income"
        "Expenses" -> "Daily Spending"
        "All" -> "Daily Total"
        else -> ""
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = when(tabType) {
                "Income" -> "Income Highlights"
                "Expenses" -> "Spending Highlights"
                "All" -> "Summary Highlights"
                else -> "Highlights"
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val monthDisplay = highestMonth.value.toMonthName()
            HighlightCard(
                modifier = Modifier.weight(1f),
                title = "Highest Month",
                value = monthDisplay,
                description = "${formatCurrencyKmp(highestMonth.amount)} $amountSuffix",
                backgroundColor = SegmentColor3,
                titleColor = Color.White,
                valueColor = Color.White,
                contentSpacing = 8.dp
            )
            HighlightCard(
                modifier = Modifier.weight(1f),
                title = "Top Category",
                value = highestCategory.value,
                description = "${formatCurrencyKmp(highestCategory.amount)} $amountSuffix",
                backgroundColor = SegmentColor4,
                titleColor = Color.White,
                valueColor = Color.White,
                contentSpacing = 8.dp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HighlightCard(
                modifier = Modifier.weight(1f),
                title = "Highest Daily",
                value = highestDay.value.toFormattedDate(),
                description = "${formatCurrencyKmp(highestDay.amount)} $amountSuffix",
                backgroundColor = SegmentColor5,
                titleColor = Color.White,
                valueColor = Color.White,
                contentSpacing = 8.dp
            )

            HighlightCard(
                modifier = Modifier.weight(1f),
                title = "Average Per Day",
                value = formatCurrencyKmp(averagePerDay),
                description = dailyLabel,
                backgroundColor = SegmentColor1,
                titleColor = Color.White,
                valueColor = Color.White,
                contentSpacing = 8.dp
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
    valueColor: Color,
    contentSpacing: Dp = 4.dp
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = title, fontSize = 14.sp, color = titleColor)
            Spacer(modifier = Modifier.height(contentSpacing))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = valueColor)
            Spacer(modifier = Modifier.height(contentSpacing))
            Text(text = description, fontSize = 12.sp, color = titleColor.copy(alpha = 0.7f))
        }
    }
}

// month conversion: "yyyy-MM" -> "Jan 2025"
fun String.toMonthName(): String {
    val parts = this.split("-")
    if (parts.size != 2) return this
    val year = parts[0]
    val monthIndex = (parts[1].toIntOrNull()?.minus(1)) ?: return this
    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    val monthName = monthNames.getOrElse(monthIndex) { parts[1] }
    return "$monthName $year"
}

// date conversion: "yyyy-MM-dd" -> "15 Jan 2025"
fun String.toFormattedDate(): String {
    val parts = this.split("-")
    if (parts.size != 3) return this
    val day = parts[2].toIntOrNull() ?: return this
    val monthIndex = (parts[1].toIntOrNull()?.minus(1)) ?: return this
    val year = parts[0]
    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    val monthName = monthNames.getOrElse(monthIndex) { parts[1] }
    return "$day $monthName $year"
}

fun formatCurrencyKmp(amount: Double): String {
    val whole = amount.toLong()
    val fraction = ((amount - whole) * 100).toInt()
    val wholeFormatted = whole.toString()
        .reversed()
        .chunked(3)
        .joinToString(",") { it.reversed() }
        .reversed()
    return "Ksh $wholeFormatted.${fraction.toString().padStart(2, '0')}"
}

