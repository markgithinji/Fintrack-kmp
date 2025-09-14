package com.fintrack.shared.feature.transaction.data

import kotlinx.serialization.Serializable

@Serializable
data class AvailableYearsDto(
    val years: List<String>
)

data class AvailableYears(val years: List<String>)

fun AvailableYearsDto.toDomain() = AvailableYears(years)