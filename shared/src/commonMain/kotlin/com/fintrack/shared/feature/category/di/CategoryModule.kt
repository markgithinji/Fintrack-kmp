package com.fintrack.shared.feature.category.di

import com.fintrack.shared.feature.category.data.CategoryApi
import com.fintrack.shared.feature.category.data.CategoryRepositoryImpl
import com.fintrack.shared.feature.category.domain.repository.CategoryRepository
import com.fintrack.shared.feature.category.ui.CategoryManagementViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val categoryModule = module {
    single { CategoryApi(get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }

    viewModel {
        CategoryManagementViewModel(
            repository = get()
        )
    }
}
