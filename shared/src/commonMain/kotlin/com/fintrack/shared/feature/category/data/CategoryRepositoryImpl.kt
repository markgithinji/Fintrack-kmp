package com.fintrack.shared.feature.category.data

import com.fintrack.shared.feature.category.data.model.toDomain
import com.fintrack.shared.feature.category.domain.model.Category
import com.fintrack.shared.feature.category.domain.repository.CategoryRepository
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
            
            _categories.update { 
                (Category.allCategories + remoteCategories)
                    .distinctBy { it.name.lowercase() to it.isExpense }
            }
        } catch (e: Exception) {
            // Log or handle error if needed
        }
    }

    override suspend fun addCategory(name: String, isExpense: Boolean, iconName: String?): Category {
        val dto = api.addCategory(name, isExpense, iconName)
        val category = dto.toDomain()
        _categories.update { current -> 
            (current + category).distinctBy { it.name.lowercase() to it.isExpense }
        }
        return category
    }

    override suspend fun deleteCategory(id: String) {
        api.deleteCategory(id)
        _categories.update { current -> current.filter { it.id != id } }
    }
}
