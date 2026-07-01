package com.fintrack.shared.feature.transaction.util

import android.content.Context
import android.provider.Telephony
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import com.fintrack.shared.feature.transaction.domain.util.TransactionImporter
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MpesaImporter(
    private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : TransactionImporter {
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
            "${Telephony.Sms.Inbox.ADDRESS} LIKE ?",
            arrayOf("%MPESA%"),
            Telephony.Sms.Inbox.DEFAULT_SORT_ORDER
        )

        cursor?.use {
            val bodyIndex = it.getColumnIndex(Telephony.Sms.Inbox.BODY)
            val transactions = mutableListOf<Transaction>()
            var latestBalance: Double? = null

            while (it.moveToNext()) {
                val body = it.getString(bodyIndex)
                
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
                // We reverse to send oldest first, though importMpesaTransactions handles it on backend usually
                transactionRepository.importMpesaTransactions(transactions.reversed())
            }

            // Correct account balance using an adjustment transaction if there's a discrepancy
            if (latestBalance != null) {
                val accountResult = accountRepository.getAccountById(accountId)
                if (accountResult is Result.Success) {
                    val account = accountResult.data
                    val currentAppBalance = account.balance ?: 0.0
                    val discrepancy = latestBalance - currentAppBalance
                    
                    if (kotlin.math.abs(discrepancy) > 0.01) { // Use a small epsilon for double comparison
                        transactionRepository.addTransaction(
                            Transaction(
                                accountId = accountId,
                                isIncome = discrepancy > 0,
                                amount = kotlin.math.abs(discrepancy),
                                transactionCost = 0.0,
                                category = "General",
                                // Use 1s ago to avoid future date validation issues with backend clock skew
                                dateTime = kotlin.time.Clock.System.now().minus(1.seconds),
                                description = "M-Pesa Balance Adjustment",
                                balance = latestBalance // Critical: set the balance here!
                            )
                        )
                    }
                }
            }
        }
        Unit
    }
}
