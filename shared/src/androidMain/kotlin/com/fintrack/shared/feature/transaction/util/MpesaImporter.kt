package com.fintrack.shared.feature.transaction.util

import android.content.Context
import android.provider.Telephony
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.core.logger.KMPLogger
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import com.fintrack.shared.feature.transaction.domain.util.TransactionImporter
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import kotlin.time.Instant
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
    override suspend fun importHistory(onProgress: (Float) -> Unit): Unit = withContext(Dispatchers.IO) {
        onProgress(0.05f)
        val accountsResult = accountRepository.getAccounts()
        val accounts = (accountsResult as? Result.Success)?.data ?: emptyList()
        val accountId = accounts.find { it.isMpesa }?.id 
            ?: accounts.find { it.name.lowercase() == "mpesa" }?.id 
            ?: "mpesa"

        val cursor = context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.Inbox.BODY, Telephony.Sms.Inbox.ADDRESS, Telephony.Sms.Inbox.DATE),
            "${Telephony.Sms.Inbox.ADDRESS} = ?",
            arrayOf("MPESA"),
            "${Telephony.Sms.Inbox.DATE} DESC"
        )

        cursor?.use {
            val bodyIndex = it.getColumnIndex(Telephony.Sms.Inbox.BODY)
            val dateIndex = it.getColumnIndex(Telephony.Sms.Inbox.DATE)
            val transactions = mutableListOf<Transaction>()
            var latestBalance: Double? = null
            var loggedCount = 0
            val totalMessages = it.count

            while (it.moveToNext()) {
                val body = it.getString(bodyIndex)
                val timestamp = it.getLong(dateIndex)
                val smsInstant = Instant.fromEpochMilliseconds(timestamp)
                
                // Log the most recent 9000 messages to help improve the parser
                if (loggedCount < 9000) {
                    logger.debug("MPESA_PARSER_DEBUG", "Message ${loggedCount + 1}: $body")
                }

                // Keep the first balance we find (most recent message)
                if (latestBalance == null) {
                    latestBalance = MpesaParser.parseBalance(body)
                }

                val transaction = MpesaParser.parse(body, accountId, smsInstant)
                if (transaction != null) {
                    transactions.add(transaction)
                } else {
                    if (body.contains("Confirmed", ignoreCase = true)) {
                        logger.debug("MPESA_PARSER_ERROR", "Failed to parse: $body")
                    }
                }
                
                loggedCount++
                if (loggedCount % 50 == 0) {
                    // Update progress up to 30% during scanning
                    onProgress(0.05f + (loggedCount.toFloat() / totalMessages) * 0.25f)
                }
            }

            if (transactions.isNotEmpty()) {
                onProgress(0.3f)
                // We reverse to send oldest first
                val reversedTransactions = transactions.reversed()
                val chunks = reversedTransactions.chunked(100)
                val totalChunks = chunks.size
                
                // Chunk the import to avoid "Internal Server Error" (often caused by large payloads)
                chunks.forEachIndexed { index, chunk ->
                    transactionRepository.importMpesaTransactions(chunk)
                    // Update progress from 30% to 90% during upload
                    onProgress(0.3f + ((index + 1).toFloat() / totalChunks) * 0.6f)
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
            onProgress(1.0f)
        }
        Unit
    }
}
