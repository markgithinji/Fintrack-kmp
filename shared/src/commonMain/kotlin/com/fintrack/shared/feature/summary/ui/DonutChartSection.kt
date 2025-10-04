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

//
//@Composable
//fun SimpleDonutChart(
//    categorySums: List<Pair<String, Double>>,
//    totalAmount: Double,
//    modifier: Modifier = Modifier,
//    chartSize: Dp = 200.dp,
//    gapPercentage: Float = 0.02f,
//    segmentColors: List<Color> = SegmentColors
//) {
//    if (categorySums.isEmpty() || totalAmount <= 0.0) return
//
//    Box(
//        modifier = modifier
//            .size(chartSize)
//            .fillMaxWidth(),
//        contentAlignment = Alignment.Center
//    ) {
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            val strokeWidthPx = 40.dp.toPx()
//            val diameter = size.minDimension - strokeWidthPx // ensures perfect circle
//
//            val gapAngle = 360f * gapPercentage
//            var startAngle = -90f
//
//            categorySums.forEachIndexed { index, (_, amount) ->
//                val sweep = ((amount / totalAmount) * 360f).toFloat() - gapAngle
//                drawArc(
//                    color = segmentColors[index % segmentColors.size],
//                    startAngle = startAngle,
//                    sweepAngle = sweep,
//                    useCenter = false,
//                    topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
//                    size = Size(diameter, diameter),
//                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
//                )
//                startAngle += sweep + gapAngle
//            }
//
//        }
//    }
//}
