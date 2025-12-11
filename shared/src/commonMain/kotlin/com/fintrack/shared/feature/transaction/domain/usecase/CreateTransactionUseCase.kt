package com.fintrack.shared.feature.transaction.domain.usecase

import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.transaction.domain.model.Category
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import kotlinx.datetime.LocalDateTime

class CreateTransactionUseCase {
    operator fun invoke(
        amount: String,
        isIncome: Boolean,
        category: Category?,
        description: String,
        selectedAccount: Account?,
        dateTime: LocalDateTime
    ): Transaction? {
        val parsedAmount = amount.toDoubleOrNull() ?: return null

        return Transaction(
            id = null,
            accountId = selectedAccount?.id ?: return null,
            amount = parsedAmount,
            isIncome = isIncome,
            category = category?.name ?: return null,
            description = description.takeIf { it.isNotBlank() },
            dateTime = dateTime
        )
    }
}