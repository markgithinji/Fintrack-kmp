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

    // --- Highlights summary ---
    suspend fun getHighlightsSummary(): Result<HighlightsSummary> = try {
        val dto = api.getHighlightsSummary()
        Result.Success(dto.toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    // --- Distribution summary ---
    suspend fun getDistributionSummary(
        weekOrMonthCode: String,       // e.g., "2025-W37" or "2025-09"
        type: String? = null,          // "income" | "expense" | null
        start: String? = null,         // optional "YYYY-MM-DD"
        end: String? = null            // optional "YYYY-MM-DD"
    ): Result<DistributionSummary> = try {
        val dto = api.getDistributionSummary(
            weekOrMonthCode = weekOrMonthCode,
            type = type,
            start = start,
            end = end
        )
        Result.Success(dto.toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getAvailableWeeks(): Result<List<String>> = try {
        val dto = api.getAvailableWeeks()
        Result.Success(dto.weeks)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getAvailableMonths(): Result<AvailableMonths> = try {
        val dto = api.getAvailableMonths()
        Result.Success(dto.toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getAvailableYears(): Result<AvailableYears> = try {
        val dto = api.getAvailableYears()
        Result.Success(dto.toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

}
