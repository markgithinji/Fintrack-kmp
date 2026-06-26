package com.fintrack.shared.feature.summary.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
    gapPercentage: Float = 0.03f,
    segmentColors: List<Color> = SegmentColors,
    onSliceSelected: ((index: Int) -> Unit)? = null
) {
    if (categorySums.isEmpty() || totalAmount <= 0.0) return

    // State to track selected slice
    var selectedIndex by remember { mutableStateOf(-1) }

    // Entrance animation
    val entranceProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entranceProgress.animateTo(1f, animationSpec = tween(1000))
    }

    // Each slice has its own DonutChartState
    val sliceStates = categorySums.map { remember { mutableStateOf(DonutChartState()) } }

    // Animate stroke width on selection
    val animatedStrokes = sliceStates.map {
        animateDpAsState(
            targetValue = it.value.stroke,
            animationSpec = tween(300)
        )
    }

    val chartScale by animateFloatAsState(
        targetValue = if (entranceProgress.value < 1f) 0.8f + (0.2f * entranceProgress.value) else 1f,
        animationSpec = tween(500)
    )

    Box(
        modifier = modifier
            .size(chartSize)
            .scale(chartScale), 
        contentAlignment = Alignment.Center
    ) {
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
            val baseStrokeWidthPx = 36.dp.toPx()
            val diameter = size.minDimension - 80.dp.toPx() // More room for selected stroke
            var startAngle = -90f

            categorySums.forEachIndexed { index, (_, amount) ->
                val allocatedAngle = ((amount / totalAmount) * 360.0).toFloat()
                val sweep = (allocatedAngle * entranceProgress.value - 360f * gapPercentage).coerceAtLeast(0.1f)
                
                val isSelected = selectedIndex == index
                val strokeWidth = animatedStrokes[index].value.toPx()
                
                val color = segmentColors[index % segmentColors.size]
                
                // Draw a subtle shadow/glow if selected
                if (isSelected) {
                    drawArc(
                        color = color.copy(alpha = 0.2f),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2),
                        size = Size(diameter, diameter),
                        style = Stroke(
                            width = strokeWidth + 8.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                }

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2),
                    size = Size(diameter, diameter),
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
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
            if (!isShowingOverallTotal) {
                val category = Category.fromName(displayText, isExpense = true)
                val icon = category.toIcon()

                Icon(
                    imageVector = icon,
                    contentDescription = displayText,
                    modifier = Modifier.size(32.dp),
                    tint = segmentColors[selectedIndex % segmentColors.size]
                )
            } else {
                Text(
                    text = "📊",
                    fontSize = 28.sp
                )
            }

            Text(
                text = displayText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = displayAmount.toCurrencyString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
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
            State.Selected -> 52.dp
            State.Unselected -> 36.dp
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
        // Hit box check: between inner and outer radius
        val outerRadius = center.x 
        val innerRadius = center.x - stroke * 2 
        
        if (angle.isInsideAngle(tapAngle)) {
            if (distance > innerRadius && distance < outerRadius) {
                selectedIndex = index
                newDataTapped = true
            }
        }
    }

    if (selectedIndex >= 0 && newDataTapped) {
        if (currentSelectedIndex != selectedIndex) {
            onItemSelected(selectedIndex)
        }
    }
    
    if (currentSelectedIndex >= 0) {
        if (!newDataTapped || currentSelectedIndex == selectedIndex) {
            onItemDeselected(currentSelectedIndex)
            onNoItemSelected()
        } else {
            onItemDeselected(currentSelectedIndex)
        }
    }
}
