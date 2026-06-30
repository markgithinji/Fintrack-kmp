package com.fintrack.shared.feature.account.data.model

import com.fintrack.shared.feature.account.domain.model.Account

fun Account.toCreateRequest(): CreateAccountRequest = CreateAccountRequest(
    name = this.name,
    isMpesa = this.isMpesa
)

fun Account.toUpdateRequest(): UpdateAccountRequest = UpdateAccountRequest(
    name = this.name,
    isMpesa = this.isMpesa
)

fun AccountDto.toDomain(): Account = Account(
    id = this.id ?: "",   // Empty string for new inserts
    name = this.name,
    balance = this.balance,
    income = this.income,
    expense = this.expense,
    isDefault = this.isDefault ?: false,
    isMpesa = this.isMpesa ?: false
)