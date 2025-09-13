package com.fintrack.shared.feature.transaction.data

import kotlinx.serialization.Serializable

data class AvailableWeeks(
    val weeks: List<String>
)

@Serializable
data class AvailableWeeksDto(
    val weeks: List<String>
)
