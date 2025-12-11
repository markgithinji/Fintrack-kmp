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

private fun Double.formatToAmount(): String {
    return if (this % 1.0 == 0.0) {
        this.toInt().toString()
    } else {
        val multiplied = (this * 100).toInt()
        val result = multiplied.toDouble() / 100
        result.toString()
    }
}