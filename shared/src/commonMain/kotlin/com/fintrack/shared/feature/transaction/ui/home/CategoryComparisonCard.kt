package com.fintrack.shared.feature.transaction.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.settings.ui.toCurrencyString
import com.fintrack.shared.feature.core.util.formatToSinglePrecision
import com.fintrack.shared.feature.summary.domain.model.CategoryComparison
import com.fintrack.shared.feature.transaction.domain.model.Category
import com.fintrack.shared.feature.core.ui.AnimatedShimmerBox
import com.fintrack.shared.feature.transaction.ui.util.toColor
import com.fintrack.shared.feature.transaction.ui.util.toIcon

@Composable
fun CategoryComparisonCard(
    categoryComparisonResult: Result<List<CategoryComparison>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Text(
                text = "Category Trends",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (categoryComparisonResult) {
                is Result.Loading -> {
                    repeat(2) { index ->
                        LoadingCategoryComparisonItem()
                        if (index < 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 0.5.dp,
                                color = Color.LightGray.copy(alpha = 0.4f)
                            )
                        }
                    }
                }

                is Result.Error -> {
                    CategoryComparisonErrorState(
                        onRetry = { /* Add retry logic */ }
                    )
                }

                is Result.Success -> {
                    if (categoryComparisonResult.data.isEmpty()) {
                        CategoryComparisonEmptyState()
                    } else {
                        categoryComparisonResult.data.forEachIndexed { index, comparison ->
                            CategoryComparisonItem(
                                comparison = comparison,
                                isLast = index == categoryComparisonResult.data.lastIndex
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryComparisonItem(
    comparison: CategoryComparison,
    isLast: Boolean
) {
    val category = Category.fromName(
        comparison.category,
        comparison.currentTotal < 0 || comparison.previousTotal < 0
    )
    val icon = category.toIcon()
    val bgColor = category.toColor().copy(alpha = 0.15f)
    val iconTint = category.toColor()

    val positive = comparison.changePercentage >= 0
    val arrowIcon = if (positive)
        Icons.AutoMirrored.Outlined.TrendingUp
    else
        Icons.AutoMirrored.Outlined.TrendingDown

    val changeColor = if (positive)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.error

    val periodLabel = when (comparison.period.lowercase()) {
        "weekly" -> "week"
        "monthly" -> "month"
        "yearly" -> "year"
        else -> comparison.period
    }

    val changeText = if (positive) {
        "${comparison.changePercentage.formatToSinglePrecision()}% more than last $periodLabel"
    } else {
        "${(comparison.changePercentage * -1).formatToSinglePrecision()}% less than last $periodLabel"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(bgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = comparison.category,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content area
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = comparison.category,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = comparison.currentTotal.toCurrencyString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Change indicator
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = changeColor.copy(alpha = 0.1f)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = arrowIcon,
                            contentDescription = if (positive) "Increase" else "Decrease",
                            tint = changeColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = changeText,
                            style = MaterialTheme.typography.labelMedium,
                            color = changeColor,
                            maxLines = 2,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        if (!isLast) {
            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
private fun LoadingCategoryComparisonItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedShimmerBox(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            AnimatedShimmerBox(
                modifier = Modifier
                    .width(120.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(6.dp))
            AnimatedShimmerBox(
                modifier = Modifier
                    .width(80.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }

        AnimatedShimmerBox(
            modifier = Modifier
                .width(140.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    }
}

@Composable
private fun CategoryComparisonErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Unable to load trends",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Check your connection and try again",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = "Retry",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Again")
        }
    }
}

@Composable
private fun CategoryComparisonEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Analytics,
                contentDescription = "No Data",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No trends available",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Transaction data will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}