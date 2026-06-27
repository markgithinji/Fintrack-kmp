package com.fintrack.shared.feature.transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.core.domain.SaveState
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.transaction.domain.model.Category
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import com.fintrack.shared.feature.transaction.domain.usecase.CreateTransactionUseCase
import com.fintrack.shared.feature.transaction.domain.usecase.ValidateTransactionUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

// TODO: add debouncing to validation logic
class TransactionViewModel(
    private val repo: TransactionRepository,
    private val validateTransactionUseCase: ValidateTransactionUseCase,
    private val createTransactionUseCase: CreateTransactionUseCase
) : ViewModel() {

    private val _recentTransactions = MutableStateFlow<Result<List<Transaction>>>(Result.Loading)
    val recentTransactions: StateFlow<Result<List<Transaction>>> = _recentTransactions

    private val _saveState = MutableStateFlow<SaveState<Transaction>>(SaveState.Idle)
    val saveState: StateFlow<SaveState<Transaction>> = _saveState

    private val _deleteResult = MutableStateFlow<Result<Unit>?>(null)
    val deleteResult: StateFlow<Result<Unit>?> = _deleteResult

    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError

    private val _selectedTransaction = MutableStateFlow<Result<Transaction>>(Result.Loading)
    val selectedTransaction: StateFlow<Result<Transaction>> = _selectedTransaction

    private var lastLoadedRecentAccountId: String? = null

    init {
        viewModelScope.launch {
            repo.refreshSignal.collectLatest {
                lastLoadedRecentAccountId?.let { loadRecentTransactions(it, force = true) }
            }
        }
    }

    fun validateTransaction(
        amount: String,
        category: Category?,
        selectedAccount: Account?
    ): Boolean {
        val result = validateTransactionUseCase(amount, category, selectedAccount)

        when (result) {
            is ValidateTransactionUseCase.TransactionValidationResult.Valid -> {
                _validationError.value = null
                return true
            }

            is ValidateTransactionUseCase.TransactionValidationResult.Invalid -> {
                _validationError.value = result.errorMessage
                return false
            }
        }
    }

    fun addTransaction(
        amount: String,
        isIncome: Boolean,
        category: Category?,
        description: String,
        selectedAccount: Account?,
        dateTime: LocalDateTime
    ) {
        // Validate first
        if (!validateTransaction(amount, category, selectedAccount)) {
            return
        }

        val transaction = createTransactionUseCase(
            amount = amount,
            isIncome = isIncome,
            category = category,
            description = description,
            selectedAccount = selectedAccount,
            dateTime = dateTime
        ) ?: return

        // Save transaction
        viewModelScope.launch {
            _saveState.value = SaveState.Idle
            _saveState.value = SaveState.Loading

            try {
                val result = repo.addTransaction(transaction)
                _saveState.value = when (result) {
                    is Result.Success -> SaveState.Success(result.data)
                    is Result.Error -> SaveState.Error(result.exception)
                    is Result.Loading -> SaveState.Loading
                }
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e)
            }
        }
    }

    fun updateTransaction(
        id: String,
        amount: String,
        isIncome: Boolean,
        category: Category?,
        description: String,
        selectedAccount: Account?,
        dateTime: LocalDateTime
    ) {
        // Validate first
        if (!validateTransaction(amount, category, selectedAccount)) {
            return
        }

        val transaction = createTransactionUseCase(
            amount = amount,
            isIncome = isIncome,
            category = category,
            description = description,
            selectedAccount = selectedAccount,
            dateTime = dateTime
        )?.copy(id = id) ?: return

        viewModelScope.launch {
            _saveState.value = SaveState.Idle
            _saveState.value = SaveState.Loading

            try {
                val result = repo.updateTransaction(id, transaction)
                _saveState.value = when (result) {
                    is Result.Success -> SaveState.Success(result.data)
                    is Result.Error -> SaveState.Error(result.exception)
                    is Result.Loading -> SaveState.Loading
                }
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e)
            }
        }
    }

    fun loadTransactionById(id: String) {
        val current = _selectedTransaction.value
        if (current is Result.Success && current.data.id == id) return

        viewModelScope.launch {
            _selectedTransaction.value = Result.Loading
            _selectedTransaction.value = repo.getTransaction(id)
        }
    }

    fun resetSelectedTransaction() {
        _selectedTransaction.value = Result.Loading
    }

    // Keeping the old method for backward compatibility
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _saveState.value = SaveState.Idle
            _saveState.value = SaveState.Loading

            try {
                val result = repo.addTransaction(transaction)
                _saveState.value = when (result) {
                    is Result.Success -> SaveState.Success(result.data)
                    is Result.Error -> SaveState.Error(result.exception)
                    is Result.Loading -> SaveState.Loading
                }
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e)
            }
        }
    }

    fun loadRecentTransactions(accountId: String?, force: Boolean = false) {
        if (accountId == null) {
            _recentTransactions.value = Result.Error(Exception("No account selected"))
            return
        }

        if (!force && _recentTransactions.value is Result.Success && lastLoadedRecentAccountId == accountId) {
            return
        }

        viewModelScope.launch {
            _recentTransactions.value = Result.Loading
            lastLoadedRecentAccountId = accountId
            val result = repo.getTransactions(
                limit = 6,
                sortBy = "date",
                order = "DESC",
                accountId = accountId,
                isIncome = null
            )
            _recentTransactions.value = when (result) {
                is Result.Success -> Result.Success(result.data.first)
                is Result.Error -> Result.Error(result.exception)
                is Result.Loading -> Result.Loading
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTransactionsPagingData(
        accountId: String?,
        isIncome: Boolean? = null
    ): Flow<PagingData<Transaction>> {
        return repo.refreshSignal
            .onStart { emit(Unit) }
            .flatMapLatest {
                repo.getTransactionsPagingFlow(accountId, isIncome)
            }
            .cachedIn(viewModelScope)
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            _deleteResult.value = Result.Loading
            _deleteResult.value = repo.deleteTransaction(id)
        }
    }

    fun resetDeleteResult() {
        _deleteResult.value = null
    }
}