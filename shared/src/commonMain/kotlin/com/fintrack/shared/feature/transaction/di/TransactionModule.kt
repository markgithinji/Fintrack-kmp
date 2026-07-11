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
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val transactionModule = module {
    single { TransactionApi(get()) }
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }

    single { ValidateTransactionUseCase() }
    single { CreateTransactionUseCase() }
    single { ExportTransactionsUseCase(repository = get(), fileSaver = get()) }
    single { SyncRecurringBillsUseCase(transactionRepository = get(), settingsDataSource = get(), notificationService = get()) }

    single { createTransactionImporter(transactionRepository = get(), accountRepository = get()) }

    viewModel {
        TransactionViewModel(
            repo = get(),
            categoryRepo = get(),
            validateTransactionUseCase = get(),
            createTransactionUseCase = get(),
            transactionImporter = get()
        )
    }
}
