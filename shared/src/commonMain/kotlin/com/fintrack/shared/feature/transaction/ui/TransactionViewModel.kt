package com.fintrack.shared.feature.transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.core.domain.SaveState
import com.fintrack.shared.feature.core.util.GlobalRefreshManager
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.transaction.domain.model.Category
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import com.fintrack.shared.feature.transaction.domain.usecase.CreateTransactionUseCase
import com.fintrack.shared.feature.transaction.domain.usecase.GetCategoriesUseCase
import com.fintrack.shared.feature.transaction.domain.usecase.ValidateTransactionUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(FlowPreview::class, ExperimentalTime::class)
class TransactionViewModel(
    private val repo: TransactionRepository,
    private val validateTransactionUseCase: ValidateTransactionUseCase,
    private val createTransactionUseCase: CreateTransactionUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val transactionImporter: com.fintrack.shared.feature.transaction.domain.util.TransactionImporter,
    private val globalRefreshManager: GlobalRefreshManager
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    private val _transactionCost = MutableStateFlow("")
    val transactionCost: StateFlow<String> = _transactionCost.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _selectedAccount = MutableStateFlow<Account?>(null)
    val selectedAccount: StateFlow<Account?> = _selectedAccount.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

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

    private val _importState = MutableStateFlow<Result<Unit>?>(null)
    val importState: StateFlow<Result<Unit>?> = _importState

    private var lastLoadedRecentAccountId: String? = null

    init {
        viewModelScope.launch {
            globalRefreshManager.refreshEvent.collect {
                repo.triggerRefresh()
                refreshCategories()
            }
        }
        viewModelScope.launch {
            repo.refreshSignal.collectLatest {
                lastLoadedRecentAccountId?.let { loadRecentTransactions(it, force = true) }
            }
        }
        viewModelScope.launch {
            getCategoriesUseCase().collect {
                _categories.value = it
            }
        }
        
        refreshCategories()
    }

    fun onAmountChange(newAmount: String) {
        _amount.value = newAmount
        _validationError.value = null
    }

    fun onTransactionCostChange(newCost: String) {
        _transactionCost.value = newCost
        _validationError.value = null
    }

    fun onCategoryChange(newCategory: Category?) {
        _selectedCategory.value = newCategory
        _validationError.value = null
    }

    fun onAccountChange(newAccount: Account?) {
        _selectedAccount.value = newAccount
        _validationError.value = null
    }

    fun onDescriptionChange(newDescription: String) {
        _description.value = newDescription
        _validationError.value = null
    }

    fun refreshCategories() {
        viewModelScope.launch {
            try {
                getCategoriesUseCase.refresh()
            } catch (e: Exception) {
                // Ignore error here or log it
            }
        }
    }

    fun validateTransaction(
        amount: String,
        transactionCost: String,
        description: String,
        category: Category?,
        selectedAccount: Account?
    ): Boolean {
        val result = validateTransactionUseCase(amount, transactionCost, description, category, selectedAccount)

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
        transactionCost: String,
        isIncome: Boolean,
        category: Category?,
        description: String,
        selectedAccount: Account?,
        dateTime: Instant
    ) {
        // Validate first
        if (!validateTransaction(amount, transactionCost, description, category, selectedAccount)) {
            return
        }

        val transaction = createTransactionUseCase(
            amount = amount,
            transactionCost = transactionCost,
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
                    is Result.Success -> {
                        globalRefreshManager.triggerRefresh()
                        SaveState.Success(result.data)
                    }
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
        transactionCost: String,
        isIncome: Boolean,
        category: Category?,
        description: String,
        selectedAccount: Account?,
        dateTime: Instant
    ) {
        // Validate first
        if (!validateTransaction(amount, transactionCost, description, category, selectedAccount)) {
            return
        }

        val transaction = createTransactionUseCase(
            amount = amount,
            transactionCost = transactionCost,
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
                    is Result.Success -> {
                        globalRefreshManager.triggerRefresh()
                        SaveState.Success(result.data)
                    }
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
                    is Result.Success -> {
                        globalRefreshManager.triggerRefresh()
                        SaveState.Success(result.data)
                    }
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
            val result = repo.deleteTransaction(id)
            _deleteResult.value = result
            if (result is Result.Success) {
                globalRefreshManager.triggerRefresh()
            }
        }
    }

    fun importMpesaTransactions() {
        viewModelScope.launch {
            _importState.value = Result.Loading
            try {
                transactionImporter.importHistory()
                _importState.value = Result.Success(Unit)
                globalRefreshManager.triggerRefresh()
            } catch (e: Exception) {
                _importState.value = Result.Error(e)
            }
        }
    }

    fun resetImportState() {
        _importState.value = null
    }

    fun resetDeleteResult() {
        _deleteResult.value = null
    }

    fun clearValidationError() {
        _validationError.value = null
    }
}
