package com.fintrack.shared.feature.transaction.domain.usecase

import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.transaction.domain.model.Category

class ValidateTransactionUseCase {
    operator fun invoke(
        amount: String,
        category: Category?,
        selectedAccount: Account?
    ): TransactionValidationResult {
        return when {
            amount.isBlank() -> TransactionValidationResult.Invalid("Please enter an amount")
            amount.toDoubleOrNull() == null -> TransactionValidationResult.Invalid("Please enter a valid amount")
            amount.toDoubleOrNull()?.let { it <= 0 } == true ->
                TransactionValidationResult.Invalid("Amount must be greater than zero")

            category == null -> TransactionValidationResult.Invalid("Please select a category")
            selectedAccount == null -> TransactionValidationResult.Invalid("Please select an account")
            else -> TransactionValidationResult.Valid
        }
    }

    sealed class TransactionValidationResult {
        object Valid : TransactionValidationResult()
        data class Invalid(val errorMessage: String) : TransactionValidationResult()
    }
}