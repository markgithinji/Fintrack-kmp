package com.fintrack.shared.feature.account.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AccountDto(
    val id: String? = null,
    val name: String,
    val balance: Double? = null,
    val income: Double? = null,
    val expense: Double? = null
)