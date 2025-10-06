package com.fintrack.shared.feature.transaction.ui.addtransaction

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LoadingBarChart() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
    ) {
        // Y-axis placeholder
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Bottom
        ) {
            // Y-axis labels
            Column(
                modifier = Modifier
                    .height(180.dp)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(5) {
                    ShimmerEffect(
                        modifier = Modifier
                            .width(30.dp)
                            .height(12.dp)
                    )
                }
            }

            // Bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                repeat(7) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ShimmerEffect(
                            modifier = Modifier
                                .width(24.dp)
                                .height(180.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ShimmerEffect(
                            modifier = Modifier
                                .width(20.dp)
                                .height(10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition()
    val alphaAnim by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier.background(Color.LightGray.copy(alpha = alphaAnim))
    )
}

@Composable
fun LoadingLineChart() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        ShimmerEffect(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        )
    }
}

@Composable
fun LoadingCategoryItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon placeholder
        ShimmerEffect(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Text placeholder
        Column {
            ShimmerEffect(
                modifier = Modifier
                    .width(80.dp)
                    .height(16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            ShimmerEffect(
                modifier = Modifier
                    .width(60.dp)
                    .height(12.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Change text placeholder
        ShimmerEffect(
            modifier = Modifier
                .width(120.dp)
                .height(14.dp)
        )
    }
}

@Composable
fun LoadingInfoCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(70.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon placeholder with shimmer
            AnimatedShimmerBox(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                AnimatedShimmerBox(
                    modifier = Modifier
                        .width(60.dp)
                        .height(12.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                AnimatedShimmerBox(
                    modifier = Modifier
                        .width(80.dp)
                        .height(15.dp)
                )
            }
        }
    }
}

@Composable
fun LoadingTransactionRow() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Loading icon placeholder
                AnimatedShimmerBox(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    // Loading category name
                    AnimatedShimmerBox(
                        modifier = Modifier
                            .width(80.dp)
                            .height(16.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Loading description
                    AnimatedShimmerBox(
                        modifier = Modifier
                            .width(120.dp)
                            .height(12.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                // Loading amount
                AnimatedShimmerBox(
                    modifier = Modifier
                        .width(60.dp)
                        .height(16.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Loading date
                AnimatedShimmerBox(
                    modifier = Modifier
                        .width(40.dp)
                        .height(12.dp)
                )
            }
        }
    }
}

@Composable
fun AnimatedShimmerBox(
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 1000, translateAnim - 1000),
        end = Offset(translateAnim, translateAnim)
    )

    Box(
        modifier = modifier.background(brush = brush)
    )
}