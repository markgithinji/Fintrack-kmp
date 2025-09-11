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

    // Transactions state wrapped in Result
    private val _transactions = MutableStateFlow<Result<List<Transaction>>>(Result.Loading)
    val transactions: StateFlow<Result<List<Transaction>>> = _transactions

    // Save transaction state wrapped in Result
    private val _saveResult = MutableStateFlow<Result<Transaction>?>(null)
    val saveResult: StateFlow<Result<Transaction>?> = _saveResult

    private val _summary = MutableStateFlow<Summary?>(null)
    val summary: StateFlow<Summary?> = _summary

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

    fun refresh() {
        viewModelScope.launch {
            _transactions.value = Result.Loading
            _transactions.value = repo.getTransactions()
        }
    }


    fun loadSummary() {
        viewModelScope.launch {
            try {
                when (val result = repo.getSummary()) {
                    is Result.Success -> _summary.value = result.data
                    is Result.Error -> {
                        _summary.value = null
                        println("Failed to load summary: ${result.exception.message}")
                        // Optionally expose error state via another StateFlow
                    }

                    Result.Loading -> TODO()
                }
            } catch (e: Exception) {
                _summary.value = null
                println("Failed to load summary: ${e.message}")
            }
        }
    }
}
