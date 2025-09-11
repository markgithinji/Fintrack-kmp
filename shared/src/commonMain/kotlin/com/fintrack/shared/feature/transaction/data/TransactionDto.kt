package com.fintrack.shared.feature.transaction.data

import com.fintrack.shared.feature.transaction.model.Transaction
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    val id: Int? = null,
    val isIncome: Boolean,
    val amount: Double,
    val category: String,
    val date: String,
    val description: String? = null
)
fun TransactionDto.toDomain(): Transaction =
    Transaction(
        id = id,
        isIncome = isIncome,
        amount = amount,
        category = category,
        date = LocalDate.parse(date),
        description = description
    )

fun Transaction.toDto(): TransactionDto =
    TransactionDto(
        id = id,
        isIncome = isIncome,
        amount = amount,
        category = category,
        date = date.toString(),
        description = description
    )
