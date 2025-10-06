package com.fintrack.shared.feature.summary.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

// Chart segments
val SegmentColor1 = Color(0xFFE63946) // Strong red
val SegmentColor2 = Color(0xFF228B22) // Forest Green
val SegmentColor3 = Color(0xFF457B9D) // Vibrant blue
val SegmentColor4 = Color(0xFFF4A261) // Warm orange
val SegmentColor5 = Color(0xFF2A9D8F) // Teal / turquoise

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = koinViewModel()
) {
    // --- Collect UI state ---
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val availableWeeks by viewModel.availableWeeks.collectAsStateWithLifecycle()
    val availableMonths by viewModel.availableMonths.collectAsStateWithLifecycle()
    val availableYears by viewModel.availableYears.collectAsStateWithLifecycle()
    val highlights by viewModel.highlights.collectAsStateWithLifecycle()
    val distributionResult by viewModel.distribution.collectAsStateWithLifecycle()

    // --- Load initial data ---
    LaunchedEffect(Unit) {
        viewModel.loadAvailablePeriods()
        viewModel.loadHighlights()
    }

    val safePeriod =
        selectedPeriod ?: getDefaultPeriod(availableWeeks, availableMonths, availableYears)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // --- Screen header (Income / Expense tabs) ---
        item(key = "screenHeader") {
            ScreenHeader(
                selectedTab = selectedTab,
                onTabSelected = viewModel::onTabChanged
            )
        }

        item(key = "spacer1") { Spacer(Modifier.height(16.dp)) }

        // --- Spending highlights ---
        item(key = "spendingHighlights") {
            SpendingHighlightsSection(
                tabType = selectedTab,
                highlightsResult = highlights,
                loadHighlights = viewModel::loadHighlights
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
    val backgroundColor = if (isSelected) Color.Black else Color.Transparent
    val textColor = if (isSelected) Color.White else Color.Black
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .clip(shape)
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        Text(
            text = tab.displayName,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}
