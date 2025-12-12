package com.fintrack.shared.feature.budget.domain.usecase

import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.budget.domain.model.ValidationResult
import com.fintrack.shared.feature.transaction.domain.model.Category
import kotlinx.datetime.LocalDate

class BudgetValidationUseCase {
    operator fun invoke(
        name: String,
        amount: String,
        categories: Set<Category>,
        startDate: LocalDate?,
        endDate: LocalDate?,
        selectedAccount: Account?
    ): ValidationResult {
        val limit = amount.toDoubleOrNull() ?: 0.0
        val errors = mutableListOf<String>()

        if (name.isBlank()) {
            errors.add("Budget name is required")
        }
        if (categories.isEmpty()) {
            errors.add("At least one category is required")
        }
        if (limit <= 0) {
            errors.add("Valid amount is required")
        }
        if (startDate == null) {
            errors.add("Start date is required")
        }
        if (endDate == null) {
            errors.add("End date is required")
        }
        if (selectedAccount == null) {
            errors.add("Please select an account for this budget")
        }

        return if (errors.isEmpty()) {
            ValidationResult(isValid = true)
        } else {
            ValidationResult(
                isValid = false,
                errorMessage = errors.joinToString("\n")
            )
        }
    }
}