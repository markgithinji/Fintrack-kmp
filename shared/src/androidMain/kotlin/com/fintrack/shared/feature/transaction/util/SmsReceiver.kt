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

    @OptIn(ExperimentalTime::class)
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val fullMessage = messages.joinToString("") { it.displayMessageBody }
        val sender = messages.firstOrNull()?.displayOriginatingAddress

        if (sender?.equals("MPESA", ignoreCase = true) == true) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val isEnabled = settingsDataSource.isMpesaListenerEnabled.first()
                    if (!isEnabled) return@launch

                    val mpesaSimSlot = settingsDataSource.mpesaSimSlot.first()
                    
                    val accountsResult = accountRepository.getAccounts()
                    val accounts = (accountsResult as? Result.Success)?.data ?: emptyList()
                    val mpesaAccountId = accounts.find { it.isMpesa }?.id 
                        ?: accounts.find { it.name.lowercase() == "mpesa" }?.id 
                        ?: "mpesa"
                    
                    val transaction = MpesaParser.parse(fullMessage, mpesaAccountId)
                    if (transaction != null) {
                        val result = transactionRepository.addTransaction(transaction)
                        if (result is Result.Success) {
                            notificationService.showTransactionNotification(result.data)
                        }
                    }
                } catch (_: Exception) {
                    // Silently fail or log
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
