package com.fintrack.shared.feature.budget.domain.usecase

import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.core.domain.ValidationResult
import com.fintrack.shared.feature.category.domain.model.Category
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

        return when {
            name.isBlank() -> ValidationResult.Error("Budget name is required")
            limit <= 0 -> ValidationResult.Error("Valid amount is required")
            selectedAccount == null -> ValidationResult.Error("Please select an account for this budget")
            categories.isEmpty() -> ValidationResult.Error("At least one category is required")
            startDate == null -> ValidationResult.Error("Start date is required")
            endDate == null -> ValidationResult.Error("End date is required")
            else -> ValidationResult.Success
        }
    }
}