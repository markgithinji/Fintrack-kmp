package com.fintrack.shared.feature.account.data

import com.fintrack.shared.feature.account.data.model.AccountDto
import com.fintrack.shared.feature.account.domain.Account
import com.fintrack.shared.feature.transaction.data.AccountDto

fun Account.toDto(): AccountDto = AccountDto(
    id = this.id,
    name = this.name,
    balance = this.balance
)

fun AccountDto.toDomain(): Account = Account(
    id = this.id ?: 0,   // 0 for new inserts
    name = this.name,
    balance = this.balance
)