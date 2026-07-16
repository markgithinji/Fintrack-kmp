package com.fintrack.shared.feature.category.data

import com.fintrack.shared.feature.category.data.model.toDomain
import com.fintrack.shared.feature.category.domain.model.Category
import com.fintrack.shared.feature.category.domain.model.CategoryRule
import com.fintrack.shared.feature.category.domain.repository.CategoryRepository
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.util.safeApiCall

class CategoryRepositoryImpl(
    private val categoryApi: CategoryApi
) : CategoryRepository {

    override suspend fun getCategories(): Result<List<Category>> = safeApiCall {
        val dtos = categoryApi.getCategories()
        dtos.map { it.toDomain() }
    }

    override suspend fun getCategoryRules(): Result<List<CategoryRule>> = safeApiCall {
        categoryApi.getCategoryRules()
    }

    override suspend fun addCategory(name: String, isExpense: Boolean, iconName: String?): Result<Category> = safeApiCall {
        val dto = categoryApi.addCategory(name, isExpense, iconName)
        dto.toDomain()
    }

    override suspend fun deleteCategory(id: String): Result<Unit> = safeApiCall {
        categoryApi.deleteCategory(id)
    }
}
