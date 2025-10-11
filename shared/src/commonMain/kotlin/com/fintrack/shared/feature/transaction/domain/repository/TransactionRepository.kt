package com.fintrack.shared.feature.transaction.domain.repository

import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.transaction.domain.model.Transaction

interface TransactionRepository {
    suspend fun getTransactions(
        limit: Int,
        sortBy: String,
        order: String,
        afterDate: String?= null,
        afterId: String?= null,
        accountId: String?= null
    ): Result<Pair<List<Transaction>, String?>>

    suspend fun getRecentTransactions(accountId: String?): Result<List<Transaction>>
    suspend fun addTransaction(transaction: Transaction): Result<Transaction>
}