package com.fintrack.shared.feature.category.domain.usecase

import com.fintrack.shared.feature.category.domain.model.Category
import com.fintrack.shared.feature.category.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class GetCategoriesUseCase(private val repository: CategoryRepository) {
    operator fun invoke(): Flow<List<Category>> = repository.getCategories()
    suspend fun refresh() = repository.refreshCategories()
}

class AddCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(name: String, isExpense: Boolean, iconName: String? = null): Category {
        if (name.isBlank()) throw IllegalArgumentException("Category name cannot be empty")
        return repository.addCategory(name, isExpense, iconName)
    }
}

class DeleteCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(id: String) = repository.deleteCategory(id)
}
