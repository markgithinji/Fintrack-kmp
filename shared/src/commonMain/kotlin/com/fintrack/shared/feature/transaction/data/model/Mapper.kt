package com.fintrack.shared.feature.transaction.data.model

import com.fintrack.shared.feature.transaction.domain.model.Transaction

fun TransactionDto.toDomain(): Transaction =
    Transaction(
        id = id,
        isIncome = isIncome,
        amount = amount,
        category = category,
        dateTime = dateTime,
        description = description
    )

fun Transaction.toCreateRequest(): CreateTransactionRequest {
    return CreateTransactionRequest(
        accountId = this.id.toString(),
        isIncome = this.isIncome,
        amount = this.amount,
        category = this.category,
        dateTime = this.dateTime,
        description = this.description
    )
}
