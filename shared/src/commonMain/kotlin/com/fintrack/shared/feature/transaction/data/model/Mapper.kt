package com.fintrack.shared.feature.transaction.data.model

import com.fintrack.shared.feature.transaction.domain.model.Transaction

fun TransactionDto.toDomain(): Transaction =
    Transaction(
        id = id,
        accountId = accountId,
        isIncome = isIncome,
        amount = amount,
        transactionCost = transactionCost,
        category = category,
        dateTime = dateTime,
        description = description
    )

fun Transaction.toCreateRequest(): CreateTransactionRequest {
    return CreateTransactionRequest(
        accountId = this.accountId,
        isIncome = this.isIncome,
        amount = this.amount,
        transactionCost = this.transactionCost,
        category = this.category,
        dateTime = this.dateTime,
        description = this.description
    )
}
