package com.fintrack.shared.feature.transaction.di

import com.fintrack.shared.feature.transaction.data.TransactionApi
import com.fintrack.shared.feature.transaction.data.TransactionRepositoryImpl
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import com.fintrack.shared.feature.transaction.ui.TransactionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val transactionModule = module {
    factory { TransactionApi(get(), getProperty("baseUrl")) }
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    viewModel { TransactionViewModel(get()) }
}