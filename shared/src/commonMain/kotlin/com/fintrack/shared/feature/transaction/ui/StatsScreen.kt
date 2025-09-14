package com.fintrack.shared.feature.transaction.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// Chart segments
val SegmentColor1 = Color(0xFFE63946) // Strong red
val SegmentColor2 = Color(0xFF228B22) // Forest Green
val SegmentColor3 = Color(0xFF457B9D) // Vibrant blue
val SegmentColor4 = Color(0xFFF4A261) // Warm orange
val SegmentColor5 = Color(0xFF2A9D8F) // Teal / turquoise

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = viewModel()
) {
    // --- Collect UI state ---
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val selectedWeek by viewModel.selectedWeek.collectAsStateWithLifecycle()
    val availableWeeks by viewModel.availableWeeks.collectAsStateWithLifecycle()
    val highlights by viewModel.highlights.collectAsStateWithLifecycle()
    val distributionResult by viewModel.distribution.collectAsStateWithLifecycle()

    // --- Load initial data ---
    LaunchedEffect(Unit) {
        viewModel.loadAvailableWeeks()
        viewModel.loadHighlights()
    }

    // --- Remember scroll state ---
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // --- Screen header ---
        item(key = "screenHeader") {
            val onTabSelected: (TabType) -> Unit = remember(viewModel) { viewModel::onTabChanged }
            ScreenHeader(
                selectedTab = selectedTab.name.capitalize(),
                onTabSelected = { tabString ->
                    val tab = if (tabString.equals("Income", true)) TabType.INCOME else TabType.EXPENSE
                    onTabSelected(tab)
                }
            )
        }

        // --- Spacer ---
        item(key = "headerSpacer") { Spacer(Modifier.height(16.dp)) }

        // --- Spending highlights ---
        item(key = "spendingHighlights") {
            val loadHighlights = remember(viewModel) { viewModel::loadHighlights }
            SpendingHighlightsSection(
                tabType = selectedTab.name.capitalize(),
                highlightsResult = highlights,
                loadHighlights = loadHighlights
            )
        }

        item(key = "highlightsSpacer") { Spacer(Modifier.height(16.dp)) }

        // --- Category totals ---
        if (selectedPeriod == Period.WEEK && selectedWeek != null) {
            item(key = selectedWeek!!) {
                val onWeekSelected = remember(viewModel) { viewModel::onWeekChanged }
                CategoryTotalsCardWithTabs(
                    tabType = selectedTab.name.capitalize(),
                    period = selectedPeriod.name.lowercase(),
                    value = selectedWeek!!,
                    distributionResult = distributionResult,
                    availableWeeks = availableWeeks,
                    onWeekSelected = onWeekSelected
                )
            }
        }
    }
}




@Composable
fun ScreenHeader(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    val tabs = listOf("All", "Income", "Expenses")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        tabs.forEach { tab ->
            TabItem(
                text = tab,
                isSelected = tab == selectedTab,
                onClick = { onTabSelected(tab) }
            )
        }
    }
}

@Composable
fun TabItem(
    text: String,
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
        Text(text = text, color = textColor, fontWeight = FontWeight.SemiBold)
    }
}
