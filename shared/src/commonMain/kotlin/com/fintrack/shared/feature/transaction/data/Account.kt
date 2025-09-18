package com.fintrack.shared.feature.transaction.data

import kotlinx.serialization.Serializable

data class Account(
    val id: Int,
    val name: String,
    val balance: Double? = null,
)

@Serializable
data class AccountDto(
    val id: Int? = null,
    val name: String,
    val balance: Double? = null,
)

fun Account.toDto(): AccountDto = AccountDto(
    id = this.id,
    name = this.name,
    balance = this.balance
)

// DTO -> Domain
fun AccountDto.toDomain(): Account = Account(
    id = this.id ?: 0,   // 0 for new inserts
    name = this.name,
    balance = this.balance
)