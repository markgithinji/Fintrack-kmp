package com.fintrack.shared.feature.transaction.data.model

import com.fintrack.shared.feature.transaction.data.TransactionDto
import com.fintrack.shared.feature.transaction.model.Transaction
import kotlinx.datetime.LocalDateTime

fun TransactionDto.toDomain(): Transaction =
    Transaction(
        id = id,
        isIncome = isIncome,
        amount = amount,
        category = category,
        dateTime = LocalDateTime.parse(dateTime),
        description = description
    )

fun Transaction.toDto(): TransactionDto =
    TransactionDto(
        id = id,
        isIncome = isIncome,
        amount = amount,
        category = category,
        dateTime = dateTime.toString(),
        description = description
    )
