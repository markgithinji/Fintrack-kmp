package com.fintrack.shared.feature.transaction.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.util.safeApiCall
import com.fintrack.shared.feature.transaction.data.model.toCreateRequest
import com.fintrack.shared.feature.transaction.data.model.toDomain
import com.fintrack.shared.feature.transaction.domain.model.RecurringBill
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class TransactionRepositoryImpl(
    private val api: TransactionApi
) : TransactionRepository {

    companion object {
        private const val PAGE_SIZE = 20
        private const val INITIAL_LOAD_SIZE = 20
        private const val PREFETCH_DISTANCE = 10
    }

    private val _refreshSignal = MutableSharedFlow<Unit>(replay = 0)
    override val refreshSignal: Flow<Unit> = _refreshSignal.asSharedFlow()

    override suspend fun triggerRefresh() {
        _refreshSignal.emit(Unit)
    }

    override suspend fun getTransactions(
        limit: Int,
        sortBy: String,
        order: String,
        afterDateTime: String?,
        afterId: String?,
        accountId: String?,
        isIncome: Boolean?,
        category: String?,
        startDate: String?,
        endDate: String?,
        hasTransactionCost: Boolean?
    ): Result<Pair<List<Transaction>, String?>> =
        safeApiCall {
            val paginated = api.getTransactions(
                limit = limit,
                sortBy = sortBy,
                order = order,
                afterDateTime = afterDateTime,
                afterId = afterId,
                accountId = accountId,
                isIncome = isIncome,
                category = category,
                startDate = startDate,
                endDate = endDate,
                hasTransactionCost = hasTransactionCost
            )
            val transactions = paginated.data.map { it.toDomain() }
            transactions to paginated.nextCursor
        }

    override suspend fun addTransaction(transaction: Transaction): Result<Transaction> {
        val result = safeApiCall {
            val createRequest = transaction.toCreateRequest()
            val dto = api.addTransaction(createRequest)
            dto.toDomain()
        }
        if (result is Result.Success) {
            triggerRefresh()
        }
        return result
    }

    override suspend fun addTransactions(transactions: List<Transaction>): Result<Unit> {
        val result = safeApiCall {
            val requests = transactions.map { it.toCreateRequest() }
            api.addTransactions(requests)
        }
        if (result is Result.Success) {
            triggerRefresh()
        }
        return result
    }

    override suspend fun importMpesaTransactions(transactions: List<Transaction>): Result<Unit> {
        val result = safeApiCall {
            val requests = transactions.map { it.toCreateRequest() }
            api.importMpesaTransactions(requests)
        }
        if (result is Result.Success) {
            triggerRefresh()
        }
        return result
    }

    override suspend fun getTransaction(id: String): Result<Transaction> =
        safeApiCall {
            api.getTransaction(id).toDomain()
        }

    override suspend fun getAllTransactions(): Result<List<Transaction>> =
        safeApiCall {
            // Fetching with a large limit for export. 
            // In a real app, this might need to handle pagination to get *everything*.
            val paginated = api.getTransactions(limit = 1000)
            paginated.data.map { it.toDomain() }
        }

    override suspend fun updateTransaction(id: String, transaction: Transaction): Result<Transaction> {
        val result = safeApiCall {
            api.updateTransaction(id, transaction.toCreateRequest()).toDomain()
        }
        if (result is Result.Success) {
            triggerRefresh()
        }
        return result
    }

    override suspend fun deleteTransaction(id: String): Result<Unit> {
        val result = safeApiCall {
            api.deleteTransaction(id)
        }
        if (result is Result.Success) {
            triggerRefresh()
        }
        return result
    }

    override suspend fun deleteAllTransactions(accountIds: List<String>?): Result<Unit> {
        val result = safeApiCall {
            api.deleteAllTransactions(accountIds)
        }
        if (result is Result.Success) {
            triggerRefresh()
        }
        return result
    }

    override suspend fun getRecurringBills(): Result<List<RecurringBill>> = safeApiCall {
        api.getRecurringBills()
    }

    override fun getTransactionsPagingFlow(
        accountId: String?,
        isIncome: Boolean?,
        category: String?,
        startDate: String?,
        endDate: String?,
        hasTransactionCost: Boolean?
    ): Flow<PagingData<Transaction>> {
        return createPager {
            TransactionPagingSource(
                repo = this,
                accountId = accountId,
                isIncome = isIncome,
                category = category,
                startDate = startDate,
                endDate = endDate,
                hasTransactionCost = hasTransactionCost
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