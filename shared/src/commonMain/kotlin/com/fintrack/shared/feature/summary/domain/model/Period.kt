package com.fintrack.shared.feature.summary.domain.model

sealed class Period {
    data class Week(val code: String) : Period()
    data class Month(val code: String) : Period()
    data class Year(val code: String) : Period()
}