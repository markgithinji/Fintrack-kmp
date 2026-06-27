package com.fintrack.shared.feature.transaction.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val isExpense: Boolean,
    val iconName: String? = null
)
