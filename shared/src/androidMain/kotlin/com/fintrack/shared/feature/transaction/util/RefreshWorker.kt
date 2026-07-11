package com.fintrack.shared.feature.transaction.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RefreshWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val transactionRepository: TransactionRepository by inject()

    override suspend fun doWork(): Result {
        // Since GlobalRefreshManager is gone, we trigger the repository directly.
        // If we need a more global effect, we'd need a different mechanism for background-to-UI communication.
        transactionRepository.triggerRefresh()
        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "global_refresh_work"
    }
}
