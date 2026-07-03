package com.fintrack.shared.feature.summary.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.GreenIncome
import com.example.compose.PinkExpense
import com.example.compose.SegmentColor1
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
import com.fintrack.shared.feature.core.ui.CommonErrorState

@Composable
fun SpendingHighlightsSection(
    tabType: TabType,
    highlightsResult: Result<StatisticsSummary>,
    loadHighlights: () -> Unit
) {
    val sectionTitle = when (tabType) {
        TabType.Income -> "Income Overview"
        TabType.Expense -> "Spending Overview"
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = sectionTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.heightIn(min = 250.dp)) {
            Crossfade(
                targetState = highlightsResult,
                animationSpec = tween(durationMillis = 300)
            ) { result ->
                when (result) {
                    is Result.Loading -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                LoadingHighlightCard(modifier = Modifier.weight(1f))
                                LoadingHighlightCard(modifier = Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        SuccessContent(tabType, result.data)
                    }
                }
            }
        }
    }
}

@Composable
private fun SuccessContent(
    tabType: TabType,
    data: StatisticsSummary
) {
    val highlights = when (tabType) {
        TabType.Income -> data.incomeHighlights
        TabType.Expense -> data.expenseHighlights
    }

    val isIncome = tabType == TabType.Income
    val accentColor = if (isIncome) GreenIncome else PinkExpense

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HighlightCard(
                modifier = Modifier.weight(1f),
                title = "Busiest Month",
                value = highlights.highestMonth?.value?.toMonthName() ?: "N/A",
                subValue = highlights.highestMonth?.amount?.toCurrencyString() ?: "$0",
                icon = Icons.Default.CalendarMonth,
                color = SegmentColor3
            )
            HighlightCard(
                modifier = Modifier.weight(1f),
                title = "Top Category",
                value = highlights.highestCategory?.value ?: "N/A",
                subValue = highlights.highestCategory?.amount?.toCurrencyString() ?: "$0",
                icon = Icons.Default.Category,
                color = SegmentColor4
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HighlightCard(
                modifier = Modifier.weight(1f),
                title = "Peak Day",
                value = highlights.highestDay?.value?.toFormattedDate() ?: "N/A",
                subValue = highlights.highestDay?.amount?.toCurrencyString() ?: "$0",
                icon = Icons.Default.Today,
                color = SegmentColor5
            )
            HighlightCard(
                modifier = Modifier.weight(1f),
                title = "Daily Avg",
                value = highlights.averagePerDay.toCurrencyString(),
                subValue = if (isIncome) "Avg. Income" else "Avg. Expense",
                icon = Icons.AutoMirrored.Filled.ShowChart,
                color = SegmentColor2
            )
        }
    }
}

@Composable
fun HighlightCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subValue: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subValue,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = color
                )
            }
        }
    }
}

@Composable
fun LoadingHighlightCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AnimatedShimmerBox(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                AnimatedShimmerBox(
                    modifier = Modifier
                        .width(60.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                AnimatedShimmerBox(
                    modifier = Modifier
                        .width(100.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                AnimatedShimmerBox(
                    modifier = Modifier
                        .width(80.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Composable
fun ErrorHighlightCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        CommonErrorState(
            modifier = Modifier.padding(16.dp),
            title = "Oops! Something went wrong",
            errorMessage = message,
            onRetry = onRetry
        )
    }
}
