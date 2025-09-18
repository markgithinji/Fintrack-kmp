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

    suspend fun getHighlightsSummary(): Result<HighlightsSummary> = try {
        Result.Success(api.getHighlightsSummary().toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getDistributionSummary(
        weekOrMonthCode: String,
        type: String? = null,
        start: String? = null,
        end: String? = null
    ): Result<DistributionSummary> = try {
        Result.Success(api.getDistributionSummary(weekOrMonthCode, type, start, end).toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getAvailableWeeks(): Result<List<String>> = try {
        Result.Success(api.getAvailableWeeks().weeks)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getAvailableMonths(): Result<AvailableMonths> = try {
        Result.Success(api.getAvailableMonths().toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getAvailableYears(): Result<AvailableYears> = try {
        Result.Success(api.getAvailableYears().toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getOverviewSummary(): Result<OverviewSummary> = try {
        Result.Success(api.getOverviewSummary().toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }
}
