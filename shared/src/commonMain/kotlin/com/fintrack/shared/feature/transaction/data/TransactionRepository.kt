package com.fintrack.shared.feature.transaction.data

import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.summary.data.model.toDomain
import com.fintrack.shared.feature.summary.domain.AvailableMonths
import com.fintrack.shared.feature.summary.domain.AvailableWeeks
import com.fintrack.shared.feature.summary.domain.AvailableYears
import com.fintrack.shared.feature.summary.domain.CategoryComparison
import com.fintrack.shared.feature.summary.domain.DistributionSummary
import com.fintrack.shared.feature.summary.domain.StatisticsSummary
import com.fintrack.shared.feature.summary.domain.OverviewSummary
import com.fintrack.shared.feature.transaction.data.model.toDomain
import com.fintrack.shared.feature.transaction.data.model.toDto
import com.fintrack.shared.feature.transaction.model.Transaction

class TransactionRepository(
    private val api: TransactionApi
) {
    // --- Paginated / all transactions ---
    suspend fun getTransactions(
        limit: Int = 20,
        sortBy: String = "date",
        order: String = "DESC",
        afterDate: String? = null,
        afterId: Int? = null,
        accountId: Int? = null
    ): Result<Pair<List<Transaction>, String?>> = try {
        val paginated = api.getTransactions(
            limit = limit,
            sortBy = sortBy,
            order = order,
            afterDate = afterDate,
            afterId = afterId,
            accountId = accountId
        )
        val transactions = paginated.data.map { it.toDomain() }
        Result.Success(transactions to paginated.nextCursor)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // --- Recent transactions ---
    suspend fun getRecentTransactions(accountId: Int? = null): Result<List<Transaction>> = try {
        val paginated = api.getTransactions(
            limit = 6,
            sortBy = "date",
            order = "DESC",
            accountId = accountId
        )
        Result.Success(paginated.data.map { it.toDomain() })
    } catch (e: Exception) {
        Result.Error(e)
    }

    // --- Add new transaction ---
    suspend fun addTransaction(transaction: Transaction): Result<Transaction> = try {
        val dto = api.addTransaction(transaction.toDto())
        Result.Success(dto.toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }
}

