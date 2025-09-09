package com.fintrack.shared.feature.transaction.data

import com.fintrack.shared.feature.transaction.model.Transaction

class TransactionRepository(
    private val api: TransactionApi
) {
    suspend fun getTransactions(): List<Transaction> = api.getTransactions()

    suspend fun addTransaction(transaction: Transaction): Transaction =
        api.addTransaction(transaction)

    suspend fun getSummary(): Map<String, Any> = api.getSummary()
}