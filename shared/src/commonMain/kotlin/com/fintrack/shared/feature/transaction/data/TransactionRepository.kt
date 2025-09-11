package com.fintrack.shared.feature.transaction.data

import com.fintrack.shared.feature.transaction.model.Transaction

class TransactionRepository(
    private val api: TransactionApi
) {
    suspend fun getTransactions(): Result<List<Transaction>> = try {
        val dtos = api.getTransactions()
        Result.Success(dtos.map { it.toDomain() })
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun addTransaction(transaction: Transaction): Result<Transaction> = try {
        val dto = api.addTransaction(transaction.toDto())
        Result.Success(dto.toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getSummary(): Result<Map<String, Any>> = try {
        Result.Success(api.getSummary())
    } catch (e: Exception) {
        Result.Error(e)
    }
}

