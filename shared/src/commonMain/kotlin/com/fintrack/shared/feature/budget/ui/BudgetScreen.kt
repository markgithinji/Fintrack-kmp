package com.fintrack.shared.feature.budget.ui


import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.budget.domain.Budget
import com.fintrack.shared.feature.budget.domain.BudgetWithStatus
import com.fintrack.shared.feature.transaction.ui.addtransaction.AnimatedShimmerBox
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlin.math.pow
import kotlin.math.round

@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = viewModel(),
    onAddBudget: () -> Unit = {},
    onBudgetClick: (BudgetWithStatus) -> Unit
) {
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        when (budgets) {
            is Result.Loading -> BudgetScreenLoadingState()

            is Result.Error -> {
                BudgetErrorRetryState(
                    errorMessage = "Unable to load your budgets",
                    onRetry = { viewModel.reloadBudgets() }
                )
            }

            is Result.Success -> {
                val data = (budgets as Result.Success<List<BudgetWithStatus>>).data
                LazyColumn {
                    // Always show "+" at the top
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAddBudget() }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Budget"
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Add Budget")
                        }
                    }

                    if (data.isEmpty()) {
                        item {
                            Text(
                                "No budgets yet. Add one to get started.",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        items(data) { budgetWithStatus ->
                            BudgetItem(
                                budgetWithStatus = budgetWithStatus,
                                onClick = { onBudgetClick(budgetWithStatus) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetErrorRetryState(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRetryLoading by remember { mutableStateOf(false) }

    LaunchedEffect(isRetryLoading) {
        if (isRetryLoading) {
            isRetryLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        var isAnimating by remember { mutableStateOf(false) }
        val rotation by animateFloatAsState(
            targetValue = if (isAnimating) 10f else 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "error_icon_rotation"
        )

        LaunchedEffect(Unit) {
            isAnimating = true
        }

        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = "Error",
                modifier = Modifier
                    .size(60.dp)
                    .graphicsLayer {
                        rotationZ = rotation
                    },
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Oops!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Please check your connection and try again",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Retry button
        Button(
            onClick = {
                isRetryLoading = true
                onRetry()
            },
            modifier = Modifier
                .height(54.dp)
                .fillMaxWidth(0.7f),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 4.dp
            )
        ) {
            if (isRetryLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Retry",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Try Again",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Secondary action
        TextButton(
            onClick = { /* Optional: Add support action */ },
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text(
                text = "Contact Support",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun BudgetScreenLoadingState() {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Add Budget loading item
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon placeholder
                AnimatedShimmerBox(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Text placeholder
                AnimatedShimmerBox(
                    modifier = Modifier
                        .width(100.dp)
                        .height(20.dp)
                )
            }
        }

        // Loading budget items
        items(5) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header row: Name + Type chip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Budget name placeholder
                        AnimatedShimmerBox(
                            modifier = Modifier
                                .width(120.dp)
                                .height(24.dp)
                        )

                        // Type chip placeholder
                        AnimatedShimmerBox(
                            modifier = Modifier
                                .width(60.dp)
                                .height(28.dp)
                                .clip(RoundedCornerShape(50))
                        )
                    }

                    // Limit + Status section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Limit placeholder
                        AnimatedShimmerBox(
                            modifier = Modifier
                                .width(100.dp)
                                .height(18.dp)
                        )

                        // Spent placeholder
                        AnimatedShimmerBox(
                            modifier = Modifier
                                .width(80.dp)
                                .height(16.dp)
                        )

                        // Remaining placeholder
                        AnimatedShimmerBox(
                            modifier = Modifier
                                .width(90.dp)
                                .height(16.dp)
                        )

                        // Progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.LightGray.copy(alpha = 0.3f))
                        ) {
                            AnimatedShimmerBox(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f) // Random progress
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(50))
                            )
                        }

                        // Percentage placeholder
                        AnimatedShimmerBox(
                            modifier = Modifier
                                .width(50.dp)
                                .height(14.dp)
                        )
                    }

                    // Categories section
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(3) {
                            AnimatedShimmerBox(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(50))
                            )
                        }
                    }

                    // Period placeholder
                    AnimatedShimmerBox(
                        modifier = Modifier
                            .width(180.dp)
                            .height(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetItem(
    budgetWithStatus: BudgetWithStatus,
    onClick: () -> Unit
) {
    val budget = budgetWithStatus.budget
    val status = budgetWithStatus.status

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header row: Name + Type chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = budget.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Box(
                    modifier = Modifier
                        .background(
                            color = if (budget.isExpense)
                                MaterialTheme.colorScheme.errorContainer
                            else
                                MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (budget.isExpense) "Expense" else "Income",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (budget.isExpense)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Limit + Status aligned to left
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Progress bar first
                LinearProgressIndicator(
                    progress = { ((status.percentageUsed / 100.0).coerceAtMost(1.0)).toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = if (status.isExceeded) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )

                // Percentage text
                Text(
                    text = "${status.percentageUsed.roundToDecimals(1)}% used",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (status.isExceeded) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Top
                ) {
                    // Limit
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.width(80.dp)
                    ) {
                        Text(
                            text = "Limit",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${budget.limit}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Spent
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.width(80.dp)
                    ) {
                        Text(
                            text = "Spent",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${status.spent}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Remaining
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.width(100.dp)
                    ) {
                        Text(
                            text = "Remaining",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${status.remaining}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (status.isExceeded) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Categories
            if (budget.categories.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    budget.categories.forEach { category ->
                        AssistChip(
                            onClick = {},
                            label = { Text(category.name) }
                        )
                    }
                }
            }

            Text(
                text = "${formatBudgetDate(budget.startDate)} - ${formatBudgetDate(budget.endDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun formatBudgetDate(localDate: LocalDate): String {
    val day = localDate.dayOfMonth
    val monthName = when (localDate.month) {
        Month.JANUARY -> "Jan"
        Month.FEBRUARY -> "Feb"
        Month.MARCH -> "Mar"
        Month.APRIL -> "Apr"
        Month.MAY -> "May"
        Month.JUNE -> "Jun"
        Month.JULY -> "Jul"
        Month.AUGUST -> "Aug"
        Month.SEPTEMBER -> "Sept"
        Month.OCTOBER -> "Oct"
        Month.NOVEMBER -> "Nov"
        Month.DECEMBER -> "Dec"
        else -> {}
    }
    val year = localDate.year.toString().takeLast(2)
    return "$day $monthName $year"
}
fun Double.roundToDecimals(decimals: Int): String {
    val factor = 10.0.pow(decimals)
    return (round(this * factor) / factor).toString()
}