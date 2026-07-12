package com.fintrack.shared.feature.category.di

import com.fintrack.shared.feature.category.data.CategoryApi
import com.fintrack.shared.feature.category.data.CategoryRepositoryImpl
import com.fintrack.shared.feature.category.data.LocalCategoryDataSource
import com.fintrack.shared.feature.category.domain.repository.CategoryRepository
import com.fintrack.shared.feature.category.domain.usecase.AddCategoryUseCase
import com.fintrack.shared.feature.category.domain.usecase.DeleteCategoryUseCase
import com.fintrack.shared.feature.category.domain.usecase.SyncCategoriesUseCase
import com.fintrack.shared.feature.category.ui.CategoryManagementViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val categoryModule = module {
    single { CategoryApi(client = get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(categoryApi = get()) }
    single { LocalCategoryDataSource() }
    
    single { SyncCategoriesUseCase(repository = get(), localDataSource = get()) }
    single { AddCategoryUseCase(repository = get(), localDataSource = get()) }
    single { DeleteCategoryUseCase(repository = get(), localDataSource = get()) }

    viewModel { 
        CategoryManagementViewModel(
            localDataSource = get(),
            syncCategoriesUseCase = get(),
            addCategoryUseCase = get(),
            deleteCategoryUseCase = get()
        ) 
    }
}
