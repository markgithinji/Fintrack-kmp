package com.fintrack.shared.feature.transaction.util

import android.content.Context
import android.provider.Telephony
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.core.logger.KMPLogger
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import com.fintrack.shared.feature.transaction.domain.util.TransactionImporter
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MpesaImporter(
    private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : TransactionImporter {
    private val logger = KMPLogger()

    @OptIn(ExperimentalTime::class)
    override suspend fun importHistory(): Unit = withContext(Dispatchers.IO) {
        val accountsResult = accountRepository.getAccounts()
        val accounts = (accountsResult as? Result.Success)?.data ?: emptyList()
        val accountId = accounts.find { it.isMpesa }?.id 
            ?: accounts.find { it.name.lowercase() == "mpesa" }?.id 
            ?: "mpesa"

        val cursor = context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.Inbox.BODY, Telephony.Sms.Inbox.ADDRESS),
            "${Telephony.Sms.Inbox.ADDRESS} = ?",
            arrayOf("MPESA"),
            Telephony.Sms.Inbox.DEFAULT_SORT_ORDER
        )

        cursor?.use {
            val bodyIndex = it.getColumnIndex(Telephony.Sms.Inbox.BODY)
            val transactions = mutableListOf<Transaction>()
            var latestBalance: Double? = null
            var loggedCount = 0

            while (it.moveToNext()) {
                val body = it.getString(bodyIndex)
                
                // Log the first 500 messages to help improve the parser
                if (loggedCount < 500) {
                    logger.debug("MPESA_PARSER_DEBUG", "Message ${loggedCount + 1}: $body")
                    loggedCount++
                }

                // Keep the first balance we find (latest message)
                if (latestBalance == null) {
                    latestBalance = MpesaParser.parseBalance(body)
                }

                val transaction = MpesaParser.parse(body, accountId)
                if (transaction != null) {
                    transactions.add(transaction)
                }
            }

            if (transactions.isNotEmpty()) {
                // We reverse to send oldest first
                val reversedTransactions = transactions.reversed()
                
                // Chunk the import to avoid "Internal Server Error" (often caused by large payloads)
                reversedTransactions.chunked(100).forEach { chunk ->
                    transactionRepository.importMpesaTransactions(chunk)
                }
            }

            // Correct account balance using an adjustment transaction if there's a discrepancy
            if (latestBalance != null) {
                val accountResult = accountRepository.getAccountById(accountId)
                if (accountResult is Result.Success) {
                    val account = accountResult.data
                    val currentAppBalance = account.balance ?: 0.0
                    val discrepancy = latestBalance - currentAppBalance
                    
                    if (kotlin.math.abs(discrepancy) > 0.01) {
                        // Silent update: Update the account balance directly instead of creating an adjustment transaction
                        accountRepository.addOrUpdateAccount(account.copy(balance = latestBalance))
                    }
                }
            }
        }
        Unit
    }
}
