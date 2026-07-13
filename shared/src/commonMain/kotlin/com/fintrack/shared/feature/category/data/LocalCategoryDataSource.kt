package com.fintrack.shared.feature.category.data

import com.fintrack.shared.feature.category.domain.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LocalCategoryDataSource {
    private val _categories = MutableStateFlow(Category.allCategories)
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    fun updateCategories(newCategories: List<Category>) {
        _categories.update { 
            (newCategories + Category.allCategories)
                .distinctBy { it.name.lowercase() to it.isExpense }
        }
    }

    fun addCategory(category: Category) {
        _categories.update { current ->
            (current + category).distinctBy { it.name.lowercase() to it.isExpense }
        }
    }

    fun removeCategory(id: String) {
        _categories.update { current -> current.filter { it.id != id } }
    }
}