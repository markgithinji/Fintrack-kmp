package com.fintrack.shared.feature.transaction.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import com.fintrack.shared.feature.core.util.Result

class TransactionPagingSource(
    private val repo: TransactionRepository,
    private val accountId: String?,
    private val isIncome: Boolean? = null
) : PagingSource<String, Transaction>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Transaction> {
        return try {
            val cursor = params.key
            val afterDateTime = cursor?.split("|")?.getOrNull(0)
            val afterId = cursor?.split("|")?.getOrNull(1)

            val result = repo.getTransactions(
                limit = params.loadSize,
                sortBy = "date",
                order = "DESC",
                afterDateTime = afterDateTime,
                afterId = afterId,
                accountId = accountId,
                isIncome = isIncome
            )

            when (result) {
                is Result.Success -> {
                    val (transactions, nextCursor) = result.data

                    val validNextKey = if (transactions.isEmpty() || nextCursor == cursor) null else nextCursor

                    LoadResult.Page(
                        data = transactions,
                        prevKey = null,
                        nextKey = validNextKey
                    )
                }
                is Result.Error -> LoadResult.Error(result.exception)
                is Result.Loading -> LoadResult.Error(Exception("Unexpected loading state"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, Transaction>): String? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
        }
    }
}