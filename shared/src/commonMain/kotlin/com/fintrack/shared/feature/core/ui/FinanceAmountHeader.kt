package com.fintrack.shared.feature.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.shared.feature.navigation.ui.LocalCurrency

@Composable
fun FinanceAmountHeader(
    amount: String,
    label: String,
    isIncome: Boolean,
    themeColor: Color,
    onToggleNumpad: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    val amountFontSize by animateDpAsState(
        targetValue = when {
            amount.length >= 10 -> 32.dp
            amount.length >= 8 -> 38.dp
            amount.length >= 6 -> 44.dp
            else -> 48.dp
        }.value.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    Surface(
        color = themeColor,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp + paddingValues.calculateTopPadding())
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onToggleNumpad(true) }
            ),
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = label,
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn()).togetherWith(
                        slideOutVertically { height -> -height } + fadeOut())
                }
            ) { targetLabel ->
                Text(
                    text = targetLabel,
                    color = (if (isIncome) MaterialTheme.colorScheme.onTertiary else Color.White).copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.animateContentSize()
                ) {
                    Text(
                        text = LocalCurrency.current.symbol,
                        color = (if (isIncome) MaterialTheme.colorScheme.onTertiary else Color.White).copy(alpha = 0.7f),
                        fontSize = (amountFontSize.value * 0.5f).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = (amountFontSize.value * 0.2f).dp, end = 8.dp)
                    )

                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier.widthIn(min = 16.dp)
                    ) {
                        if (amount.isEmpty() || amount == "0" || amount == "0.00") {
                            Text(
                                "0.00",
                                color = (if (isIncome) MaterialTheme.colorScheme.onTertiary else Color.White).copy(alpha = 0.4f),
                                fontSize = amountFontSize.value.sp,
                                fontWeight = FontWeight.Black
                            )
                        } else {
                            // Split into integer and decimal parts for formatting
                            val parts = amount.split(".")
                            val integerPart = parts[0].reversed().chunked(3).joinToString(",").reversed()
                            
                            // Ensure at least 2 decimal places are shown if a decimal point exists or was loaded
                            val decimalPart = if (parts.size > 1) {
                                if (parts[1].length < 2) parts[1].padEnd(2, '0') else parts[1]
                            } else {
                                "00"
                            }
                            
                            val formattedAmount = "$integerPart.$decimalPart"

                            AnimatedNumber(
                                value = formattedAmount,
                                style = TextStyle(
                                    color = if (isIncome) MaterialTheme.colorScheme.onTertiary else Color.White,
                                    fontSize = amountFontSize.value.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Start,
                                    letterSpacing = 0.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
