package com.fintrack.shared.feature.transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.category.data.LocalCategoryDataSource
import com.fintrack.shared.feature.category.domain.model.Category
import com.fintrack.shared.feature.category.domain.usecase.SyncCategoriesUseCase
import com.fintrack.shared.feature.core.domain.SaveState
import com.fintrack.shared.feature.core.logger.KMPLogger
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import com.fintrack.shared.feature.transaction.domain.model.TransactionFormState
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import com.fintrack.shared.feature.transaction.domain.usecase.CreateTransactionUseCase
import com.fintrack.shared.feature.transaction.domain.usecase.ValidateTransactionUseCase
import com.fintrack.shared.feature.transaction.domain.util.TransactionImporter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Instant

@OptIn(FlowPreview::class)
class TransactionViewModel(
    private val repo: TransactionRepository,
    private val localCategoryDataSource: LocalCategoryDataSource,
    private val syncCategoriesUseCase: SyncCategoriesUseCase,
    private val validateTransactionUseCase: ValidateTransactionUseCase,
    private val createTransactionUseCase: CreateTransactionUseCase,
    private val transactionImporter: TransactionImporter
) : ViewModel() {

    private val _categories = MutableStateFlow(Category.allCategories)
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _formState = MutableStateFlow(TransactionFormState())
    val formState: StateFlow<TransactionFormState> = _formState.asStateFlow()

    private val _recentTransactions = MutableStateFlow<Result<List<Transaction>>>(Result.Loading)
    val recentTransactions: StateFlow<Result<List<Transaction>>> = _recentTransactions

    private val _saveState = MutableStateFlow<SaveState<Transaction>>(SaveState.Idle)
    val saveState: StateFlow<SaveState<Transaction>> = _saveState

    private val _deleteResult = MutableStateFlow<Result<Unit>?>(null)
    val deleteResult: StateFlow<Result<Unit>?> = _deleteResult

    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError

    private val _importState = MutableStateFlow<Result<Unit>?>(null)
    val importState: StateFlow<Result<Unit>?> = _importState

    private val _importProgress = MutableStateFlow(0f)
    val importProgress: StateFlow<Float> = _importProgress.asStateFlow()

    private val logger = KMPLogger()

    private var hasAutoSynced = false
    private var lastLoadedRecentAccountId: String? = null
    private var recentTransactionsJob: Job? = null
    private var importJob: Job? = null

    init {
        viewModelScope.launch {
            localCategoryDataSource.categories.collect {
                _categories.value = it
            }
        }

        refreshCategories()
    }

    fun onAmountChange(newAmount: String) {
        _formState.update { it.copy(amount = newAmount) }
        _validationError.value = null
    }

    fun onTransactionCostChange(newCost: String) {
        _formState.update { it.copy(transactionCost = newCost) }
        _validationError.value = null
    }

    fun onCategoryChange(newCategory: Category?) {
        _formState.update { it.copy(selectedCategory = newCategory) }
        _validationError.value = null
    }

    fun onAccountChange(newAccount: Account?) {
        _formState.update { it.copy(selectedAccount = newAccount) }
        _validationError.value = null
    }

    fun onDescriptionChange(newDescription: String) {
        _formState.update { it.copy(description = newDescription) }
        _validationError.value = null
    }

    fun onTypeChange(isIncome: Boolean) {
        _formState.update { it.copy(isIncome = isIncome) }
        _validationError.value = null
    }

    fun onDateTimeChange(dateTime: Instant) {
        _formState.update { it.copy(dateTime = dateTime) }
        _validationError.value = null
    }

    fun refreshCategories() {
        viewModelScope.launch {
            syncCategoriesUseCase()
        }
    }

    fun validateTransaction(
        amount: String,
        transactionCost: String,
        description: String,
        category: Category?,
        selectedAccount: Account?
    ): Boolean {
        val result = validateTransactionUseCase(
            amount,
            transactionCost,
            description,
            category,
            selectedAccount
        )

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

    fun addTransaction() {
        val state = _formState.value
        // Validate first
        if (!validateTransaction(
                state.amount,
                state.transactionCost,
                state.description,
                state.selectedCategory,
                state.selectedAccount
            )
        ) {
            return
        }

        val transaction = createTransactionUseCase(
            amount = state.amount,
            transactionCost = state.transactionCost,
            isIncome = state.isIncome,
            category = state.selectedCategory,
            description = state.description,
            selectedAccount = state.selectedAccount,
            dateTime = state.dateTime
        ) ?: return

        // Save transaction
        viewModelScope.launch {
            _saveState.value = SaveState.Idle
            _saveState.value = SaveState.Loading

            try {
                val result = repo.addTransaction(transaction)
                _saveState.value = when (result) {
                    is Result.Success -> {
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

    fun updateTransaction(id: String) {
        val state = _formState.value
        // Validate first
        if (!validateTransaction(
                state.amount,
                state.transactionCost,
                state.description,
                state.selectedCategory,
                state.selectedAccount
            )
        ) {
            return
        }

        val transaction = createTransactionUseCase(
            amount = state.amount,
            transactionCost = state.transactionCost,
            isIncome = state.isIncome,
            category = state.selectedCategory,
            description = state.description,
            selectedAccount = state.selectedAccount,
            dateTime = state.dateTime
        )?.copy(id = id) ?: return

        viewModelScope.launch {
            _saveState.value = SaveState.Idle
            _saveState.value = SaveState.Loading

            try {
                val result = repo.updateTransaction(id, transaction)
                _saveState.value = when (result) {
                    is Result.Success -> {
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

    private var loadedTransactionId: String? = null

    fun loadTransactionById(id: String, accounts: List<Account>) {
        if (loadedTransactionId == id) return

        viewModelScope.launch {
            val result = repo.getTransaction(id)

            if (result is Result.Success) {
                val transaction = result.data
                loadedTransactionId = transaction.id
                val category = if (transaction.categoryId != null) {
                    Category.fromId(
                        transaction.categoryId,
                        name = transaction.category,
                        isExpense = !transaction.isIncome
                    )
                } else {
                    Category.fromName(transaction.category, isExpense = !transaction.isIncome)
                }

                _formState.value = TransactionFormState(
                    amount = transaction.amount.toPlainString().let { s ->
                        if (s.contains(".")) s.trimEnd('0').trimEnd('.') else s
                    },
                    transactionCost = transaction.transactionCost.toPlainString().let { s ->
                        if (s.contains(".")) s.trimEnd('0').trimEnd('.') else s
                    },
                    isIncome = transaction.isIncome,
                    selectedCategory = category,
                    selectedAccount = accounts.find { it.id == transaction.accountId },
                    description = transaction.description ?: "",
                    dateTime = transaction.dateTime
                )
            }
        }
    }

    fun resetSelectedTransaction() {
        loadedTransactionId = null
        _formState.value = TransactionFormState()
    }

    fun loadRecentTransactions(accountId: String?, force: Boolean = false) {
        if (accountId == null) {
            _recentTransactions.value = Result.Error(Exception("No account selected"))
            return
        }

        if (!force && _recentTransactions.value is Result.Success && lastLoadedRecentAccountId == accountId) {
            return
        }

        recentTransactionsJob?.cancel()
        recentTransactionsJob = viewModelScope.launch {
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

    private var lastPagingParams: TransactionPagingParams? = null
    private var cachedPagingFlow: Flow<PagingData<Transaction>>? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTransactionsPagingData(
        accountId: String?,
        isIncome: Boolean? = null,
        category: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        hasTransactionCost: Boolean? = null
    ): Flow<PagingData<Transaction>> {
        val newParams = TransactionPagingParams(
            accountId = accountId,
            isIncome = isIncome,
            category = category,
            startDate = startDate,
            endDate = endDate,
            hasTransactionCost = hasTransactionCost
        )

        if (newParams == lastPagingParams && cachedPagingFlow != null) {
            return cachedPagingFlow!!
        }

        lastPagingParams = newParams
        val flow = repo.getTransactionsPagingFlow(
            accountId = accountId,
            isIncome = isIncome,
            category = category,
            startDate = startDate,
            endDate = endDate,
            hasTransactionCost = hasTransactionCost
        )
            .cachedIn(viewModelScope)

        cachedPagingFlow = flow
        return flow
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            _deleteResult.value = Result.Loading
            val result = repo.deleteTransaction(id)
            _deleteResult.value = result
        }
    }

    fun importTransactions() {
        logger.info(
            "SYNC_FLOW",
            "importTransactions triggered. Current state: ${_importState.value}"
        )
        if (_importState.value is Result.Loading) {
            logger.info("SYNC_FLOW", "Already importing, skipping.")
            return
        }

        importJob?.cancel()
        importJob = viewModelScope.launch {
            _importState.value = Result.Loading
            _importProgress.value = 0f
            logger.info("SYNC_FLOW", "Starting transaction import job")
            try {
                transactionImporter.importHistory { progress ->
                    _importProgress.value = progress
                }
                logger.info("SYNC_FLOW", "Transaction import completed successfully")
                _importState.value = Result.Success(Unit)
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    logger.error("SYNC_FLOW", "Transaction import failed", e)
                    _importState.value = Result.Error(e)
                } else {
                    logger.info("SYNC_FLOW", "Transaction import job cancelled")
                }
            }
        }
    }

    fun cancelImport() {
        logger.info("SYNC_FLOW", "cancelImport called. Cancelling import job.")
        importJob?.cancel()
        resetImportState()
    }

    fun autoSyncTransactions() {
        if (!hasAutoSynced) {
            hasAutoSynced = true
            importTransactions()
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

private data class TransactionPagingParams(
    val accountId: String?,
    val isIncome: Boolean?,
    val category: String?,
    val startDate: String?,
    val endDate: String?,
    val hasTransactionCost: Boolean?
)
