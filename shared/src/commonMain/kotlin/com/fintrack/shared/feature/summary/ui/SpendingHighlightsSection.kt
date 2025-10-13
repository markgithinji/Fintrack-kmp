package com.fintrack.shared.feature.summary.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.summary.domain.model.Highlight
import com.fintrack.shared.feature.summary.domain.model.StatisticsSummary
import com.fintrack.shared.feature.transaction.ui.addtransaction.AnimatedShimmerBox
import com.fintrack.shared.feature.transaction.ui.home.GreenIncome

@Composable
fun SpendingHighlightsSection(
    tabType: TabType,
    highlightsResult: Result<StatisticsSummary>,
    loadHighlights: () -> Unit
) {
    LaunchedEffect(Unit) {
        loadHighlights()
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = when (tabType) {
                is TabType.Income -> "Income Highlights"
                is TabType.Expense -> "Spending Highlights"
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(16.dp))

        when (highlightsResult) {
            is Result.Loading -> {
                // Show loading highlight cards
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LoadingHighlightCard(modifier = Modifier.weight(1f))
                        LoadingHighlightCard(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LoadingHighlightCard(modifier = Modifier.weight(1f))
                        LoadingHighlightCard(modifier = Modifier.weight(1f))
                    }
                }
            }

            is Result.Error -> {
                ErrorHighlightCard(
                    message = highlightsResult.exception.message ?: "Failed to load highlights",
                    onRetry = loadHighlights
                )
            }

            is Result.Success -> {
                val data = highlightsResult.data
                val summaryHighlights = when (tabType) {
                    is TabType.Income -> data.incomeHighlights
                    is TabType.Expense -> data.expenseHighlights
                }

                // Provide defaults if null
                val month = summaryHighlights.highestMonth ?: Highlight("", "", 0.0)
                val category = summaryHighlights.highestCategory ?: Highlight("", "", 0.0)
                val day = summaryHighlights.highestDay ?: Highlight("", "", 0.0)
                val average = summaryHighlights.averagePerDay

                val amountSuffix = when (tabType) {
                    is TabType.Income -> "received"
                    is TabType.Expense -> "spent"
                }
                val dailyLabel = when (tabType) {
                    is TabType.Income -> "Daily Income"
                    is TabType.Expense -> "Daily Spending"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HighlightCard(
                        modifier = Modifier.weight(1f),
                        title = "Highest Month",
                        value = month.value.toMonthName(),
                        description = "${formatCurrencyKmp(month.amount)} $amountSuffix",
                        backgroundColor = SegmentColor3,
                        titleColor = Color.White,
                        valueColor = Color.White,
                        contentSpacing = 8.dp
                    )
                    HighlightCard(
                        modifier = Modifier.weight(1f),
                        title = "Top Category",
                        value = category.value,
                        description = "${formatCurrencyKmp(category.amount)} $amountSuffix",
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
                        value = day.value.toFormattedDate(),
                        description = "${formatCurrencyKmp(day.amount)} $amountSuffix",
                        backgroundColor = SegmentColor5,
                        titleColor = Color.White,
                        valueColor = Color.White,
                        contentSpacing = 8.dp
                    )

                    HighlightCard(
                        modifier = Modifier.weight(1f),
                        title = "Average Per Day",
                        value = formatCurrencyKmp(average),
                        description = dailyLabel,
                        backgroundColor = SegmentColor2,
                        titleColor = Color.White,
                        valueColor = Color.White,
                        contentSpacing = 8.dp
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingHighlightCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            // Loading title
            AnimatedShimmerBox(
                modifier = Modifier
                    .width(80.dp)
                    .height(14.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Loading value
            AnimatedShimmerBox(
                modifier = Modifier
                    .width(100.dp)
                    .height(18.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Loading description
            AnimatedShimmerBox(
                modifier = Modifier
                    .width(120.dp)
                    .height(12.dp)
            )
        }
    }
}

@Composable
fun ErrorHighlightCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error",
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Failed to load highlights",
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = GreenIncome),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text(
                    text = "Try Again",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
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
        modifier = modifier.height(100.dp),
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

    // Format the whole part with commas
    val wholeStr = whole.toString()
    val sb = StringBuilder()
    var count = 0
    for (i in wholeStr.length - 1 downTo 0) {
        sb.append(wholeStr[i])
        count++
        if (count % 3 == 0 && i != 0) {
            sb.append(',')
        }
    }
    val formattedWhole = sb.reverse().toString()

    return "Ksh $formattedWhole.${fraction.toString().padStart(2, '0')}"
}
