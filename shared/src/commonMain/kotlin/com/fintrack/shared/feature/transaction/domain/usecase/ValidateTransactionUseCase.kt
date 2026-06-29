package com.fintrack.shared.feature.transaction.domain.usecase

import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.transaction.domain.model.Category

class ValidateTransactionUseCase {
    operator fun invoke(
        amount: String,
        transactionCost: String = "0",
        category: Category?,
        selectedAccount: Account?
    ): TransactionValidationResult {
        val parsedAmount = amount.toDoubleOrNull()
        val parsedCost = transactionCost.toDoubleOrNull()

        return when {
            amount.isBlank() -> TransactionValidationResult.Invalid("Please enter an amount")
            parsedAmount == null -> TransactionValidationResult.Invalid("Please enter a valid amount")
            parsedAmount <= 0 -> TransactionValidationResult.Invalid("Amount must be greater than zero")
            
            transactionCost.isNotBlank() && parsedCost == null -> 
                TransactionValidationResult.Invalid("Please enter a valid transaction cost")
            parsedCost != null && parsedCost < 0 -> 
                TransactionValidationResult.Invalid("Transaction cost cannot be negative")

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