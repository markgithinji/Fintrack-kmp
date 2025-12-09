package com.fintrack.shared.feature.transaction.domain.repository

import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun getTransactions(
        limit: Int,
        sortBy: String,
        order: String,
        afterDateTime: String? = null,
        afterId: String? = null,
        accountId: String? = null,
        isIncome: Boolean? = null
    ): Result<Pair<List<Transaction>, String?>>

    suspend fun addTransaction(transaction: Transaction): Result<Transaction>

    fun getTransactionsPagingFlow(
        accountId: String?,
        isIncome: Boolean? = null
    ): Flow<PagingData<Transaction>>
}