package com.fintrack.shared.feature.account.domain.model

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

data class Account(
    val id: String,
    val name: String,
    val balance: BigDecimal? = null,
    val income: BigDecimal? = null,
    val expense: BigDecimal? = null,
    val isDefault: Boolean = false,
    val type: AccountType = AccountType.GENERAL,
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
