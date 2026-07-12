package com.fintrack.shared.feature.budget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.core.domain.ValidationResult
import com.fintrack.shared.feature.budget.domain.model.Budget
import com.fintrack.shared.feature.budget.domain.model.BudgetFormState
import com.fintrack.shared.feature.budget.domain.model.BudgetWithStatus
import com.fintrack.shared.feature.budget.domain.repository.BudgetRepository
import com.fintrack.shared.feature.budget.domain.usecase.BudgetValidationUseCase
import com.fintrack.shared.feature.core.domain.SaveState
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.category.data.LocalCategoryDataSource
import com.fintrack.shared.feature.category.domain.model.Category
import com.fintrack.shared.feature.category.domain.usecase.SyncCategoriesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val validationUseCase: BudgetValidationUseCase,
    private val localCategoryDataSource: LocalCategoryDataSource,
    private val syncCategoriesUseCase: SyncCategoriesUseCase
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _budgets = MutableStateFlow<Result<List<BudgetWithStatus>>>(Result.Loading)
    val budgets: StateFlow<Result<List<BudgetWithStatus>>> = _budgets.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState<Budget>>(SaveState.Idle)
    val saveState: StateFlow<SaveState<Budget>> = _saveState

    private val _deleteResult = MutableStateFlow<Result<Unit>?>(null)
    val deleteResult: StateFlow<Result<Unit>?> = _deleteResult

    private val _selectedBudget = MutableStateFlow<Result<BudgetWithStatus>>(Result.Loading)
    val selectedBudget: StateFlow<Result<BudgetWithStatus>> = _selectedBudget

    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError

    private val _formState = MutableStateFlow(BudgetFormState())
    val formState: StateFlow<BudgetFormState> = _formState

    private var hasInitializedNewBudget = false

    val validationState: StateFlow<ValidationResult> = _formState
        .map { formState ->
            validationUseCase(
                name = formState.name,
                amount = formState.amount,
                categories = formState.selectedCategories,
                startDate = formState.startDate,
                endDate = formState.endDate,
                selectedAccounts = formState.selectedAccounts
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ValidationResult.Error("") // Initial error state
        )

    init {
        reloadBudgets(showLoading = true)
        
        viewModelScope.launch {
            localCategoryDataSource.categories.collect { cats ->
                _categories.value = cats
                // Only set default category if we are creating a NEW budget 
                // and haven't selected any categories yet.
                if (_formState.value.id == null && 
                    _formState.value.selectedCategories.isEmpty() && 
                    cats.isNotEmpty() && 
                    !hasInitializedNewBudget) {
                    
                    val firstExpense = cats.firstOrNull { it.isExpense }
                    if (firstExpense != null) {
                        _formState.update { it.copy(selectedCategories = setOf(firstExpense)) }
                    }
                }
            }
        }
        refreshCategories()
    }

    fun initializeForm(budgetId: String?, availableAccounts: List<Account>) {
        if (budgetId == null) {
            if (!hasInitializedNewBudget) {
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                _formState.value = BudgetFormState(
                    name = "",
                    amount = "",
                    selectedCategories = emptySet(),
                    isExpense = true,
                    startDate = today,
                    endDate = today + DatePeriod(months = 1),
                    selectedAccounts = emptySet()
                )
                hasInitializedNewBudget = true
            }
        } else {
            val currentSelected = _selectedBudget.value
            if (currentSelected is Result.Success) {
                val budget = currentSelected.data.budget
                val budgetAccounts = availableAccounts.filter { it.id in budget.accountIds }.toSet()
                
                _formState.value = BudgetFormState(
                    id = budget.id,
                    name = budget.name,
                    amount = budget.limit.toLong().toString().let { if (it == "0") "" else it },
                    selectedCategories = budget.categories.map { budgetCat ->
                        _categories.value.find { it.name == budgetCat.name && it.isExpense == budgetCat.isExpense } ?: budgetCat
                    }.toSet(),
                    isExpense = budget.isExpense,
                    startDate = budget.startDate,
                    endDate = budget.endDate,
                    selectedAccounts = budgetAccounts
                )
            }
        }
    }

    fun refreshCategories() {
        viewModelScope.launch {
            syncCategoriesUseCase()
        }
    }

    fun toggleAccount(account: Account) {
        _formState.update { state ->
            val updatedAccounts = if (state.selectedAccounts.contains(account)) {
                state.selectedAccounts - account
            } else {
                state.selectedAccounts + account
            }
            state.copy(selectedAccounts = updatedAccounts)
        }
    }

    fun setName(name: String) {
        _formState.update { it.copy(name = name) }
    }

    fun setAmount(amount: String) {
        _formState.update { it.copy(amount = amount) }
    }

    fun setIsExpense(isExpense: Boolean) {
        _formState.update { current ->
            val updatedCategories = if (isExpense != current.isExpense) {
                // Reset categories when type changes
                val expenseCategories = _categories.value.filter { it.isExpense }
                val incomeCategories = _categories.value.filter { !it.isExpense }
                
                val firstCategory = if (isExpense && expenseCategories.isNotEmpty()) {
                    expenseCategories[0]
                } else if (!isExpense && incomeCategories.isNotEmpty()) {
                    incomeCategories[0]
                } else {
                    null
                }
                if (firstCategory != null) setOf(firstCategory) else emptySet()
            } else {
                current.selectedCategories
            }

            current.copy(
                isExpense = isExpense,
                selectedCategories = updatedCategories
            )
        }
    }

    fun setCategories(categories: Set<Category>) {
        _formState.update { it.copy(selectedCategories = categories) }
    }

    fun setPeriod(startDate: LocalDate?, endDate: LocalDate?) {
        _formState.update { it.copy(startDate = startDate, endDate = endDate) }
    }

    // Reload all budgets from the server
    fun reloadBudgets(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _budgets.value = Result.Loading
            }
            _budgets.value = budgetRepository.getBudgets(accountId = null)
        }
    }

    fun saveBudget() {
        val currentForm = _formState.value
        val validation = validationUseCase(
            name = currentForm.name,
            amount = currentForm.amount,
            categories = currentForm.selectedCategories,
            startDate = currentForm.startDate,
            endDate = currentForm.endDate,
            selectedAccounts = currentForm.selectedAccounts
        )

        // Handle validation result with when statement
        when (validation) {
            is ValidationResult.Error -> {
                _validationError.value = validation.message
                return
            }

            is ValidationResult.Success -> {
                _validationError.value = null
                // Continue with saving
                viewModelScope.launch {
                    _saveState.value = SaveState.Loading
                    val budget = Budget(
                        id = currentForm.id,
                        accountIds = currentForm.selectedAccounts.map { it.id },
                        name = currentForm.name,
                        categories = currentForm.selectedCategories.toList(),
                        limit = currentForm.amount.toDoubleOrNull() ?: 0.0,
                        isExpense = currentForm.isExpense,
                        startDate = currentForm.startDate!!,
                        endDate = currentForm.endDate!!
                    )

                    val result = budgetRepository.addOrUpdateBudget(budget)
                    _saveState.value = when (result) {
                        is Result.Success -> {
                            reloadBudgets(showLoading = false)
                            SaveState.Success(result.data)
                        }
                        is Result.Error -> SaveState.Error(result.exception)
                        is Result.Loading -> SaveState.Loading
                    }
                }
            }
        }
    }

    fun removeBudget(id: String) {
        viewModelScope.launch {
            _deleteResult.value = Result.Loading
            val result = budgetRepository.deleteBudget(id)
            _deleteResult.value = result
            if (result is Result.Success) {
                reloadBudgets(showLoading = false)
            }
        }
    }

    fun loadBudgetById(id: String) {
        val current = _selectedBudget.value
        if (current is Result.Success && current.data.budget.id == id) return

        viewModelScope.launch {
            _selectedBudget.value = Result.Loading
            _selectedBudget.value = budgetRepository.getBudgetById(id)
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    fun resetDeleteResult() {
        _deleteResult.value = null
    }

    fun clearValidationError() {
        _validationError.value = null
    }
}
