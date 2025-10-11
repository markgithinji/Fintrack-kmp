package com.fintrack.shared.feature.account.data.model

import com.fintrack.shared.feature.account.domain.model.Account

fun Account.toDto(): AccountDto = AccountDto(
    id = this.id,
    name = this.name,
    balance = this.balance
)

fun AccountDto.toDomain(): Account = Account(
    id = this.id ?: "",   // Empty string for new inserts
    name = this.name,
    balance = this.balance,
    income = this.income,
    expense = this.expense
)