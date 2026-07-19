package com.fintrack.shared.feature.account.domain.model

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val id: String,
    val name: String,
    @Serializable(with = com.fintrack.shared.feature.core.data.serialization.BigDecimalSerializer::class)
    val balance: BigDecimal? = null,
    @Serializable(with = com.fintrack.shared.feature.core.data.serialization.BigDecimalSerializer::class)
    val income: BigDecimal? = null,
    @Serializable(with = com.fintrack.shared.feature.core.data.serialization.BigDecimalSerializer::class)
    val expense: BigDecimal? = null,
    val isDefault: Boolean = false,
    val type: AccountType = AccountType.GENERAL,
    val linkedSources: List<String> = emptyList(),
    val createdAt: Instant? = null,
    val lastSyncedAt: Instant? = null,
)

@Serializable
enum class AccountType {
    @SerialName("general")
    GENERAL,
    @SerialName("mpesa")
    MPESA,
    @SerialName("equity")
    EQUITY
}
