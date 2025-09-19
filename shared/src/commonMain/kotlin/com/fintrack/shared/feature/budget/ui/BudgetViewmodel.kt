package com.fintrack.shared.feature.budget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.budget.data.repository.BudgetRepository
import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.budget.domain.Budget
import com.fintrack.shared.feature.transaction.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class BudgetViewModel(
    private val repo: BudgetRepository = BudgetRepository()
) : ViewModel() {

    private val _budgets = MutableStateFlow<Result<List<Budget>>>(Result.Loading)
    val budgets: StateFlow<Result<List<Budget>>> = _budgets

    private val _saveResult = MutableStateFlow<Result<Budget>?>(null)
    val saveResult: StateFlow<Result<Budget>?> = _saveResult

    private val _deleteResult = MutableStateFlow<Result<Unit>?>(null)
    val deleteResult: StateFlow<Result<Unit>?> = _deleteResult

    private val _selectedBudget = MutableStateFlow<Result<Budget>?>(null)
    val selectedBudget: StateFlow<Result<Budget>?> = _selectedBudget

    init {
        reloadBudgets()
    }

    fun reloadBudgets() {
        viewModelScope.launch {
            _budgets.value = repo.getBudgets()
        }
    }

    fun saveBudget(
        id: Int? = null,
        name: String,
        categories: List<Category>,
        limit: Double,
        isExpense: Boolean,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        viewModelScope.launch {
            _saveResult.value = repo.addOrUpdateBudget(
                Budget(
                    id = id,
                    name = name,
                    categories = categories,
                    limit = limit,
                    isExpense = isExpense,
                    startDate = startDate,
                    endDate = endDate
                )
            )
            reloadBudgets()
        }
    }

    fun removeBudget(id: Int) {
        viewModelScope.launch {
            _deleteResult.value = repo.deleteBudget(id)
            reloadBudgets()
        }
    }

    fun loadBudgetById(id: Int) {
        viewModelScope.launch {
            // First try from local cache
            val current = _budgets.value
            if (current is Result.Success) {
                val found = current.data.firstOrNull { it.id == id }
                if (found != null) {
                    _selectedBudget.value = Result.Success(found)
                    return@launch
                }
            }

            // If not found locally, fetch from repo
            _selectedBudget.value = repo.getBudgetById(id)
        }
    }
}
