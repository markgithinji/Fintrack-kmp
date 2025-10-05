package com.fintrack.shared.feature.transaction.domain.repository

import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.transaction.domain.model.Transaction

interface TransactionRepository {
    suspend fun getTransactions(
        limit: Int = 20,
        sortBy: String = "date",
        order: String = "DESC",
        afterDate: String? = null,
        afterId: Int? = null,
        accountId: Int? = null
    ): Result<Pair<List<Transaction>, String?>>

    suspend fun getRecentTransactions(accountId: Int? = null): Result<List<Transaction>>

    suspend fun addTransaction(transaction: Transaction): Result<Transaction>
}