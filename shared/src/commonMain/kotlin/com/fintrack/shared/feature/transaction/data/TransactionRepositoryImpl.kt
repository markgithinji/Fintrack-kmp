package com.fintrack.shared.feature.transaction.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.util.safeApiCall
import com.fintrack.shared.feature.transaction.data.model.toCreateRequest
import com.fintrack.shared.feature.transaction.data.model.toDomain
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class TransactionRepositoryImpl(
    private val api: TransactionApi
) : TransactionRepository {

    companion object {
        private const val PAGE_SIZE = 20
        private const val INITIAL_LOAD_SIZE = 20
        private const val PREFETCH_DISTANCE = 10
    }

    override suspend fun getTransactions(
        limit: Int,
        sortBy: String,
        order: String,
        afterDateTime: String?,
        afterId: String?,
        accountId: String?,
        isIncome: Boolean?
    ): Result<Pair<List<Transaction>, String?>> =
        safeApiCall {
            val paginated = api.getTransactions(
                limit = limit,
                sortBy = sortBy,
                order = order,
                afterDateTime = afterDateTime,
                afterId = afterId,
                accountId = accountId,
                isIncome = isIncome
            )
            val transactions = paginated.data.map { it.toDomain() }
            transactions to paginated.nextCursor
        }

    override suspend fun addTransaction(transaction: Transaction): Result<Transaction> =
        safeApiCall {
            val createRequest = transaction.toCreateRequest()
            val dto = api.addTransaction(createRequest)
            dto.toDomain()
        }

    override suspend fun getTransaction(id: String): Result<Transaction> =
        safeApiCall {
            api.getTransaction(id).toDomain()
        }

    override suspend fun updateTransaction(id: String, transaction: Transaction): Result<Transaction> =
        safeApiCall {
            api.updateTransaction(id, transaction.toCreateRequest()).toDomain()
        }

    override fun getTransactionsPagingFlow(
        accountId: String?,
        isIncome: Boolean?
    ): Flow<PagingData<Transaction>> {
        return createPager {
            TransactionPagingSource(
                repo = this,
                accountId = accountId,
                isIncome = isIncome
            )
        }
    }

    private fun createPager(
        pagingSourceFactory: () -> PagingSource<String, Transaction>
    ): Flow<PagingData<Transaction>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = INITIAL_LOAD_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow
            .distinctUntilChanged()
    }
}