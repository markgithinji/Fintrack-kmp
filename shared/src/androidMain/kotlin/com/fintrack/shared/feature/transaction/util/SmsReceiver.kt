package com.fintrack.shared.feature.transaction.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.fintrack.shared.feature.account.domain.model.AccountType
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.category.domain.repository.CategoryRepository
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
import java.util.concurrent.TimeUnit

class SmsReceiver : BroadcastReceiver(), KoinComponent {
    private val accountRepository: AccountRepository by inject()
    private val categoryRepository: CategoryRepository by inject()
    private val settingsDataSource: SettingsDataSource by inject()
    private val notificationService: NotificationService by inject()
    private val logger = KMPLogger()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION || context == null) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val fullMessage = messages.joinToString("") { it.displayMessageBody }
        val sender = messages.firstOrNull()?.displayOriginatingAddress

        logger.debug("SmsReceiver", "Received SMS from: $sender")

        val isMpesa = sender?.contains("MPESA", ignoreCase = true) == true || 
                     sender?.contains("M-PESA", ignoreCase = true) == true
        
        val isEquity = sender?.contains("EquitBank", ignoreCase = true) == true || 
                      sender?.contains("EquityBank", ignoreCase = true) == true

        if (isMpesa || isEquity) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val isEnabled = if (isMpesa) {
                        settingsDataSource.isMpesaListenerEnabled.first()
                    } else {
                        settingsDataSource.isEquityListenerEnabled.first()
                    }

                    if (!isEnabled) {
                        logger.debug("SmsReceiver", "${if (isMpesa) "M-Pesa" else "Equity"} listener is disabled in settings")
                        return@launch
                    }

                    logger.debug("SmsReceiver", "Processing ${if (isMpesa) "M-Pesa" else "Equity"} message: ${fullMessage.take(50)}...")
                    
                    val accountsResult = accountRepository.getAccounts()
                    val accounts = (accountsResult as? Result.Success)?.data ?: emptyList()
                    
                    val accountId = if (isMpesa) {
                        accounts.find { it.type == AccountType.MPESA }?.id
                            ?: accounts.find { it.name.lowercase() == "mpesa" }?.id 
                            ?: "mpesa"
                    } else {
                        accounts.find { it.type == AccountType.EQUITY }?.id
                            ?: accounts.find { it.name.lowercase().contains("equity") }?.id 
                            ?: "equity"
                    }
                    
                    var transaction = if (isMpesa) {
                        MpesaParser.parse(fullMessage, accountId)
                    } else {
                        EquityParser.parse(fullMessage, accountId)
                    }

                    if (transaction != null) {
                        // Map category name to ID
                        val categoriesResult = categoryRepository.getCategories()
                        val categories = (categoriesResult as? Result.Success)?.data ?: emptyList()
                        val categoryName = transaction.category
                        val isExpense = !transaction.isIncome
                        
                        val categoryId = categories.find { 
                            it.name.equals(categoryName, ignoreCase = true) && it.isExpense == isExpense 
                        }?.id ?: categories.find { 
                            it.name.equals("Transfer", ignoreCase = true) && it.isExpense == isExpense 
                        }?.id ?: categories.firstOrNull { it.isExpense == isExpense }?.id
                        
                        if (categoryId != null) {
                            transaction = transaction.copy(categoryId = categoryId)
                        }

                        // Show notification immediately so the user knows we caught it
                        notificationService.showTransactionNotification(transaction)

                        logger.info("SmsReceiver", "Scheduling sync for ${if (isMpesa) "M-Pesa" else "Equity"} transaction: ${transaction.externalId}")
                        
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
                        logger.warning("SmsReceiver", "Failed to parse ${if (isMpesa) "M-Pesa" else "Equity"} message: $fullMessage")
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
