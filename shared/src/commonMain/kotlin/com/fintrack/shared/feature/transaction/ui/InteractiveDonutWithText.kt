package com.fintrack.shared.feature.transaction.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.fintrack.shared.feature.transaction.model.Category
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun InteractiveDonutWithText(
    categorySums: List<Pair<String, Double>>,
    totalAmount: Double,
    modifier: Modifier = Modifier,
    chartSize: Dp = 250.dp,
    gapPercentage: Float = 0.02f,
    segmentColors: List<Color> = SegmentColors,
    onSliceSelected: ((index: Int) -> Unit)? = null
) {
    if (categorySums.isEmpty() || totalAmount <= 0.0) return

    // State to track selected slice
    var selectedIndex by remember { mutableStateOf(-1) }

    // Each slice has its own DonutChartState
    val sliceStates = categorySums.map { remember { mutableStateOf(DonutChartState()) } }

    // Animate stroke width on selection
    val animatedStrokes = sliceStates.map {
        animateDpAsState(
            targetValue = it.value.stroke,
            animationSpec = tween(500)
        )
    }

    Box(modifier = modifier.size(chartSize), contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val anglesList =
                            calculateAnglesList(categorySums, totalAmount, gapPercentage)

                        handleCanvasTap(
                            center,
                            tapOffset,
                            anglesList,
                            selectedIndex,
                            animatedStrokes.map { it.value.toPx() },
                            onItemSelected = { index ->
                                selectedIndex = index
                                sliceStates[index].value =
                                    DonutChartState(DonutChartState.State.Selected)
                                onSliceSelected?.invoke(index)
                            },
                            onItemDeselected = { index ->
                                sliceStates[index].value =
                                    DonutChartState(DonutChartState.State.Unselected)
                            },
                            onNoItemSelected = { selectedIndex = -1 }
                        )
                    }
                }
        ) {
            // Draw arcs using incoming segmentColors
            val strokeWidthPx = 40.dp.toPx()
            val diameter = size.minDimension - strokeWidthPx
            var startAngle = -90f

            categorySums.forEachIndexed { index, (_, amount) ->
                val sweep = (((amount / totalAmount) * 360.0).toFloat()) - 360f * gapPercentage
                drawArc(
                    color = segmentColors[index % segmentColors.size],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
                    size = Size(diameter, diameter),
                    style = Stroke(
                        width = animatedStrokes[index].value.toPx(),
                        cap = StrokeCap.Butt
                    )
                )
                startAngle += sweep + 360f * gapPercentage
            }
        }

        // --- Center label with icon ---
        val display =
            if (selectedIndex >= 0) categorySums[selectedIndex] else "Total" to totalAmount
        val category = Category.fromName(display.first, isExpense = true) // assume expense
        val icon = category.toIcon()

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = display.first,
                modifier = Modifier.size(28.dp),
                tint = Color.Gray
            )
            Text(
                text = display.first,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Text(
                text = "ksh ${display.second.toInt()}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}


// --- Donut slice state ---
private class DonutChartState(
    val state: State = State.Unselected
) {
    val stroke: Dp
        get() = when (state) {
            State.Selected -> 60.dp
            State.Unselected -> 40.dp
        }

    enum class State { Selected, Unselected }
}

// --- Helper: calculate arcs ---
private data class DrawingAngles(val start: Float, val sweep: Float) {
    fun isInsideAngle(angle: Float): Boolean {
        val end = (start + sweep) % 360
        return if (start < end) angle in start..end else angle >= start || angle <= end
    }
}

private fun calculateAnglesList(
    data: List<Pair<String, Double>>,
    totalAmount: Double,
    gapPercentage: Float
): List<DrawingAngles> {
    val gapAngle = 360f * gapPercentage
    var startAngle = -90f
    return data.map { (_, amount) ->
        val sweep = ((amount / totalAmount * 360.0).toFloat()) - gapAngle
        val angle = DrawingAngles(startAngle, sweep)
        startAngle += sweep + gapAngle
        angle
    }
}


// --- Tap detection helpers ---
private fun handleCanvasTap(
    center: Offset,
    tapOffset: Offset,
    anglesList: List<DrawingAngles>,
    currentSelectedIndex: Int,
    currentStrokeValues: List<Float>,
    onItemSelected: (Int) -> Unit = {},
    onItemDeselected: (Int) -> Unit = {},
    onNoItemSelected: () -> Unit = {}
) {
    val normalized = tapOffset.findNormalizedPointFromTouch(center)
    val touchAngle = calculateTouchAngleAccordingToCanvas(center, normalized)
    val distance = findTouchDistanceFromCenter(center, normalized)

    var selectedIndex = -1
    var newDataTapped = false

    anglesList.forEachIndexed { index, angle ->
        val stroke = currentStrokeValues[index]
        if (angle.isInsideAngle(touchAngle)) {
            if (distance > (center.x - stroke) && distance < center.x) {
                selectedIndex = index
                newDataTapped = true
            }
        }
    }

    if (selectedIndex >= 0 && newDataTapped) onItemSelected(selectedIndex)
    if (currentSelectedIndex >= 0) {
        onItemDeselected(currentSelectedIndex)
        if (currentSelectedIndex == selectedIndex || !newDataTapped) onNoItemSelected()
    }
}

private fun findTouchDistanceFromCenter(center: Offset, touch: Offset) =
    sqrt((touch.x - center.x).pow(2) + (touch.y - center.y).pow(2))

private fun Offset.findNormalizedPointFromTouch(canvasCenter: Offset) =
    Offset(this.x, canvasCenter.y + (canvasCenter.y - this.y))

private fun calculateTouchAngleAccordingToCanvas(
    canvasCenter: Offset,
    normalizedPoint: Offset
): Float {
    val angle = calculateTouchAngleInDegrees(canvasCenter, normalizedPoint)
    return ((angle + 360) % 360).toFloat()
}

private fun calculateTouchAngleInDegrees(canvasCenter: Offset, normalizedPoint: Offset): Double {
    val rad = atan2(normalizedPoint.y - canvasCenter.y, normalizedPoint.x - canvasCenter.x)
    return rad * -180 / PI
}
