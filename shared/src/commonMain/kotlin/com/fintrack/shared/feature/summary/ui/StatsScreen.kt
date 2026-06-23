package com.fintrack.shared.feature.summary.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.shared.feature.summary.domain.model.Period
import com.fintrack.shared.feature.summary.domain.model.TabType
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = koinViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val availableWeeks by viewModel.availableWeeks.collectAsStateWithLifecycle()
    val availableMonths by viewModel.availableMonths.collectAsStateWithLifecycle()
    val availableYears by viewModel.availableYears.collectAsStateWithLifecycle()
    val highlights by viewModel.highlights.collectAsStateWithLifecycle()
    val distributionResult by viewModel.distribution.collectAsStateWithLifecycle()

    val safePeriod = selectedPeriod ?: remember(availableWeeks, availableMonths, availableYears) {
        getDefaultPeriod(availableWeeks, availableMonths, availableYears)
    }

    // Load initial data
    LaunchedEffect(Unit) {
        viewModel.loadAvailablePeriods()
        viewModel.loadHighlights()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item(key = "screenHeader") {
            ScreenHeader(
                selectedTab = selectedTab,
                onTabSelected = { viewModel.onTabChanged(it) }
            )
        }

        item(key = "spacer1") { Spacer(Modifier.height(16.dp)) }

        item(key = "spendingHighlights") {
            SpendingHighlightsSection(
                tabType = selectedTab,
                highlightsResult = highlights,
                loadHighlights = { viewModel.loadHighlights() }
            )
        }

        item(key = "spacer2") { Spacer(Modifier.height(16.dp)) }

        item(key = "categoryTotals") {
            CategoryTotalsCardWithTabs(
                tabType = selectedTab,
                period = safePeriod,
                distributionResult = distributionResult,
                availableWeeks = availableWeeks,
                availableMonths = availableMonths,
                availableYears = availableYears,
                onWeekSelected = { week -> viewModel.onPeriodChanged(Period.Week(week)) },
                onMonthSelected = { month -> viewModel.onPeriodChanged(Period.Month(month)) },
                onYearSelected = { year -> viewModel.onPeriodChanged(Period.Year(year)) },
                onPeriodSelected = { period -> viewModel.onPeriodChanged(period) }
            )
        }
    }
}

// Helper function to create a default period
private fun getDefaultPeriod(
    availableWeeks: List<String>,
    availableMonths: List<String>,
    availableYears: List<String>
): Period {
    return when {
        availableWeeks.isNotEmpty() -> Period.Week(availableWeeks.first())
        availableMonths.isNotEmpty() -> Period.Month(availableMonths.first())
        availableYears.isNotEmpty() -> Period.Year(availableYears.first())
        else -> Period.Week("2024-W01") // Fallback default
    }
}


@Composable
fun ScreenHeader(
    selectedTab: TabType,
    onTabSelected: (TabType) -> Unit
) {
    val tabs = listOf(TabType.Income, TabType.Expense)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        tabs.forEach { tab ->
            TabItem(
                tab = tab,
                isSelected = tab == selectedTab,
                onClick = { onTabSelected(tab) }
            )
        }
    }
}

@Composable
fun TabItem(
    tab: TabType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(durationMillis = 200)
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = 200)
    )

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .clip(shape)
            .then(
                if (!isSelected) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = shape
                    )
                } else Modifier
            )
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        Text(
            text = tab.displayName,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
