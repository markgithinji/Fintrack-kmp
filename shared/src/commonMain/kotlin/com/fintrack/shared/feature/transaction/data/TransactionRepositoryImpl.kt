package com.fintrack.shared.feature.transaction.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.fintrack.shared.feature.core.logger.KMPLogger
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
    private val api: TransactionApi,
) : TransactionRepository {

    private val logger = KMPLogger()

    private val _dataChangedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val dataChangedEvent: Flow<Unit> = _dataChangedEvent.asSharedFlow()

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
        return safeApiCall {
            val createRequest = transaction.toCreateRequest()
            val dto = api.addTransaction(createRequest)
            _dataChangedEvent.tryEmit(Unit)
            dto.toDomain()
        }
    }

    override suspend fun addTransactions(transactions: List<Transaction>): Result<Unit> {
        return safeApiCall {
            val requests = transactions.map { it.toCreateRequest() }
            api.addTransactions(requests)
            _dataChangedEvent.tryEmit(Unit)
        }
    }

    override suspend fun importMpesaTransactions(transactions: List<Transaction>): Result<Unit> {
        logger.debug("SYNC_FLOW", "Repository: importMpesaTransactions called with ${transactions.size} transactions")
        val result = safeApiCall {
            val requests = transactions.map { it.toCreateRequest() }
            api.importMpesaTransactions(requests)
        }
        if (result is Result.Success) {
            logger.debug("SYNC_FLOW", "Repository: importMpesaTransactions success")
            _dataChangedEvent.tryEmit(Unit)
        }
        if (result is Result.Error) {
            logger.error("SYNC_FLOW", "Repository: importMpesaTransactions failed", result.exception)
        }
        return result
    }

    override suspend fun importEquityTransactions(transactions: List<Transaction>): Result<Unit> {
        logger.debug("SYNC_FLOW", "Repository: importEquityTransactions called with ${transactions.size} transactions")
        val result = safeApiCall {
            val requests = transactions.map { it.toCreateRequest() }
            api.importEquityTransactions(requests)
        }
        if (result is Result.Success) {
            logger.debug("SYNC_FLOW", "Repository: importEquityTransactions success")
            _dataChangedEvent.tryEmit(Unit)
        } else if (result is Result.Error) {
            logger.error("SYNC_FLOW", "Repository: importEquityTransactions failed", result.exception)
        }
        return result
    }

    override suspend fun getTransaction(id: String): Result<Transaction> =
        safeApiCall {
            api.getTransaction(id).toDomain()
        }

    override suspend fun getAllTransactions(
        startDate: String?,
        endDate: String?,
        accountId: String?
    ): Result<List<Transaction>> =
        safeApiCall {
            // Fetching with a large limit for export. 
            val paginated = api.getTransactions(
                limit = 2000,
                startDate = startDate,
                endDate = endDate,
                accountId = accountId
            )
            paginated.data.map { it.toDomain() }
        }

    override suspend fun updateTransaction(id: String, transaction: Transaction): Result<Transaction> {
        return safeApiCall {
            val result = api.updateTransaction(id, transaction.toCreateRequest()).toDomain()
            _dataChangedEvent.tryEmit(Unit)
            result
        }
    }

    override suspend fun deleteTransaction(id: String): Result<Unit> {
        return safeApiCall {
            api.deleteTransaction(id)
            _dataChangedEvent.tryEmit(Unit)
        }
    }

    override suspend fun deleteAllTransactions(accountIds: List<String>?): Result<Unit> {
        return safeApiCall {
            api.deleteAllTransactions(accountIds)
            _dataChangedEvent.tryEmit(Unit)
        }
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
