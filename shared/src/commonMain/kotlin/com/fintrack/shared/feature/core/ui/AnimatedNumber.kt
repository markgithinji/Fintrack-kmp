package com.fintrack.shared.feature.core.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

@Composable
fun AnimatedNumber(
    value: String,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        value.forEachIndexed { index, char ->
            if (char == ',') {
                Text(
                    text = ",",
                    style = style,
                    softWrap = false
                )
            } else {
                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                        } else {
                            (slideInVertically { -it } + fadeIn()).togetherWith(slideOutVertically { it } + fadeOut())
                        }.using(SizeTransform(clip = false))
                    },
                    label = "Digit-$index"
                ) { digit ->
                    Text(
                        text = digit.toString(),
                        style = style,
                        softWrap = false
                    )
                }
            }
        }
    }
}
