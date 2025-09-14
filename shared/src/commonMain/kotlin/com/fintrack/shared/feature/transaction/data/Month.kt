package com.fintrack.shared.feature.transaction.data

import kotlinx.serialization.Serializable

@Serializable
data class AvailableMonthsDto(
    val months: List<String> // e.g., ["2025-07", "2025-08", "2025-09"]
)

data class AvailableMonths(
    val months: List<String>
)

fun AvailableMonthsDto.toDomain(): AvailableMonths {
    return AvailableMonths(months = this.months)
}
