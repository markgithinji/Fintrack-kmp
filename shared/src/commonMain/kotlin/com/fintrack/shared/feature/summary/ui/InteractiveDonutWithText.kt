package com.fintrack.shared.feature.summary.ui

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
import com.fintrack.shared.feature.settings.ui.toCurrencyString
import com.fintrack.shared.feature.transaction.domain.model.Category
import com.fintrack.shared.feature.transaction.ui.util.toIcon
import kotlin.math.PI
import kotlin.math.atan2
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
                val allocatedAngle = ((amount / totalAmount) * 360.0).toFloat()
                val sweep = (allocatedAngle - 360f * gapPercentage).coerceAtLeast(0.5f)
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
                startAngle += allocatedAngle
            }
        }

        // --- Center label with conditional icon ---
        val isShowingOverallTotal = selectedIndex < 0
        val displayText = if (isShowingOverallTotal) "Total" else categorySums[selectedIndex].first
        val displayAmount = if (isShowingOverallTotal) totalAmount else categorySums[selectedIndex].second

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Show icon for ALL categories (including "Total" if it's in the list)
            // Only skip icon when showing overall summary (no slice selected)
            if (!isShowingOverallTotal) {
                val category = Category.fromName(displayText, isExpense = true)
                val icon = category.toIcon()

                Icon(
                    imageVector = icon,
                    contentDescription = displayText,
                    modifier = Modifier.size(28.dp),
                    tint = Color.Gray
                )
            }

            Text(
                text = displayText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Text(
                text = displayAmount.toCurrencyString(),
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
        val normalizedStart = (start % 360 + 360) % 360
        val end = (normalizedStart + sweep) % 360
        return if (normalizedStart < end) {
            angle in normalizedStart..end
        } else {
            angle >= normalizedStart || angle <= end
        }
    }
}

private fun calculateAnglesList(
    data: List<Pair<String, Double>>,
    totalAmount: Double,
    gapPercentage: Float
): List<DrawingAngles> {
    var startAngle = 270f // Start at 12 o'clock
    return data.map { (_, amount) ->
        val allocatedSweep = (amount / totalAmount * 360.0).toFloat()
        // We use the full allocated angle for hit testing so that even the gaps are clickable
        // and to prevent negative sweeps which break the 'isInsideAngle' logic.
        val angle = DrawingAngles(startAngle, allocatedSweep)
        startAngle += allocatedSweep
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
    val dx = tapOffset.x - center.x
    val dy = tapOffset.y - center.y
    val distance = sqrt(dx * dx + dy * dy)
    val tapAngle = (atan2(dy, dx) * 180f / PI.toFloat() + 360f) % 360f

    var selectedIndex = -1
    var newDataTapped = false

    anglesList.forEachIndexed { index, angle ->
        val stroke = currentStrokeValues[index]
        if (angle.isInsideAngle(tapAngle)) {
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
