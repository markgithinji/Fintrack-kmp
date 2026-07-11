package com.fintrack.shared.feature.account.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateAccountRequest(
    val name: String,
    val type: AccountType = AccountType.GENERAL,
    val balance: Double? = null
)