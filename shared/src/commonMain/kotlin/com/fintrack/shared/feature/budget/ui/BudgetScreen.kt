package com.fintrack.shared.feature.budget.ui


import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.budget.domain.model.BudgetWithStatus
import com.fintrack.shared.feature.transaction.ui.addtransaction.AnimatedShimmerBox
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.pow
import kotlin.math.round
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = koinViewModel(),
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
                    item {
                        SexyAddBudgetButton(
                            onClick = onAddBudget,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    if (data.isEmpty()) {
                        item {
                            BudgetEmptyState(
                                onAddBudget = onAddBudget,
                                modifier = Modifier.fillMaxWidth()
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
fun SexyAddBudgetButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .shadow(
                elevation = if (isPressed) 8.dp else 16.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            awaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onTap = { onClick() }
                )
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated icon container
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val rotation by animateFloatAsState(
                        targetValue = if (isPressed) 90f else 0f,
                        animationSpec = tween(durationMillis = 200),
                        label = "add_icon_rotation"
                    )

                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.8f else 1f,
                        animationSpec = tween(durationMillis = 200),
                        label = "add_icon_scale"
                    )

                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Budget",
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer {
                                rotationZ = rotation
                                scaleX = scale
                                scaleY = scale
                            },
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Create New Budget",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Set up a spending or income plan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            // Chevron icon
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun BudgetEmptyState(
    onAddBudget: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration container
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "No Budgets Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Start by creating your first budget to track your finances",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        // Primary CTA button
        Button(
            onClick = onAddBudget,
            modifier = Modifier
                .height(54.dp)
                .fillMaxWidth(0.8f),
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
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Create Budget",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
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
                                    .fillMaxWidth(0.7f)
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

                        // Stats row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            repeat(3) {
                                AnimatedShimmerBox(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(40.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }

                    // Categories section (limited to 3 + more)
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
                        // "+X more" placeholder
                        AnimatedShimmerBox(
                            modifier = Modifier
                                .width(70.dp)
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
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
    val visibleCategories = budget.categories.take(3)
    val remainingCategoriesCount = budget.categories.size - 3

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

            // Categories with limit of 3
            if (budget.categories.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    // Show first 3 categories
                    visibleCategories.forEach { category ->
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    category.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                    // Show "+X more" if there are more than 3 categories
                    if (remainingCategoriesCount > 0) {
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+$remainingCategoriesCount more",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
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