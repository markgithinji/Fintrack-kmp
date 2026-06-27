package com.fintrack.shared.feature.transaction.domain.repository

import com.fintrack.shared.feature.transaction.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(): Flow<List<Category>>
    suspend fun refreshCategories()
    suspend fun addCategory(name: String, isExpense: Boolean, iconName: String? = null): Category
    suspend fun deleteCategory(id: String)
}
