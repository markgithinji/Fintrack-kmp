package com.fintrack.shared.feature.category.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.category.domain.model.Category
import com.fintrack.shared.feature.category.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryManagementState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class CategoryManagementViewModel(
    private val repository: CategoryRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    val state: StateFlow<CategoryManagementState> = combine(
        repository.getCategories(),
        _isLoading,
        _error
    ) { categories, loading, error ->
        CategoryManagementState(categories, loading, error)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CategoryManagementState(categories = Category.allCategories)
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.refreshCategories()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addCategory(name: String, isExpense: Boolean, iconName: String? = null) {
        if (name.isBlank()) {
            _error.value = "Category name cannot be empty"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.addCategory(name, isExpense, iconName)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteCategory(id)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
