package com.fintrack.shared.feature.account.data.model

import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.account.domain.model.AccountType

fun Account.toCreateRequest(): CreateAccountRequest = CreateAccountRequest(
    name = this.name,
    type = this.type,
    balance = this.balance
)

fun Account.toUpdateRequest(): UpdateAccountRequest = UpdateAccountRequest(
    name = this.name,
    type = this.type,
    balance = this.balance
)

fun AccountDto.toDomain(): Account = Account(
    id = this.id ?: "",   // Empty string for new inserts
    name = this.name,
    balance = this.balance,
    income = this.income,
    expense = this.expense,
    isDefault = this.isDefault ?: false,
    type = this.type ?: AccountType.GENERAL,
    createdAt = this.createdAt
)