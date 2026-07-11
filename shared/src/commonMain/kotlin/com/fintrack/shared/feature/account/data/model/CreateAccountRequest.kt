package com.fintrack.shared.feature.account.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateAccountRequest(
    val name: String,
    val type: AccountType = AccountType.GENERAL,
    val balance: Double? = 0.0
)