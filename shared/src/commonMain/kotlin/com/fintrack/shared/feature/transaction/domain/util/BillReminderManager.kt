package com.fintrack.shared.feature.transaction.domain.util

import com.fintrack.shared.feature.transaction.domain.usecase.SyncRecurringBillsUseCase
import com.fintrack.shared.feature.core.util.GlobalRefreshManager
import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class BillReminderManager(
    private val globalRefreshManager: GlobalRefreshManager,
    private val settingsDataSource: SettingsDataSource,
    private val syncRecurringBillsUseCase: SyncRecurringBillsUseCase
) {
    private val scope = CoroutineScope(SupervisorJob())

    init {
        // Sync whenever data changes (new transactions added etc)
        scope.launch {
            globalRefreshManager.refreshEvent.collectLatest {
                try {
                    syncRecurringBillsUseCase()
                } catch (e: Exception) {
                    // Log error
                }
            }
        }

        // Sync when settings change
        scope.launch {
            combine(
                settingsDataSource.isBillReminderEnabled,
                settingsDataSource.billReminderDaysBefore
            ) { enabled, days -> enabled to days }
                .distinctUntilChanged()
                .collectLatest { (enabled, _) ->
                    if (enabled) {
                        try {
                            syncRecurringBillsUseCase()
                        } catch (e: Exception) {
                            // Log error
                        }
                    }
                }
        }
        
        // Initial sync on app start
        scope.launch {
            try {
                syncRecurringBillsUseCase()
            } catch (e: Exception) {
                // Log error
            }
        }
    }
}
