package com.fintrack.shared.feature.summary.domain.model

data class TransactionCountSummary(
    val totalIncomeTransactions: Int,
    val totalExpenseTransactions: Int,
    val totalTransactions: Int
)