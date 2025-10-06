package com.fintrack.shared.feature.summary.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


@Composable
fun DonutChartSection(categorySums: List<Pair<String, Float>>, totalAmount: Float) {
    if (categorySums.isEmpty() || totalAmount <= 0f) return

    // Sort and prepare top categories + "Others"
    val sortedForChart = categorySums.sortedByDescending { it.second }
    val topForChart = sortedForChart.take(4).toMutableList()
    val othersTotal = sortedForChart.drop(4).sumOf { it.second.toDouble() }.toFloat()
    if (othersTotal > 0f) topForChart.add("Others" to othersTotal)

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val chartColors = topForChart.mapIndexed { index, _ ->
            if (index < 4) SegmentColors[index] else SegmentColors.last()
        }

        // Interactive Donut Chart
        InteractiveDonutWithText(
            categorySums = topForChart.map { it.first to it.second.toDouble() },
            totalAmount = totalAmount.toDouble(),
            segmentColors = chartColors
        )
    }
}