package com.fintrack.shared.feature.core.util

fun Double.formatToSinglePrecision(): String {
    val multiplied = (this * 10).toInt()
    val result = multiplied.toDouble() / 10
    return if (result % 1.0 == 0.0) {
        result.toInt().toString()
    } else {
        result.toString()
    }
}

fun Double.formatToCurrency(): String {
    return "KSh ${this.formatToAmount()}"
}

fun Double.formatToAmount(showDecimals: Boolean = true): String {
    val isNegative = this < 0
    val absValue = kotlin.math.abs(this)
    val totalCents = (absValue * 100 + 0.5).toLong()
    val integerPart = totalCents / 100
    val decimalPart = totalCents % 100
    
    val integerString = integerPart.toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
    
    val result = if (showDecimals) {
        val decimalString = decimalPart.toString().padStart(2, '0')
        "$integerString.$decimalString"
    } else {
        integerString
    }
    
    return if (isNegative) "-$result" else result
}
