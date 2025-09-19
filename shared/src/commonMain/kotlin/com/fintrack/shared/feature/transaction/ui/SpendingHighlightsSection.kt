package com.fintrack.shared.feature.transaction.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.summary.domain.Highlight
import com.fintrack.shared.feature.summary.domain.HighlightsSummary
import com.fintrack.shared.feature.summary.ui.SegmentColor2
import com.fintrack.shared.feature.summary.ui.SegmentColor3
import com.fintrack.shared.feature.summary.ui.SegmentColor4
import com.fintrack.shared.feature.summary.ui.SegmentColor5
import com.fintrack.shared.feature.summary.ui.TabType

@Composable
fun SpendingHighlightsSection(
    tabType: TabType,
    highlightsResult: Result<HighlightsSummary>,
    loadHighlights: () -> Unit
) {
    LaunchedEffect(Unit) {
        loadHighlights()
    }

    when (val highlights = highlightsResult) {
        is Result.Loading -> Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        is Result.Error -> Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Error: ${highlights.exception.message ?: "Unknown"}",
                color = Color.Red
            )
        }

        is Result.Success -> {
            val data = highlights.data
            val summaryHighlights = when (tabType) {
                is TabType.Income -> data.incomeHighlights
                is TabType.Expense -> data.expenseHighlights
            }

            // Provide defaults if null
            val month = summaryHighlights.highestMonth ?: Highlight("", "", 0.0)
            val category = summaryHighlights.highestCategory ?: Highlight("", "", 0.0)
            val day = summaryHighlights.highestDay ?: Highlight("", "", 0.0)
            val average = summaryHighlights.averagePerDay

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
