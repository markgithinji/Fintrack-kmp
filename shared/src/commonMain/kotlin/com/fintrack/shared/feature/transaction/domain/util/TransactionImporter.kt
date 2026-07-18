package com.fintrack.shared.feature.transaction.domain.util

import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.category.domain.repository.CategoryRepository
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository

import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource

interface TransactionImporter {
    suspend fun importHistory(
        targetAccountId: String? = null,
        isPortfolioSeed: Boolean = false,
        onProgress: (Float) -> Unit = {}
    )
}

expect fun createTransactionImporter(
    transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    settingsDataSource: SettingsDataSource
): TransactionImporter
