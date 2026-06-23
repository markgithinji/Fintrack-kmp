package com.fintrack.shared.feature.core.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class ThousandsSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val formatted = originalText.reversed()
            .chunked(3)
            .joinToString(",")
            .reversed()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val safeOffset = offset.coerceAtMost(originalText.length)
                val digitsToRight = originalText.length - safeOffset
                val totalCommas = (originalText.length - 1) / 3
                val commasToRight = digitsToRight / 3
                val commasToLeft = totalCommas - commasToRight
                return safeOffset + commasToLeft
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val safeOffset = offset.coerceAtMost(formatted.length)
                var commas = 0
                for (i in 0 until safeOffset) {
                    if (formatted[i] == ',') commas++
                }
                return safeOffset - commas
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
