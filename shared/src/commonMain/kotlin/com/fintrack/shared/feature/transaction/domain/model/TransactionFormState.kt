package com.fintrack.shared.feature.transaction.domain.model

import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.category.domain.model.Category
import kotlin.time.Clock
import kotlin.time.Instant

data class TransactionFormState(
    val amount: String = "",
    val transactionCost: String = "",
    val isIncome: Boolean = false,
    val selectedCategory: Category? = null,
    val selectedAccount: Account? = null,
    val description: String = "",
    val dateTime: Instant = Clock.System.now()
)
