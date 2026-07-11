package com.fintrack.shared.feature.category.di

import com.fintrack.shared.feature.category.data.CategoryApi
import com.fintrack.shared.feature.category.data.CategoryRepositoryImpl
import com.fintrack.shared.feature.category.domain.repository.CategoryRepository
import com.fintrack.shared.feature.category.domain.usecase.AddCategoryUseCase
import com.fintrack.shared.feature.category.domain.usecase.DeleteCategoryUseCase
import com.fintrack.shared.feature.category.domain.usecase.GetCategoriesUseCase
import com.fintrack.shared.feature.category.ui.CategoryManagementViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val categoryModule = module {
    single { CategoryApi(get(), getProperty("baseUrl")) }
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }

    single { GetCategoriesUseCase(get()) }
    single { AddCategoryUseCase(get()) }
    single { DeleteCategoryUseCase(get()) }

    viewModel {
        CategoryManagementViewModel(
            getCategoriesUseCase = get(),
            addCategoryUseCase = get(),
            deleteCategoryUseCase = get()
        )
    }
}
