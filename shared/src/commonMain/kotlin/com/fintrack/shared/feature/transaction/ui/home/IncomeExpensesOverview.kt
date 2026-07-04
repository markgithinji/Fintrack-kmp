package com.fintrack.shared.feature.transaction.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.GreenIncome
import com.example.compose.PinkExpense
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.summary.domain.model.OverviewSummary
import com.fintrack.shared.feature.core.util.shortDayName
import com.fintrack.shared.feature.settings.ui.toCurrencyString
import kotlinx.datetime.LocalDate

@Composable
fun IncomeExpensesOverview(overviewResult: Result<OverviewSummary>) {
    var selectedPeriod by remember { mutableStateOf(OverviewPeriod.Weekly) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        when (overviewResult) {
            is Result.Loading -> {
                OverviewLoadingState(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { period -> selectedPeriod = period }
                )
            }

            is Result.Error -> {
                OverviewErrorState(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { period -> selectedPeriod = period }
                )
            }

            is Result.Success -> {
                OverviewSuccessState(
                    overview = overviewResult.data,
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { period -> selectedPeriod = period }
                )
            }
        }
    }
}

@Composable
private fun OverviewLoadingState(
    selectedPeriod: OverviewPeriod,
    onPeriodSelected: (OverviewPeriod) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        OverviewHeader(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = onPeriodSelected
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = selectedPeriod,
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) togetherWith
                            fadeOut(animationSpec = tween(400))
                }
            ) { period ->
                when (period) {
                    OverviewPeriod.Weekly -> LoadingBarChart()
                    OverviewPeriod.Monthly -> LoadingLineChart()
                }
            }
        }
    }
}

@Composable
private fun OverviewErrorState(
    selectedPeriod: OverviewPeriod,
    onPeriodSelected: (OverviewPeriod) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        OverviewHeader(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = onPeriodSelected
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Failed to load overview",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun OverviewSuccessState(
    overview: OverviewSummary,
    selectedPeriod: OverviewPeriod,
    onPeriodSelected: (OverviewPeriod) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        OverviewHeader(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = onPeriodSelected
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            AnimatedContent(
                targetState = selectedPeriod,
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.92f)) togetherWith
                            fadeOut(animationSpec = tween(500))
                }
            ) { period ->
                when (period) {
                    OverviewPeriod.Weekly -> {
                        val weeklyData = overview.weeklyOverview.map {
                            val dayName = LocalDate.parse(it.date).shortDayName()
                            dayName to (it.income to it.expense)
                        }
                        BarChart(
                            data = weeklyData,
                            modifier = Modifier
                                .fillMaxHeight()
                        )
                    }

                    OverviewPeriod.Monthly -> {
                        CustomLineChart(
                            data = overview.monthlyOverview,
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(bottom = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewHeader(
    selectedPeriod: OverviewPeriod,
    onPeriodSelected: (OverviewPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                "Overview",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(GreenIncome, CircleShape))
                Text(
                    " Income",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(12.dp))
                Box(Modifier.size(8.dp).background(PinkExpense, CircleShape))
                Text(
                    " Expenses",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Pill-style Period Switcher (App Consistent)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OverviewPeriod.entries.forEach { period ->
                    val isSelected = period == selectedPeriod
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { onPeriodSelected(period) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = period.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BarChart(
    data: List<Pair<String, Pair<Double, Double>>>,
    modifier: Modifier = Modifier
) {
    val totalBarHeight = 180.dp
    val barWidth = 16.dp
    val density = LocalDensity.current
    val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val fullData = weekDays.map { day ->
        data.find { it.first == day } ?: (day to (0.0 to 0.0))
    }

    val maxTotal = fullData.maxOfOrNull { it.second.first + it.second.second } ?: 1.0
    val totalBarHeightPx = with(density) { totalBarHeight.toPx() }

    var selectedAmount by remember { mutableStateOf<Double?>(null) }
    var selectedColor by remember { mutableStateOf<Color>(Color.Transparent) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }
    var barsWidth by remember { mutableStateOf(0f) }

    // Use a root container that is clickable to dismiss
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { selectedAmount = null }
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(end = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                // Y-axis labels
                Column(
                    modifier = Modifier.width(28.dp).height(totalBarHeight),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    val levels = 5
                    val step = maxTotal / levels
                    for (i in levels downTo 0) {
                        val value = step * i
                        val text = if (value >= 1000) "${(value / 1000).toInt()}k" else value.toInt().toString()
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Interactive Bars Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(totalBarHeight)
                        .onGloballyPositioned { barsWidth = it.size.width.toFloat() }
                        .pointerInput(fullData, maxTotal) {
                            detectTapGestures { offset ->
                                if (barsWidth <= 0) return@detectTapGestures
                                
                                val barAreaWidth = barsWidth / 7f
                                val index = (offset.x / barAreaWidth).toInt().coerceIn(0, 6)
                                val values = fullData[index].second
                                
                                val incomeH = (values.first / maxTotal).toFloat() * totalBarHeightPx
                                val expenseH = (values.second / maxTotal).toFloat() * totalBarHeightPx
                                
                                val incomeTop = totalBarHeightPx - incomeH
                                val expenseTop = incomeTop - expenseH
                                
                                // Increased hit-test sensitivity: allows touching anywhere in the bar's column
                                // instead of just the narrow 16.dp bar width.
                                val isWithinBarX = true 

                                if (isWithinBarX) {
                                    if (offset.y in (expenseTop - 10.dp.toPx())..incomeTop) {
                                        selectedAmount = values.second
                                        selectedColor = PinkExpense
                                        touchOffset = offset
                                    } else if (offset.y in incomeTop..(totalBarHeightPx + 10.dp.toPx())) {
                                        selectedAmount = values.first
                                        selectedColor = GreenIncome
                                        touchOffset = offset
                                    } else {
                                        selectedAmount = null
                                    }
                                }
                            }
                        }
                ) {
                    // Visual Bars
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        fullData.forEach { (label, values) ->
                            val incomeHeightFraction = (values.first / maxTotal).toFloat()
                            val expenseHeightFraction = (values.second / maxTotal).toFloat()

                            Box(
                                modifier = Modifier
                                    .width(barWidth)
                                    .height(totalBarHeight)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                // Income bar
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxHeight(incomeHeightFraction.coerceAtLeast(0.01f))
                                        .width(barWidth)
                                        .background(GreenIncome, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                )

                                // Expense stacked on income
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxHeight(expenseHeightFraction.coerceAtLeast(0.01f))
                                        .width(barWidth)
                                        .offset(y = -totalBarHeight * incomeHeightFraction)
                                        .background(PinkExpense, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                )
                            }
                        }
                    }

                    // Popup Overlay (Floating Layer - Absolutely positioned within Chart Area)
                    androidx.compose.animation.AnimatedVisibility(
                        visible = selectedAmount != null,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut(),
                        modifier = Modifier.offset {
                            IntOffset(
                                x = (touchOffset.x - 40.dp.toPx()).toInt(),
                                y = (touchOffset.y - 45.dp.toPx()).toInt()
                            )
                        }
                    ) {
                        Surface(
                            color = selectedColor,
                            shape = RoundedCornerShape(8.dp),
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                text = selectedAmount?.toCurrencyString() ?: "",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day labels aligned to bars
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 36.dp), // Match chart area start offset
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                weekDays.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

enum class OverviewPeriod {
    Weekly, Monthly
}
