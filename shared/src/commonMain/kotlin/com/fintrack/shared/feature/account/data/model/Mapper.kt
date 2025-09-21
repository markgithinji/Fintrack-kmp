package com.fintrack.shared.feature.account.data.model

import com.fintrack.shared.feature.account.domain.Account

fun Account.toDto(): AccountDto = AccountDto(
    id = this.id,
    name = this.name,
    balance = this.balance
)

fun AccountDto.toDomain(): Account = Account(
    id = this.id ?: 0,   // 0 for new inserts
    name = this.name,
    balance = this.balance,
    income = this.income,
    expense = this.expense
)