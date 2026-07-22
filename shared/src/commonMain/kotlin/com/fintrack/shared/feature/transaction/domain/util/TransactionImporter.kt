package com.fintrack.shared.feature.transaction.domain.util

interface TransactionImporter {
    suspend fun importHistory(
        targetAccountId: String? = null,
        isPortfolioSeed: Boolean = false,
        onProgress: (Float) -> Unit = {}
    )
}
