package com.fintrack.shared.feature.transaction.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource
import com.fintrack.shared.feature.settings.domain.util.NotificationService
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import com.fintrack.shared.feature.core.logger.KMPLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.ExperimentalTime

class SmsReceiver : BroadcastReceiver(), KoinComponent {
    private val transactionRepository: TransactionRepository by inject()
    private val accountRepository: AccountRepository by inject()
    private val settingsDataSource: SettingsDataSource by inject()
    private val notificationService: NotificationService by inject()
    private val logger = KMPLogger()

    @OptIn(ExperimentalTime::class)
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val fullMessage = messages.joinToString("") { it.displayMessageBody }
        val sender = messages.firstOrNull()?.displayOriginatingAddress

        logger.debug("SmsReceiver", "Received SMS from: $sender")

        if (sender?.contains("MPESA", ignoreCase = true) == true || 
            sender?.contains("M-PESA", ignoreCase = true) == true) {
            
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val isEnabled = settingsDataSource.isMpesaListenerEnabled.first()
                    if (!isEnabled) {
                        logger.debug("SmsReceiver", "M-Pesa listener is disabled in settings")
                        return@launch
                    }

                    logger.debug("SmsReceiver", "Processing M-Pesa message: ${fullMessage.take(50)}...")
                    
                    val accountsResult = accountRepository.getAccounts()
                    val accounts = (accountsResult as? Result.Success)?.data ?: emptyList()
                    val mpesaAccountId = accounts.find { it.isMpesa }?.id 
                        ?: accounts.find { it.name.lowercase() == "mpesa" }?.id 
                        ?: "mpesa"
                    
                    val transaction = MpesaParser.parse(fullMessage, mpesaAccountId)
                    if (transaction != null) {
                        logger.info("SmsReceiver", "Parsed M-Pesa transaction: ${transaction.externalId}")
                        val result = transactionRepository.addTransaction(transaction)
                        if (result is Result.Success) {
                            logger.info("SmsReceiver", "Successfully added transaction: ${transaction.externalId}")
                            notificationService.showTransactionNotification(result.data)
                        } else if (result is Result.Error) {
                            logger.error("SmsReceiver", "Failed to add transaction: ${result.exception.message}")
                        }
                    } else {
                        logger.warning("SmsReceiver", "Failed to parse M-Pesa message: $fullMessage")
                    }
                } catch (e: Exception) {
                    logger.error("SmsReceiver", "Error processing SMS: ${e.message}")
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
