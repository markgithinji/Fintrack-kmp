package com.fintrack.shared.feature.transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.transaction.data.DistributionSummary
import com.fintrack.shared.feature.transaction.data.HighlightsSummary
import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.transaction.data.TransactionApi
import com.fintrack.shared.feature.transaction.data.TransactionRepository
import com.fintrack.shared.feature.transaction.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TransactionListViewModel : ViewModel() {
    private val repo = TransactionRepository(TransactionApi())

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
                    val currentList = when (_transactions.value) {
                        is Result.Success -> (_transactions.value as Result.Success).data
                        else -> emptyList()
                    }
                    _transactions.value = Result.Success(currentList + result.data)
                }
            }
        }
    }

    fun resetSaveResult() {
        _saveResult.value = null
    }

    // --- Refresh / first page ---
    fun refresh(limit: Int = 20, sortBy: String = "date", order: String = "DESC") {
        viewModelScope.launch {
            _transactions.value = Result.Loading
            val result = repo.getTransactions(limit, sortBy, order)

            _transactions.value = when (result) {
                is Result.Success -> {
                    val (list, cursor) = result.data
                    nextCursor = cursor
                    Result.Success(list)
                }
                is Result.Error -> Result.Error(result.exception)
                Result.Loading -> Result.Loading
            }
        }
    }

    // --- Load next page ---
    fun loadMore(limit: Int = 20, sortBy: String = "date", order: String = "DESC") {
        val cursor = nextCursor ?: return
        val (afterDate, afterId) = cursor.split("|").let { it[0] to it[1].toInt() }

        viewModelScope.launch {
            val result = repo.getTransactions(limit, sortBy, order, afterDate, afterId)
            if (result is Result.Success) {
                val (list, cursorNext) = result.data
                nextCursor = cursorNext
                val current = (transactions.value as? Result.Success)?.data ?: emptyList()
                _transactions.value = Result.Success(current + list)
            }
        }
    }

    // --- Load recent transactions ---
    fun loadRecentTransactions() {
        viewModelScope.launch {
            _recentTransactions.value = Result.Loading
            _recentTransactions.value = repo.getRecentTransactions()
        }
    }
}
