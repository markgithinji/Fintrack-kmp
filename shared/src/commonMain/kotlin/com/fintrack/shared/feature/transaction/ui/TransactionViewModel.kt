package com.fintrack.shared.feature.transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val repo: TransactionRepository
) : ViewModel() {

    private var currentAccountId: String? = null
    private var currentIsIncome: Boolean? = null

    fun getTransactionsPagingData(
        accountId: String?,
        isIncome: Boolean? = null
    ): Flow<PagingData<Transaction>> {
        currentAccountId = accountId
        currentIsIncome = isIncome

        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            pagingSourceFactory = {
                repo.getTransactionsPagingSource(
                    accountId = accountId,
                    isIncome = isIncome
                )
            }
        ).flow.cachedIn(viewModelScope)
    }

    private val _recentTransactions = MutableStateFlow<Result<List<Transaction>>>(Result.Loading)
    val recentTransactions: StateFlow<Result<List<Transaction>>> = _recentTransactions

    private val _saveResult = MutableStateFlow<Result<Transaction>?>(null)
    val saveResult: StateFlow<Result<Transaction>?> = _saveResult

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _saveResult.value = Result.Loading
            _saveResult.value = repo.addTransaction(transaction)
        }
    }

    fun resetSaveResult() {
        _saveResult.value = null
    }

    fun loadRecentTransactions(accountId: String? = currentAccountId) {
        currentAccountId = accountId
        viewModelScope.launch {
            _recentTransactions.value = Result.Loading
            val result = repo.getRecentTransactions(accountId)
            _recentTransactions.value = when (result) {
                is Result.Success -> Result.Success(result.data)
                is Result.Error -> Result.Error(result.exception)
                is Result.Loading -> Result.Loading
            }
        }
    }
}