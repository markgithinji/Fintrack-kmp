package com.fintrack.shared.feature.transaction.data

import com.fintrack.shared.feature.transaction.model.Transaction

class TransactionRepository(
    private val api: TransactionApi
) {
    suspend fun getTransactions(): Result<List<Transaction>> = try {
        Result.Success(api.getTransactions())
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun addTransaction(transaction: Transaction): Result<Transaction> = try {
        Result.Success(api.addTransaction(transaction))
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getSummary(): Result<Map<String, Any>> = try {
        Result.Success(api.getSummary())
    } catch (e: Exception) {
        Result.Error(e)
    }
}
