package com.fintrack.shared.feature.core.ui

import androidx.compose.animation.animateContentSize
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
        Text(
            text = value,
            style = style,
            softWrap = false
        )
    }
}
