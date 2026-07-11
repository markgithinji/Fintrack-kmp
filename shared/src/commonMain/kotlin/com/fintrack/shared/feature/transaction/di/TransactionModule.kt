package com.fintrack.shared.feature.transaction.di

import com.fintrack.shared.feature.transaction.data.TransactionApi
import com.fintrack.shared.feature.transaction.data.TransactionRepositoryImpl
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import com.fintrack.shared.feature.transaction.domain.usecase.CreateTransactionUseCase
import com.fintrack.shared.feature.transaction.domain.usecase.ExportTransactionsUseCase
import com.fintrack.shared.feature.transaction.domain.usecase.SyncRecurringBillsUseCase
import com.fintrack.shared.feature.transaction.domain.usecase.ValidateTransactionUseCase
import com.fintrack.shared.feature.transaction.domain.util.createTransactionImporter
import com.fintrack.shared.feature.transaction.ui.TransactionViewModel
import com.fintrack.shared.feature.category.domain.usecase.GetCategoriesUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val transactionModule = module {
    single { TransactionApi(get(), getProperty("baseUrl")) }
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }

    single { ValidateTransactionUseCase() }
    single { CreateTransactionUseCase() }
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
}
