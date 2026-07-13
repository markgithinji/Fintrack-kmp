package com.fintrack.shared.feature.transaction.domain.usecase

import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.category.domain.model.Category
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal

class ValidateTransactionUseCase {
    operator fun invoke(
        amount: String,
        transactionCost: String = "0",
        description: String,
        category: Category?,
        selectedAccount: Account?
    ): TransactionValidationResult {
        val parsedAmount = try { amount.toBigDecimal() } catch (_: Exception) { null }
        val parsedCost = try { transactionCost.toBigDecimal() } catch (_: Exception) { null }

        return when {
            amount.isBlank() -> TransactionValidationResult.Invalid("Please enter an amount")
            parsedAmount == null -> TransactionValidationResult.Invalid("Please enter a valid amount")
            parsedAmount <= BigDecimal.ZERO -> TransactionValidationResult.Invalid("Amount must be greater than zero")
            
            transactionCost.isNotBlank() && parsedCost == null -> 
                TransactionValidationResult.Invalid("Please enter a valid transaction cost")
            parsedCost != null && parsedCost < BigDecimal.ZERO ->
                TransactionValidationResult.Invalid("Transaction cost cannot be negative")

            category == null -> TransactionValidationResult.Invalid("Please select a category")
            selectedAccount == null -> TransactionValidationResult.Invalid("Please select an account")
            description.isBlank() -> TransactionValidationResult.Invalid("Please enter a description")
            else -> TransactionValidationResult.Valid
        }
    }

    sealed class TransactionValidationResult {
        object Valid : TransactionValidationResult()
        data class Invalid(val errorMessage: String) : TransactionValidationResult()
    }
}