package com.fintrack.shared.feature.transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.transaction.data.TransactionApi
import com.fintrack.shared.feature.transaction.data.TransactionRepository
import com.fintrack.shared.feature.transaction.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TransactionViewModel : ViewModel() {
    private val repo = TransactionRepository(TransactionApi())

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val _saveResult = MutableStateFlow<Boolean?>(null) // null = idle, true = success, false = error
    val saveResult: StateFlow<Boolean?> = _saveResult


    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                val savedTransaction = repo.addTransaction(transaction)
                _transactions.value = _transactions.value + savedTransaction
                _saveResult.value = true // success
            } catch (e: Exception) {
                println("Error adding transaction: ${e.message}")
                _saveResult.value = false // error
            }
        }
    }

    fun resetSaveResult() {
        _saveResult.value = null
    }

    fun refresh() {
        viewModelScope.launch {
            _transactions.value = repo.getTransactions()
        }
    }
}
