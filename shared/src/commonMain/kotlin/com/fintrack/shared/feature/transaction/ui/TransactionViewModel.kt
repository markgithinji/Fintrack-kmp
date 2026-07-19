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
import com.fintrack.shared.feature.core.domain.ValidationResult
import com.fintrack.shared.feature.core.logger.KMPLogger
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.util.formatToTwoPrecision
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
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
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

    private var lastLoadedRecentAccountId: String? = null
    private var recentTransactionsJob: Job? = null
    private var importJob: Job? = null
    private var lastAutoSyncTime: Instant? = null

    init {
        logger.error("TransactionViewModel", "INSTANCE CREATED: ${this.hashCode()}")
        viewModelScope.launch {
            localCategoryDataSource.categories.collect {
                _categories.value = it
            }
        }

        refreshCategories()
    }

    fun onAmountChange(newAmount: String, selectionStart: Int? = null, selectionEnd: Int? = null) {
        _formState.update {
            it.copy(
                amount = newAmount,
                amountSelectionStart = selectionStart ?: newAmount.length,
                amountSelectionEnd = selectionEnd ?: newAmount.length
            )
        }
        _validationError.value = null
    }

    fun onAmountSelectionChange(start: Int, end: Int) {
        _formState.update { it.copy(amountSelectionStart = start, amountSelectionEnd = end) }
    }

    fun handleAmountInput(input: String) {
        val current = _formState.value
        val amount = current.amount
        val start = current.amountSelectionStart
        val end = current.amountSelectionEnd

        if (input == "." && amount.contains(".")) return
        if (amount.length >= 12 && start == end && start == amount.length) return

        val newAmount = amount.take(start) + input + amount.drop(end)

        val newSelection = start + input.length
        onAmountChange(newAmount, newSelection, newSelection)
    }

    fun handleAmountBackspace() {
        val current = _formState.value
        val amount = current.amount
        val start = current.amountSelectionStart
        val end = current.amountSelectionEnd

        if (start == 0 && end == 0) return

        val newAmount: String
        val newSelection: Int
        if (start != end) {
            newAmount = amount.take(start) + amount.drop(end)
            newSelection = start
        } else {
            newAmount = amount.take(start - 1) + amount.drop(start)
            newSelection = start - 1
        }
        onAmountChange(newAmount, newSelection, newSelection)
    }

    fun onTransactionCostChange(
        newCost: String,
        selectionStart: Int? = null,
        selectionEnd: Int? = null
    ) {
        _formState.update {
            it.copy(
                transactionCost = newCost,
                costSelectionStart = selectionStart ?: newCost.length,
                costSelectionEnd = selectionEnd ?: newCost.length
            )
        }
        _validationError.value = null
    }

    fun onCostSelectionChange(start: Int, end: Int) {
        _formState.update { it.copy(costSelectionStart = start, costSelectionEnd = end) }
    }

    fun handleCostInput(input: String) {
        val current = _formState.value
        val cost = current.transactionCost
        val start = current.costSelectionStart
        val end = current.costSelectionEnd

        if (input == "." && cost.contains(".")) return
        if (cost.length >= 10 && start == end && start == cost.length) return

        val newCost = cost.take(start) + input + cost.drop(end)

        val newSelection = start + input.length
        onTransactionCostChange(newCost, newSelection, newSelection)
    }

    fun handleCostBackspace() {
        val current = _formState.value
        val cost = current.transactionCost
        val start = current.costSelectionStart
        val end = current.costSelectionEnd

        if (start == 0 && end == 0) return

        val newCost: String
        val newSelection: Int
        if (start != end) {
            newCost = cost.take(start) + cost.drop(end)
            newSelection = start
        } else {
            newCost = cost.take(start - 1) + cost.drop(start)
            newSelection = start - 1
        }
        onTransactionCostChange(newCost, newSelection, newSelection)
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
            is ValidationResult.Success -> {
                _validationError.value = null
                return true
            }

            is ValidationResult.Error -> {
                _validationError.value = result.message
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
                val category = Category.fromId(
                    transaction.categoryId,
                    name = transaction.category,
                    isExpense = !transaction.isIncome
                )

                _formState.value = TransactionFormState(
                    amount = transaction.amount.formatToTwoPrecision(),
                    transactionCost = transaction.transactionCost.formatToTwoPrecision(),
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

        val current = _recentTransactions.value
        if (!force && current is Result.Success && lastLoadedRecentAccountId == accountId) {
            return
        }

        recentTransactionsJob?.cancel()
        recentTransactionsJob = viewModelScope.launch {
            // Only show loading if we don't have data or if the account changed
            if (current !is Result.Success || lastLoadedRecentAccountId != accountId) {
                _recentTransactions.value = Result.Loading
            }

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
        categoryId: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        hasTransactionCost: Boolean? = null
    ): Flow<PagingData<Transaction>> {
        val newParams = TransactionPagingParams(
            accountId = accountId,
            isIncome = isIncome,
            categoryId = categoryId,
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
            categoryId = categoryId,
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

    fun importTransactions(accountId: String? = null, isPortfolioSeed: Boolean = false) {
        logger.info(
            "SYNC_FLOW",
            "importTransactions triggered for account: $accountId, isPortfolioSeed: $isPortfolioSeed. Current state: ${_importState.value}"
        )
        if (_importState.value is Result.Loading) {
            logger.info("SYNC_FLOW", "Already importing, skipping.")
            return
        }

        // Update cooldown timer on any sync start
        lastAutoSyncTime = Clock.System.now()

        importJob?.cancel()
        importJob = viewModelScope.launch {
            _importState.value = Result.Loading
            _importProgress.value = 0f
            logger.info("SYNC_FLOW", "Starting transaction import job (isPortfolioSeed=$isPortfolioSeed)")
            try {
                transactionImporter.importHistory(accountId, isPortfolioSeed) { progress ->
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

    fun autoSyncTransactions(accountId: String? = null) {
        val now = Clock.System.now()
        val lastSync = lastAutoSyncTime

        if (lastSync == null || (now - lastSync) >= 2.minutes) {
            logger.info("SYNC_FLOW", "Auto-sync triggered. Last sync: $lastSync, Now: $now")
            lastAutoSyncTime = now
            importTransactions(accountId)
        } else {
            logger.info("SYNC_FLOW", "Auto-sync skipped. Cooldown active. Last sync: $lastSync")
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
    val categoryId: String?,
    val startDate: String?,
    val endDate: String?,
    val hasTransactionCost: Boolean?
)
