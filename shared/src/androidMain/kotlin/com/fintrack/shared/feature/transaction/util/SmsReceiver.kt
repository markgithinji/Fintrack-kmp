package com.fintrack.shared.feature.transaction.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.ExperimentalTime

class SmsReceiver : BroadcastReceiver(), KoinComponent {
    private val accountRepository: AccountRepository by inject()
    private val settingsDataSource: SettingsDataSource by inject()
    private val notificationService: NotificationService by inject()
    private val logger = KMPLogger()

    @OptIn(ExperimentalTime::class)
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION || context == null) return

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
                        // Show notification immediately so the user knows we caught it
                        notificationService.showTransactionNotification(transaction)

                        logger.info("SmsReceiver", "Scheduling sync for M-Pesa transaction: ${transaction.externalId}")
                        
                        val workData = workDataOf(
                            TransactionSyncWorker.KEY_TRANSACTION_JSON to Json.encodeToString(transaction)
                        )

                        val constraints = Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()

                        val syncRequest = OneTimeWorkRequestBuilder<TransactionSyncWorker>()
                            .setConstraints(constraints)
                            .setInputData(workData)
                            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                            .build()

                        WorkManager.getInstance(context).enqueue(syncRequest)
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
