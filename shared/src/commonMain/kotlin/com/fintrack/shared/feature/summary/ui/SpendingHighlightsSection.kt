package com.fintrack.shared.feature.summary.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.GreenIncome
import com.example.compose.SegmentColor2
import com.example.compose.SegmentColor3
import com.example.compose.SegmentColor4
import com.example.compose.SegmentColor5
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.settings.ui.toCurrencyString
import com.fintrack.shared.feature.summary.domain.model.Highlight
import com.fintrack.shared.feature.summary.domain.model.StatisticsSummary
import com.fintrack.shared.feature.summary.domain.model.TabType
import com.fintrack.shared.feature.summary.ui.util.toFormattedDate
import com.fintrack.shared.feature.summary.ui.util.toMonthName
import com.fintrack.shared.feature.core.ui.AnimatedShimmerBox

@Composable
fun SpendingHighlightsSection(
    tabType: TabType,
    highlightsResult: Result<StatisticsSummary>,
    loadHighlights: () -> Unit
) {
    val sectionTitle = when (tabType) {
        TabType.Income -> "Income Highlights"
        TabType.Expense -> "Spending Highlights"
    }

    val amountSuffix = when (tabType) {
        TabType.Income -> "received"
        TabType.Expense -> "spent"
    }

    val dailyLabel = when (tabType) {
        TabType.Income -> "Daily Income"
        TabType.Expense -> "Daily Spending"
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = sectionTitle,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Crossfade(
            targetState = highlightsResult,
            animationSpec = tween(durationMillis = 300)
        ) { result ->
            when (result) {
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
                        message = result.exception.message ?: "Failed to load highlights",
                        onRetry = loadHighlights
                    )
                }

                is Result.Success -> {
                    val data = result.data
                    SuccessContent(
                        tabType = tabType,
                        data = data,
                        amountSuffix = amountSuffix,
                        dailyLabel = dailyLabel
                    )
                }
            }
        }
    }
}

@Composable
private fun SuccessContent(
    tabType: TabType,
    data: StatisticsSummary,
    amountSuffix: String,
    dailyLabel: String
) {
    val summaryHighlights = when (tabType) {
        TabType.Income -> data.incomeHighlights
        TabType.Expense -> data.expenseHighlights
    }

    // Provide defaults if null
    val month = summaryHighlights.highestMonth ?: Highlight("", "", 0.0)
    val category = summaryHighlights.highestCategory ?: Highlight("", "", 0.0)
    val day = summaryHighlights.highestDay ?: Highlight("", "", 0.0)
    val average = summaryHighlights.averagePerDay

    Column {
        // First row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HighlightCard(
                modifier = Modifier.weight(1f),
                title = "Highest Month",
                value = month.value.toMonthName(),
                description = "${month.amount.toCurrencyString()} $amountSuffix",
                backgroundColor = SegmentColor3,
                titleColor = Color.White,
                valueColor = Color.White
            )
            HighlightCard(
                modifier = Modifier.weight(1f),
                title = "Top Category",
                value = category.value,
                description = "${category.amount.toCurrencyString()} $amountSuffix",
                backgroundColor = SegmentColor4,
                titleColor = Color.White,
                valueColor = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Second row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HighlightCard(
                modifier = Modifier.weight(1f),
                title = "Highest Daily",
                value = day.value.toFormattedDate(),
                description = "${day.amount.toCurrencyString()} $amountSuffix",
                backgroundColor = SegmentColor5,
                titleColor = Color.White,
                valueColor = Color.White
            )
            HighlightCard(
                modifier = Modifier.weight(1f),
                title = "Average Per Day",
                value = average.toCurrencyString(),
                description = dailyLabel,
                backgroundColor = SegmentColor2,
                titleColor = Color.White,
                valueColor = Color.White
            )
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
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
    val animatedBackground by animateColorAsState(
        targetValue = backgroundColor,
        animationSpec = tween(durationMillis = 300)
    )

    val descriptionColor = titleColor.copy(alpha = 0.7f)

    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = animatedBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(contentSpacing))

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(contentSpacing))

            Text(
                text = description,
                fontSize = 12.sp,
                color = descriptionColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
