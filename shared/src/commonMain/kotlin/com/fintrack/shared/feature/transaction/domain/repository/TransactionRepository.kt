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

    suspend fun addTransactions(transactions: List<Transaction>): Result<Unit>

    suspend fun getTransaction(id: String): Result<Transaction>

    suspend fun getAllTransactions(): Result<List<Transaction>>

    suspend fun updateTransaction(id: String, transaction: Transaction): Result<Transaction>

    suspend fun deleteTransaction(id: String): Result<Unit>

    suspend fun deleteAllTransactions(): Result<Unit>

    fun getTransactionsPagingFlow(
        accountId: String?,
        isIncome: Boolean? = null
    ): Flow<PagingData<Transaction>>

    suspend fun triggerRefresh()
    val refreshSignal: Flow<Unit>
}