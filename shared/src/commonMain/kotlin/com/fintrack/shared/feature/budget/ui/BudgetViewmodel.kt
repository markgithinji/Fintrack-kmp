package com.fintrack.shared.feature.budget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.budget.domain.model.Budget
import com.fintrack.shared.feature.budget.domain.model.BudgetFormState
import com.fintrack.shared.feature.budget.domain.model.BudgetWithStatus
import com.fintrack.shared.feature.budget.domain.model.ValidationResult
import com.fintrack.shared.feature.budget.domain.repository.BudgetRepository
import com.fintrack.shared.feature.budget.domain.usecase.BudgetValidationUseCase
import com.fintrack.shared.feature.core.domain.SaveState
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.transaction.domain.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class BudgetViewModel(
    private val repo: BudgetRepository,
    private val validationUseCase: BudgetValidationUseCase = BudgetValidationUseCase()
) : ViewModel() {

    private val _budgets = MutableStateFlow<Result<List<BudgetWithStatus>>>(Result.Loading)
    val budgets: StateFlow<Result<List<BudgetWithStatus>>> = _budgets

    private val _saveState = MutableStateFlow<SaveState<Budget>>(SaveState.Idle)
    val saveState: StateFlow<SaveState<Budget>> = _saveState

    private val _deleteResult = MutableStateFlow<Result<Unit>?>(null)
    val deleteResult: StateFlow<Result<Unit>?> = _deleteResult

    private val _selectedBudget = MutableStateFlow<Result<BudgetWithStatus>>(Result.Loading)
    val selectedBudget: StateFlow<Result<BudgetWithStatus>> = _selectedBudget

    private val _formState = MutableStateFlow(BudgetFormState())
    val formState: StateFlow<BudgetFormState> = _formState

    val validationState: StateFlow<ValidationResult> = _formState
        .map { formState ->
            validationUseCase(
                name = formState.name,
                amount = formState.amount,
                categories = formState.selectedCategories,
                startDate = formState.startDate,
                endDate = formState.endDate,
                selectedAccount = formState.selectedAccount
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ValidationResult(isValid = false)
        )

    init {
        reloadBudgets()
    }

    fun setAccount(account: Account?) {
        _formState.update { it.copy(selectedAccount = account) }
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
                val firstCategory = if (isExpense && Category.expenseCategories.isNotEmpty()) {
                    Category.expenseCategories[0]
                } else if (!isExpense && Category.incomeCategories.isNotEmpty()) {
                    Category.incomeCategories[0]
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

    fun setFormState(formState: BudgetFormState) {
        _formState.value = formState
    }

    fun reloadBudgets() {
        viewModelScope.launch {
            _budgets.value = Result.Loading
            _budgets.value = repo.getBudgets()
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
            selectedAccount = currentForm.selectedAccount
        )

        if (!validation.isValid) {
            _saveState.value = SaveState.Error(IllegalArgumentException(validation.errorMessage))
            return
        }

        viewModelScope.launch {
            _saveState.value = SaveState.Loading
            val budget = Budget(
                id = null,
                accountId = currentForm.selectedAccount!!.id,
                name = currentForm.name,
                categories = currentForm.selectedCategories.toList(),
                limit = currentForm.amount.toDoubleOrNull() ?: 0.0,
                isExpense = currentForm.isExpense,
                startDate = currentForm.startDate!!,
                endDate = currentForm.endDate!!
            )

            val result = repo.addOrUpdateBudget(budget)
            _saveState.value = when (result) {
                // Use generic SaveState.Success with Budget type
                is Result.Success -> SaveState.Success(result.data)
                is Result.Error -> SaveState.Error(result.exception)
                is Result.Loading -> SaveState.Loading
            }
        }
    }

    fun removeBudget(id: String) {
        viewModelScope.launch {
            _deleteResult.value = Result.Loading
            _deleteResult.value = repo.deleteBudget(id)
            reloadBudgets()
        }
    }

    fun loadBudgetById(id: String) {
        viewModelScope.launch {
            _selectedBudget.value = Result.Loading
            _selectedBudget.value = repo.getBudgetById(id)
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }
}