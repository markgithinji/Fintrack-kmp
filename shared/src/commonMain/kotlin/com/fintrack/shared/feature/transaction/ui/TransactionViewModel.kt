package com.fintrack.shared.feature.transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.transaction.data.Result
import com.fintrack.shared.feature.transaction.data.Summary
import com.fintrack.shared.feature.transaction.data.TransactionApi
import com.fintrack.shared.feature.transaction.data.TransactionRepository
import com.fintrack.shared.feature.transaction.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TransactionViewModel : ViewModel() {
    private val repo = TransactionRepository(TransactionApi())

    // Transactions state (full list / paginated)
    private val _transactions = MutableStateFlow<Result<List<Transaction>>>(Result.Loading)
    val transactions: StateFlow<Result<List<Transaction>>> = _transactions

    // Keep track of pagination cursor
    private var nextCursor: String? = null

    // Recent transactions (e.g. dashboard preview)
    private val _recentTransactions = MutableStateFlow<Result<List<Transaction>>>(Result.Loading)
    val recentTransactions: StateFlow<Result<List<Transaction>>> = _recentTransactions

    // Save transaction state
    private val _saveResult = MutableStateFlow<Result<Transaction>?>(null)
    val saveResult: StateFlow<Result<Transaction>?> = _saveResult

    // Summary state
    private val _summary = MutableStateFlow<Result<Summary>>(Result.Loading)
    val summary: StateFlow<Result<Summary>> = _summary


    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _saveResult.value = Result.Loading
            _saveResult.value = repo.addTransaction(transaction).also { result ->
                if (result is Result.Success) {
                    val currentList = when (_transactions.value) {
                        is Result.Success -> (_transactions.value as Result.Success).data
                        else -> emptyList()
                    }
                    // Update transactions list with new item
                    _transactions.value = Result.Success(currentList + result.data)
                }
            }
        }
    }

    fun resetSaveResult() {
        _saveResult.value = null
    }

    /**
     * Initial load / refresh of transactions (page 1).
     */
    fun refresh(
        limit: Int = 20,
        sortBy: String = "date",
        order: String = "DESC"
    ) {
        viewModelScope.launch {
            _transactions.value = Result.Loading
            val result = repo.getTransactions(
                limit = limit,
                sortBy = sortBy,
                order = order
            )

            if (result is Result.Success) {
                val (list, cursor) = result.data
                nextCursor = cursor
                _transactions.value = Result.Success(list)
            } else if (result is Result.Error) {
                println("Failed to refresh transactions: ${result.exception.message}")
                result.exception.printStackTrace()
                _transactions.value = result
            }
        }
    }


    /**
     * Load the next page of transactions using cursor.
     */
    fun loadMore(
        limit: Int = 20,
        sortBy: String = "date",
        order: String = "DESC"
    ) {
        val cursor = nextCursor ?: return // nothing to load
        val (afterDate, afterId) = cursor.split("|").let {
            it[0] to it[1].toInt()
        }

        viewModelScope.launch {
            val result = repo.getTransactions(
                limit = limit,
                sortBy = sortBy,
                order = order,
                afterDate = afterDate,
                afterId = afterId
            )

            if (result is Result.Success) {
                val (list, cursorNext) = result.data
                nextCursor = cursorNext
                val current = when (_transactions.value) {
                    is Result.Success -> (_transactions.value as Result.Success).data
                    else -> emptyList()
                }
                _transactions.value = Result.Success(current + list)
            } else {
                println("Failed to load more: ${(result as? Result.Error)?.exception?.message}")
            }
        }
    }

    /**
     * Load only the most recent 6 transactions.
     */
    fun loadRecentTransactions() {
        viewModelScope.launch {
            _recentTransactions.value = Result.Loading
            val result = repo.getRecentTransactions()
            if (result is Result.Error) {
                println("Failed to load recent transactions: ${result.exception.message}")
                result.exception.printStackTrace()
            }
            _recentTransactions.value = result
        }
    }

    fun loadSummary() {
        viewModelScope.launch {
            _summary.value = Result.Loading
            _summary.value = repo.getSummary()
        }
    }
}

