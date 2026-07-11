package com.fintrack.shared.feature.transaction.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fintrack.shared.feature.core.util.GlobalRefreshManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RefreshWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val refreshManager: GlobalRefreshManager by inject()

    override suspend fun doWork(): Result {
        refreshManager.triggerRefresh()
        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "global_refresh_work"
    }
}
