package com.fintrack.shared.feature.budget.domain.model

import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.transaction.domain.model.Category
import kotlinx.datetime.LocalDate

data class BudgetFormState(
    val name: String = "",
    val amount: String = "",
    val selectedCategories: Set<Category> = emptySet(),
    val isExpense: Boolean = true,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val selectedAccount: Account? = null
)