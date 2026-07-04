package com.fintrack.shared.feature.transaction.util

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.Data
import com.fintrack.shared.R
import com.fintrack.shared.feature.core.logger.KMPLogger
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.settings.domain.util.NotificationService
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TransactionSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val transactionRepository: TransactionRepository by inject()
    private val logger = KMPLogger()

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = NotificationCompat.Builder(context, "transaction_reminders")
            .setSmallIcon(R.drawable.ic_notification_sync)
            .setContentTitle("Syncing Transaction")
            .setContentText("Updating your records...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    override suspend fun doWork(): Result {
        val transactionJson = inputData.getString(KEY_TRANSACTION_JSON) ?: return Result.failure()
        
        return try {
            val transaction = Json.decodeFromString<Transaction>(transactionJson)
            logger.info("TransactionSyncWorker", "Syncing transaction: ${transaction.externalId}")
            
            val result = transactionRepository.addTransaction(transaction)
            
            if (result is com.fintrack.shared.feature.core.util.Result.Success) {
                logger.info("TransactionSyncWorker", "Successfully synced: ${transaction.externalId}")
                Result.success()
            } else {
                val error = (result as com.fintrack.shared.feature.core.util.Result.Error).exception
                logger.error("TransactionSyncWorker", "Failed to sync: ${error.message}")
                // If it's a network error, retry. If it's a 4xx error, maybe fail.
                Result.retry()
            }
        } catch (e: Exception) {
            logger.error("TransactionSyncWorker", "Error in worker: ${e.message}")
            Result.failure()
        }
    }

    companion object {
        const val KEY_TRANSACTION_JSON = "transaction_json"
        private const val NOTIFICATION_ID = 999
    }
}
