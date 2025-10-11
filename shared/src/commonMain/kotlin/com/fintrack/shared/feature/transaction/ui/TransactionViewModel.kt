package com.fintrack.shared.feature.transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val repo: TransactionRepository
) : ViewModel() {

    private var currentAccountId: String? = null

    // --- Transactions state (full list / paginated) ---
    private val _transactions = MutableStateFlow<Result<List<Transaction>>>(Result.Loading)
    val transactions: StateFlow<Result<List<Transaction>>> = _transactions
    private var nextCursor: String? = null

    // --- Recent transactions (dashboard preview) ---
    private val _recentTransactions = MutableStateFlow<Result<List<Transaction>>>(Result.Loading)
    val recentTransactions: StateFlow<Result<List<Transaction>>> = _recentTransactions

    // --- Save transaction state ---
    private val _saveResult = MutableStateFlow<Result<Transaction>?>(null)
    val saveResult: StateFlow<Result<Transaction>?> = _saveResult

    // --- Add transaction ---
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _saveResult.value = Result.Loading
            _saveResult.value = repo.addTransaction(transaction).also { result ->
                if (result is Result.Success) {
                    val currentList = (transactions.value as? Result.Success)?.data ?: emptyList()
                    _transactions.value = Result.Success(currentList + result.data)
                }
            }
        }
    }

    fun resetSaveResult() {
        _saveResult.value = null
    }

    // --- Refresh / first page ---
    fun refresh(
        accountId: String? = currentAccountId,
        limit: Int = 20,
        sortBy: String = "date",
        order: String = "DESC"
    ) {
        currentAccountId = accountId
        viewModelScope.launch {
            _transactions.value = Result.Loading
            val result = repo.getTransactions(limit, sortBy, order, accountId = accountId)

            _transactions.value = when (result) {
                is Result.Success -> {
                    val (list, cursor) = result.data
                    nextCursor = cursor
                    Result.Success(list)
                }

                is Result.Error -> Result.Error(result.exception)
                is Result.Loading -> Result.Loading
            }
        }
    }

    // --- Load next page ---
    fun loadMore(limit: Int = 20, sortBy: String = "date", order: String = "DESC") {
        val cursor = nextCursor ?: return
        val (afterDate, afterId) = cursor.split("|").let { it[0] to it[1] }

        viewModelScope.launch {
            val result = repo.getTransactions(
                limit,
                sortBy,
                order,
                afterDate,
                afterId,
                accountId = currentAccountId
            )
            if (result is Result.Success) {
                val (list, cursorNext) = result.data
                nextCursor = cursorNext
                val current = (transactions.value as? Result.Success)?.data ?: emptyList()
                _transactions.value = Result.Success(current + list)
            }
        }
    }

    // --- Load recent transactions ---
    fun loadRecentTransactions(accountId: String? = currentAccountId) {
        currentAccountId = accountId
        viewModelScope.launch {
            _recentTransactions.value = Result.Loading
            val result = repo.getTransactions(
                limit = 6,
                sortBy = "date",
                order = "DESC",
                accountId = accountId
            )
            _recentTransactions.value = when (result) {
                is Result.Success -> Result.Success(result.data.first) // extract transactions list
                is Result.Error -> Result.Error(result.exception)
                is Result.Loading -> Result.Loading
            }
        }
    }
}
