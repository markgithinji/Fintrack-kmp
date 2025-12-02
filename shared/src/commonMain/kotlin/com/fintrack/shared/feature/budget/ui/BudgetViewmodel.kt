package com.fintrack.shared.feature.budget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.budget.domain.model.Budget
import com.fintrack.shared.feature.budget.domain.model.BudgetWithStatus
import com.fintrack.shared.feature.budget.domain.repository.BudgetRepository
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.transaction.domain.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class BudgetViewModel(
    private val repo: BudgetRepository
) : ViewModel() {

    private val _budgets = MutableStateFlow<Result<List<BudgetWithStatus>>>(Result.Loading)
    val budgets: StateFlow<Result<List<BudgetWithStatus>>> = _budgets

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    private val _deleteResult = MutableStateFlow<Result<Unit>?>(null)
    val deleteResult: StateFlow<Result<Unit>?> = _deleteResult

    private val _selectedBudget = MutableStateFlow<Result<BudgetWithStatus>>(Result.Loading)
    val selectedBudget: StateFlow<Result<BudgetWithStatus>> = _selectedBudget


    init {
        reloadBudgets()
    }

    fun reloadBudgets() {
        viewModelScope.launch {
            val result = repo.getBudgets()
            _budgets.value = result
        }
    }

    fun saveBudget(
        id: String? = null,
        name: String,
        categories: List<Category>,
        limit: Double,
        isExpense: Boolean,
        startDate: LocalDate,
        endDate: LocalDate,
        accountId: String
    ) {
        viewModelScope.launch {
            _saveState.value = SaveState.Loading
            val budget = Budget(
                id = id,
                accountId = accountId,
                name = name,
                categories = categories,
                limit = limit,
                isExpense = isExpense,
                startDate = startDate,
                endDate = endDate
            )

            val result = repo.addOrUpdateBudget(budget)
            _saveState.value = when (result) {
                is Result.Success -> SaveState.Success(result.data)
                is Result.Error -> SaveState.Error(result.exception)
                is Result.Loading -> SaveState.Loading
            }
        }
    }

    fun removeBudget(id: String, accountId: String? = null) {
        viewModelScope.launch {
            _deleteResult.value = repo.deleteBudget(id)
            reloadBudgets()
        }
    }

    fun loadBudgetById(id: String) {
        viewModelScope.launch {
            _selectedBudget.value = repo.getBudgetById(id)
        }
    }
}

sealed class SaveState {
    object Idle : SaveState()
    object Loading : SaveState()
    data class Success(val budget: Budget) : SaveState()
    data class Error(val exception: Throwable) : SaveState()
}