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

        // Split into integer and decimal parts
        val parts = originalText.split(".")
        val integerPart = parts[0]
        val decimalPart = if (parts.size > 1) "." + parts[1] else ""

        val formattedInteger = integerPart.reversed()
            .chunked(3)
            .joinToString(",")
            .reversed()
        
        val formatted = formattedInteger + decimalPart

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val safeOffset = offset.coerceAtMost(originalText.length)
                
                // If we are in the decimal part
                if (safeOffset > integerPart.length) {
                    val commasInInteger = if (integerPart.length > 0) (integerPart.length - 1) / 3 else 0
                    return safeOffset + commasInInteger
                }
                
                // We are in the integer part
                val digitsToRight = integerPart.length - safeOffset
                val totalCommas = if (integerPart.length > 0) (integerPart.length - 1) / 3 else 0
                val commasToRight = digitsToRight / 3
                val commasToLeft = totalCommas - commasToRight
                return safeOffset + commasToLeft
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val safeOffset = offset.coerceAtMost(formatted.length)
                
                var commas = 0
                var foundDot = false
                for (i in 0 until safeOffset) {
                    if (formatted[i] == ',') commas++
                    if (formatted[i] == '.') {
                        foundDot = true
                        break
                    }
                }
                
                return if (foundDot && offset > formatted.indexOf('.')) {
                    // We are after the dot
                    val commasInInteger = if (integerPart.length > 0) (integerPart.length - 1) / 3 else 0
                    offset - commasInInteger
                } else {
                    offset - commas
                }
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
