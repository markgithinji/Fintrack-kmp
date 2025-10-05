package com.fintrack.shared.feature.transaction.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke


@Composable
fun LowerRightWavesBackground(modifier: Modifier = Modifier) {
    val baseColor = Color(0xFF2B2B2B)
    val waveColors = listOf(
        Color(0xFF3C3C3C),
        Color(0xFF4D4D4D),
        Color(0xFF5E5E5E)
    )

    Box(
        modifier = modifier
            .background(baseColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawLayeredWaves(w, h, waveColors)
        }
    }
}

private fun DrawScope.drawLayeredWaves(w: Float, h: Float, colors: List<Color>) {
    val path1 = Path().apply {
        moveTo(w * 0.7f, h)
        cubicTo(
            w * 0.75f, h * 0.85f,
            w * 0.85f, h * 0.9f,
            w, h * 0.8f
        )
        lineTo(w, h)
        close()
    }
    drawPath(path1, color = colors[0], style = Fill)

    val path2 = Path().apply {
        moveTo(w * 0.75f, h)
        cubicTo(
            w * 0.78f, h * 0.8f,
            w * 0.88f, h * 0.85f,
            w, h * 0.7f
        )
        lineTo(w, h)
        close()
    }
    drawPath(path2, color = colors[1], style = Fill)

    val path3 = Path().apply {
        moveTo(w * 0.8f, h)
        cubicTo(
            w * 0.82f, h * 0.75f,
            w * 0.9f, h * 0.8f,
            w, h * 0.6f
        )
        lineTo(w, h)
        close()
    }
    drawPath(path3, color = colors[2], style = Fill)
}