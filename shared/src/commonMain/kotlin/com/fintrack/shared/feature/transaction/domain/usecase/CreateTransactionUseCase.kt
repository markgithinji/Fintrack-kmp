package com.fintrack.shared.feature.transaction.domain.usecase

import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.transaction.domain.model.Category
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import kotlin.time.Instant

class CreateTransactionUseCase {
    operator fun invoke(
        amount: String,
        transactionCost: String = "0",
        isIncome: Boolean,
        category: Category?,
        description: String,
        selectedAccount: Account?,
        dateTime: Instant
    ): Transaction? {
        val parsedAmount = amount.toDoubleOrNull() ?: return null
        val parsedCost = transactionCost.toDoubleOrNull() ?: 0.0

        return Transaction(
            id = null,
            accountId = selectedAccount?.id ?: return null,
            amount = parsedAmount,
            transactionCost = parsedCost,
            isIncome = isIncome,
            category = category?.name ?: return null,
            description = description.takeIf { it.isNotBlank() },
            dateTime = dateTime
        )
    }
}