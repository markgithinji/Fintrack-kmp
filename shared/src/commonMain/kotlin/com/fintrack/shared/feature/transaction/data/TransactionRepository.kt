package com.fintrack.shared.feature.transaction.data

import com.fintrack.shared.feature.transaction.model.Transaction

class TransactionRepository(
    private val api: TransactionApi
) {
    suspend fun getTransactions(
        limit: Int = 20,
        sortBy: String = "date",
        order: String = "DESC",
        afterDate: String? = null,
        afterId: Int? = null
    ): Result<Pair<List<Transaction>, String?>> = try {
        val paginated = api.getTransactions(
            limit = limit,
            sortBy = sortBy,
            order = order,
            afterDate = afterDate,
            afterId = afterId
        )
        val transactions = paginated.data.map { it.toDomain() }
        Result.Success(transactions to paginated.nextCursor)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getRecentTransactions(): Result<List<Transaction>> = try {
        val paginated = api.getTransactions(
            limit = 6,
            sortBy = "date",
            order = "DESC"
        )
        Result.Success(paginated.data.map { it.toDomain() })
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun addTransaction(transaction: Transaction): Result<Transaction> = try {
        val dto = api.addTransaction(transaction.toDto())
        Result.Success(dto.toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getSummary(): Result<Summary> = try {
        val dto = api.getSummary()
        Result.Success(dto.toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }
}

