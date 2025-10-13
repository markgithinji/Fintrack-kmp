package com.fintrack.shared.feature.transaction.data

import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.util.safeApiCall
import com.fintrack.shared.feature.transaction.data.model.toCreateRequest
import com.fintrack.shared.feature.transaction.data.model.toDomain
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository

class TransactionRepositoryImpl(
    private val api: TransactionApi
) : TransactionRepository {

    override suspend fun getTransactions(
        limit: Int,
        sortBy: String,
        order: String,
        afterDate: String?,
        afterId: String?,
        accountId: String?
    ): Result<Pair<List<Transaction>, String?>> =
        safeApiCall {
            val paginated = api.getTransactions(
                limit = limit,
                sortBy = sortBy,
                order = order,
                afterDate = afterDate,
                afterId = afterId,
                accountId = accountId
            )
            val transactions = paginated.data.map { it.toDomain() }
            transactions to paginated.nextCursor
        }

    override suspend fun getRecentTransactions(accountId: String?): Result<List<Transaction>> =
        safeApiCall {
            val paginated = api.getTransactions(
                limit = 6,
                sortBy = "date",
                order = "DESC",
                accountId = accountId
            )
            paginated.data.map { it.toDomain() }
        }

    override suspend fun addTransaction(transaction: Transaction): Result<Transaction> =
        safeApiCall {
            val createRequest = transaction.toCreateRequest()
            val dto = api.addTransaction(createRequest)
            dto.toDomain()
        }
}