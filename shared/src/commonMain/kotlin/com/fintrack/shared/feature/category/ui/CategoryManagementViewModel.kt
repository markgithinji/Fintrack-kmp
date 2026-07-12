package com.fintrack.shared.feature.category.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.category.data.LocalCategoryDataSource
import com.fintrack.shared.feature.category.domain.model.Category
import com.fintrack.shared.feature.category.domain.usecase.AddCategoryUseCase
import com.fintrack.shared.feature.category.domain.usecase.DeleteCategoryUseCase
import com.fintrack.shared.feature.category.domain.usecase.SyncCategoriesUseCase
import com.fintrack.shared.feature.core.data.model.ApiException
import com.fintrack.shared.feature.core.data.model.getUserFriendlyMessage
import com.fintrack.shared.feature.core.util.Result
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
    private val localDataSource: LocalCategoryDataSource,
    private val syncCategoriesUseCase: SyncCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    val state: StateFlow<CategoryManagementState> = combine(
        localDataSource.categories,
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
            when (val result = syncCategoriesUseCase()) {
                is Result.Error -> {
                    val exception = result.exception
                    _error.value = (exception as? ApiException)?.getUserFriendlyMessage()
                        ?: exception.message ?: "Failed to refresh categories"
                }
                else -> { /* Success or Loading handled elsewhere */ }
            }
            _isLoading.value = false
        }
    }

    fun addCategory(name: String, isExpense: Boolean, iconName: String? = null) {
        if (name.isBlank()) {
            _error.value = "Category name cannot be empty"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = addCategoryUseCase(name, isExpense, iconName)) {
                is Result.Error -> {
                    val exception = result.exception
                    _error.value = (exception as? ApiException)?.getUserFriendlyMessage()
                        ?: exception.message ?: "Failed to add category"
                }
                is Result.Success -> { /* List will auto-update via repository flow */ }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = deleteCategoryUseCase(id)) {
                is Result.Error -> {
                    val exception = result.exception
                    _error.value = (exception as? ApiException)?.getUserFriendlyMessage()
                        ?: exception.message ?: "Failed to delete category"
                }
                is Result.Success -> { /* List will auto-update via repository flow */ }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
