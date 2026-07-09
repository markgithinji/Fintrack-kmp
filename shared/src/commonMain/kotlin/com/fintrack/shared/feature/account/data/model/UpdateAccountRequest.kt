package com.fintrack.shared.feature.account.data.model

import com.fintrack.shared.feature.account.domain.model.AccountType
import kotlinx.serialization.Serializable

@Serializable
data class UpdateAccountRequest(
    val name: String,
    val type: AccountType = AccountType.GENERAL,
    val balance: Double? = null
)