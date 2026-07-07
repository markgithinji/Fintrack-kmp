package com.fintrack.shared.feature.transaction.data

import com.fintrack.shared.feature.transaction.data.model.toDomain
import com.fintrack.shared.feature.transaction.domain.model.Category
import com.fintrack.shared.feature.transaction.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CategoryRepositoryImpl(
    private val api: CategoryApi
) : CategoryRepository {
    private val _categories = MutableStateFlow<List<Category>>(Category.allCategories)
    
    override fun getCategories(): StateFlow<List<Category>> = _categories.asStateFlow()

    override suspend fun refreshCategories() {
        try {
            val dtos = api.getCategories()
            val remoteCategories = dtos.map { it.toDomain() }
            
            // Deduplicate by name and type to prevent showing duplicate "Other Income" etc.
            // We keep all defaults first, then add remote categories that don't match any default name
            val defaultNames = Category.allCategories.map { it.name.lowercase() to it.isExpense }.toSet()
            val uniqueRemote = remoteCategories.filter { (it.name.lowercase() to it.isExpense) !in defaultNames }
            
            _categories.update { Category.allCategories + uniqueRemote }
        } catch (e: Exception) {
            // Log or handle error if needed
        }
    }

    override suspend fun addCategory(name: String, isExpense: Boolean, iconName: String?): Category {
        val dto = api.addCategory(name, isExpense, iconName)
        val category = dto.toDomain()
        _categories.update { current -> current + category }
        return category
    }

    override suspend fun deleteCategory(id: String) {
        api.deleteCategory(id)
        _categories.update { current -> current.filter { it.id != id } }
    }
}
