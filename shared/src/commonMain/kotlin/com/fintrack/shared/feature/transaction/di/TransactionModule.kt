package com.fintrack.shared.feature.transaction.di

import com.fintrack.shared.feature.transaction.data.CategoryApi
import com.fintrack.shared.feature.transaction.data.CategoryRepositoryImpl
import com.fintrack.shared.feature.transaction.data.TransactionApi
import com.fintrack.shared.feature.transaction.data.TransactionRepositoryImpl
import com.fintrack.shared.feature.transaction.domain.repository.CategoryRepository
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import com.fintrack.shared.feature.transaction.domain.usecase.AddCategoryUseCase
import com.fintrack.shared.feature.transaction.domain.usecase.CreateTransactionUseCase
import com.fintrack.shared.feature.transaction.domain.usecase.DeleteCategoryUseCase
import com.fintrack.shared.feature.transaction.domain.usecase.ExportTransactionsUseCase
import com.fintrack.shared.feature.transaction.domain.usecase.GetCategoriesUseCase
import com.fintrack.shared.feature.transaction.domain.usecase.SyncRecurringBillsUseCase
import com.fintrack.shared.feature.transaction.domain.usecase.ValidateTransactionUseCase
import com.fintrack.shared.feature.transaction.domain.util.createTransactionImporter
import com.fintrack.shared.feature.transaction.ui.TransactionViewModel
import com.fintrack.shared.feature.transaction.ui.category.CategoryManagementViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val transactionModule = module {
    single { TransactionApi(get(), getProperty("baseUrl")) }
    single { CategoryApi(get(), getProperty("baseUrl")) }
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }

    single { ValidateTransactionUseCase() }
    single { CreateTransactionUseCase() }
    single { GetCategoriesUseCase(get()) }
    single { AddCategoryUseCase(get()) }
    single { DeleteCategoryUseCase(get()) }
    single { ExportTransactionsUseCase(get(), get()) }
    single { SyncRecurringBillsUseCase(get(), get(), get()) }

    single { createTransactionImporter(get(), get()) }

    viewModel {
        TransactionViewModel(
            repo = get(),
            validateTransactionUseCase = get(),
            createTransactionUseCase = get(),
            getCategoriesUseCase = get(),
            transactionImporter = get()
        )
    }

    viewModel {
        CategoryManagementViewModel(
            getCategoriesUseCase = get(),
            addCategoryUseCase = get(),
            deleteCategoryUseCase = get()
        )
    }
}