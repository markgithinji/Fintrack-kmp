package com.fintrack.shared.feature.transaction.util

import android.content.Context
import android.provider.Telephony
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import com.fintrack.shared.feature.transaction.domain.util.TransactionImporter
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MpesaImporter(
    private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : TransactionImporter {
    override suspend fun importHistory(): Unit = withContext(Dispatchers.IO) {
        val accountsResult = accountRepository.getAccounts()
        val accounts = (accountsResult as? Result.Success)?.data ?: emptyList()
        val accountId = accounts.find { it.isMpesa }?.id 
            ?: accounts.find { it.name.lowercase() == "mpesa" }?.id 
            ?: "mpesa"

        // Fetch existing transactions to avoid duplicates
        val existingTransactionsResult = transactionRepository.getAllTransactions()
        val existingDescriptions = (existingTransactionsResult as? Result.Success)?.data
            ?.map { it.description }?.toSet() ?: emptySet()
        
        val cursor = context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.Inbox.BODY, Telephony.Sms.Inbox.ADDRESS),
            "${Telephony.Sms.Inbox.ADDRESS} LIKE ?",
            arrayOf("%MPESA%"),
            Telephony.Sms.Inbox.DEFAULT_SORT_ORDER
        )

        cursor?.use {
            val bodyIndex = it.getColumnIndex(Telephony.Sms.Inbox.BODY)
            val transactions = mutableListOf<Transaction>()
            while (it.moveToNext()) {
                val body = it.getString(bodyIndex)
                val transaction = MpesaParser.parse(body, accountId)
                if (transaction != null && !existingDescriptions.contains(transaction.description)) {
                    transactions.add(transaction)
                }
            }
            if (transactions.isNotEmpty()) {
                transactionRepository.addTransactions(transactions)
            }
        }
        Unit
    }
}
