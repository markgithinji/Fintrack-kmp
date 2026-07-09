package com.fintrack.shared.feature.account.data.model

import com.fintrack.shared.feature.account.domain.model.AccountType
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class AccountDto @OptIn(ExperimentalTime::class) constructor(
    val id: String? = null,
    val name: String,
    val balance: Double? = null,
    val income: Double? = null,
    val expense: Double? = null,
    val isDefault: Boolean? = false,
    val type: AccountType? = AccountType.GENERAL,
    val createdAt: Instant? = null
)
