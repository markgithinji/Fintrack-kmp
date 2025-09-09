package com.fintrack.shared.feature.transaction.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: Int? = null,
    val type: String,
    val amount: Double,
    val category: String,
    @Contextual val date: LocalDate
)
