package com.fintrack.shared.feature.summary.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    val onTabSelected = remember { { tab: TabType -> viewModel.onTabChanged(tab) } }
    val loadHighlights = remember { { viewModel.loadHighlights() } }

    val onPeriodSelected = remember { { period: Period -> viewModel.onPeriodChanged(period) } }

    val safePeriod by remember(selectedPeriod, availableWeeks, availableMonths, availableYears) {
        derivedStateOf {
            selectedPeriod ?: getDefaultPeriod(availableWeeks, availableMonths, availableYears)
        }
    }

    val onWeekSelected = remember(onPeriodSelected) {
        { week: String -> onPeriodSelected(Period.Week(week)) }
    }
    val onMonthSelected = remember(onPeriodSelected) {
        { month: String -> onPeriodSelected(Period.Month(month)) }
    }
    val onYearSelected = remember(onPeriodSelected) {
        { year: String -> onPeriodSelected(Period.Year(year)) }
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
                onTabSelected = onTabSelected
            )
        }

        item(key = "spacer1") { Spacer(Modifier.height(16.dp)) }

        item(key = "spendingHighlights") {
            SpendingHighlightsSection(
                tabType = selectedTab,
                highlightsResult = highlights,
                loadHighlights = loadHighlights
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
                onWeekSelected = onWeekSelected,
                onMonthSelected = onMonthSelected,
                onYearSelected = onYearSelected,
                onPeriodSelected = onPeriodSelected
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
        targetValue = if (isSelected) Color.Black else Color.Transparent,
        animationSpec = tween(durationMillis = 200)
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.Black,
        animationSpec = tween(durationMillis = 200)
    )

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .clip(shape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        Text(
            text = tab.displayName,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}
