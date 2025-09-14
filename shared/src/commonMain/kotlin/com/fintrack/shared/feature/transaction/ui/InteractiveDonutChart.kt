package com.fintrack.shared.feature.transaction.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun InteractiveDonutChart(
    categorySums: List<Pair<String, Double>>,
    totalAmount: Double,
    modifier: Modifier = Modifier,
    chartSize: Dp = 200.dp,
    gapPercentage: Float = 0.02f,
    segmentColors: List<Color> = SegmentColors,
    onSegmentSelected: (Int) -> Unit = {}
) {
    if (categorySums.isEmpty() || totalAmount <= 0.0) return

    val strokeUnselected = 30.dp
    val strokeSelected = 50.dp

    var selectedIndex by remember { mutableStateOf(-1) }

    // Track state per segment
    val segmentStates = categorySums.mapIndexed { index, _ ->
        remember { mutableStateOf(DonutChartState()) }
    }

    // Animate stroke change
    val strokeValues = segmentStates.map { state ->
        animateDpAsState(
            targetValue = state.value.stroke,
            animationSpec = tween(durationMillis = 700)
        )
    }

    Box(
        modifier = modifier.size(chartSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val normalized = tapOffset.findNormalizedPointFromTouch(center)
                        val touchAngle = calculateTouchAngleAccordingToCanvas(center, normalized)
                        val distance = findTouchDistanceFromCenter(center, normalized)

                        val gapAngle = 360f * gapPercentage
                        val diameter = min(size.width.toFloat(), size.height.toFloat())
                        var startAngle = -90f
                        var tappedIndex = -1

                        categorySums.forEachIndexed { index, (_, amount) ->
                            val sweep = (((amount / totalAmount) * 360.0).toFloat()) - gapAngle
                            val segment = DrawingAngles(startAngle, sweep)
                            val radius = diameter / 2f
                            val strokePx = strokeValues[index].value.toPx()
                            val strokeHalf = strokePx / 2f

                            if (segment.isInsideAngle(touchAngle) &&
                                distance in (radius - strokeHalf)..(radius + strokeHalf)
                            ) {
                                tappedIndex = index
                            }

                            startAngle += sweep + gapAngle
                        }



                        if (tappedIndex != -1) {
                            if (selectedIndex != -1) {
                                segmentStates[selectedIndex].value =
                                    DonutChartState(DonutChartState.State.Unselected)
                            }
                            segmentStates[tappedIndex].value =
                                DonutChartState(DonutChartState.State.Selected)
                            selectedIndex = tappedIndex
                            onSegmentSelected(tappedIndex)
                        }
                    }
                }
        )
        {
            val diameter = size.minDimension
            val gapAngle = 360f * gapPercentage
            var startAngle = -90f

            categorySums.forEachIndexed { index, (_, amount) ->
                val sweep = ((amount / totalAmount) * 360f).toFloat() - gapAngle
                drawArc(
                    color = segmentColors[index % segmentColors.size],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset.Zero,
                    size = Size(diameter, diameter),
                    style = Stroke(
                        width = strokeValues[index].value.toPx(),
                        cap = StrokeCap.Butt
                    )
                )
                startAngle += sweep + gapAngle
            }
        }
    }
}

// State data class
private class DonutChartState(
    val state: State = State.Unselected
) {
    val stroke: Dp
        get() = when (state) {
            State.Selected -> 50.dp
            State.Unselected -> 30.dp
        }

    enum class State {
        Selected, Unselected
    }
}

// Math utilities
private fun findTouchDistanceFromCenter(center: Offset, touch: Offset) =
    sqrt((touch.x - center.x).pow(2) + (touch.y - center.y).pow(2))

private fun Offset.findNormalizedPointFromTouch(canvasCenter: Offset) =
    Offset(this.x, canvasCenter.y + (canvasCenter.y - this.y))

private fun calculateTouchAngleAccordingToCanvas(
    canvasCenter: Offset,
    normalizedPoint: Offset
): Float {
    val angle = calculateTouchAngleInDegrees(canvasCenter, normalizedPoint)
    return adjustAngleToCanvas(angle).toFloat()
}

private fun calculateTouchAngleInDegrees(canvasCenter: Offset, normalizedPoint: Offset): Double {
    val touchInRadian =
        atan2(normalizedPoint.y - canvasCenter.y, normalizedPoint.x - canvasCenter.x)
    return touchInRadian * -180 / PI
}

private fun adjustAngleToCanvas(angle: Double) = (angle + 360.0) % 360.0

private data class DrawingAngles(val start: Float, val sweep: Float) {
    fun isInsideAngle(angle: Float): Boolean {
        val end = (start + sweep + 360f) % 360f
        return if (start < end) angle in start..end else angle >= start || angle <= end
    }
}
