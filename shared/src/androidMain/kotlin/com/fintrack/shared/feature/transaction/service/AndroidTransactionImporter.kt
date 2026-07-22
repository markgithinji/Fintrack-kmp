package com.fintrack.shared.feature.transaction.service

import android.content.Context
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.category.domain.repository.CategoryRepository
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.transaction.domain.service.TransactionImporter

class AndroidTransactionImporter(
    private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) : TransactionImporter {
    override suspend fun importHistory(
        targetAccountId: String?,
        isPortfolioSeed: Boolean,
        onProgress: (Float) -> Unit
    ) {
        val accountsResult = accountRepository.getAccounts()
        if (accountsResult is Result.Error && !isPortfolioSeed) {
            throw accountsResult.exception
        }

        val accounts = (accountsResult as? Result.Success)?.data ?: emptyList()
        
        val mpesaImporter = MpesaImporter(context, transactionRepository, accountRepository, categoryRepository)
        val equityImporter = EquityImporter(context, transactionRepository, accountRepository, categoryRepository)

        if (targetAccountId != null) {
            val targetAccount = accounts.find { it.id == targetAccountId }
            val hasMpesa = targetAccount?.linkedSources?.contains("mpesa") == true
            val hasEquity = targetAccount?.linkedSources?.contains("equity") == true
            
            when {
                isPortfolioSeed -> {
                    mpesaImporter.importHistory(targetAccountId, isPortfolioSeed, onProgress)
                }
                hasMpesa && hasEquity -> {
                    mpesaImporter.importHistory(targetAccountId, isPortfolioSeed) { onProgress(it * 0.5f) }
                    equityImporter.importHistory(targetAccountId, isPortfolioSeed) { onProgress(0.5f + (it * 0.5f)) }
                }
                hasMpesa -> {
                    mpesaImporter.importHistory(targetAccountId, isPortfolioSeed, onProgress)
                }
                hasEquity -> {
                    equityImporter.importHistory(targetAccountId, isPortfolioSeed, onProgress)
                }
                else -> {
                    onProgress(1.0f)
                }
            }
            return
        }

        // Global sync
        val mpesaAccounts = accounts.filter { it.linkedSources.contains("mpesa") }
        val equityAccounts = accounts.filter { it.linkedSources.contains("equity") }

        if (mpesaAccounts.isEmpty() && equityAccounts.isEmpty()) {
            if (isPortfolioSeed) {
                val fallback = accounts.firstOrNull()
                mpesaImporter.importHistory(fallback?.id, true, onProgress)
            } else {
                onProgress(1.0f)
            }
            return
        }

        // Perform sync for each account
        val totalSteps = mpesaAccounts.size + equityAccounts.size
        var currentStep = 0

        mpesaAccounts.forEach { account ->
            mpesaImporter.importHistory(account.id, isPortfolioSeed) { 
                onProgress((currentStep + it) / totalSteps)
            }
            currentStep++
        }

        equityAccounts.forEach { account ->
            equityImporter.importHistory(account.id, isPortfolioSeed) {
                onProgress((currentStep + it) / totalSteps)
            }
            currentStep++
        }
    }
}
