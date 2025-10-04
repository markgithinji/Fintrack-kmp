package com.fintrack.shared.feature.transaction.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill


@Composable
fun LowerRightWavesBackground(modifier: Modifier = Modifier) {
    val baseColor = Color(0xFF2B2B2B)
    val wave1 = Color(0xFF3C3C3C)
    val wave2 = Color(0xFF4D4D4D)
    val wave3 = Color(0xFF5E5E5E)

    Box(
        modifier = modifier
            .background(baseColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Wave 1
            val path1 = Path().apply {
                moveTo(w * 0.5f, h)
                cubicTo(
                    w * 0.6f, h * 0.85f,
                    w * 0.8f, h * 0.95f,
                    w, h * 0.7f
                )
                lineTo(w, h)
                lineTo(w * 0.5f, h)
                close()
            }
            drawPath(path1, color = wave1, style = Fill)

            // Wave 2
            val path2 = Path().apply {
                moveTo(w * 0.6f, h)
                cubicTo(
                    w * 0.65f, h * 0.9f,
                    w * 0.85f, h * 0.8f,
                    w, h * 0.5f
                )
                lineTo(w, h)
                lineTo(w * 0.6f, h)
                close()
            }
            drawPath(path2, color = wave2, style = Fill)

            // Wave 3
            val path3 = Path().apply {
                moveTo(w * 0.7f, h)
                cubicTo(
                    w * 0.75f, h * 0.95f,
                    w * 0.9f, h * 0.6f,
                    w, h * 0.3f
                )
                lineTo(w, h)
                lineTo(w * 0.7f, h)
                close()
            }
            drawPath(path3, color = wave3, style = Fill)
        }
    }
}