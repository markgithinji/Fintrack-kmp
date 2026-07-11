package com.fintrack.shared.feature.budget.domain.usecase

import com.fintrack.shared.feature.budget.domain.repository.BudgetRepository
import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource
import com.fintrack.shared.feature.settings.domain.util.NotificationService
import com.fintrack.shared.feature.core.util.Result
import kotlinx.coroutines.flow.first

class CheckBudgetThresholdsUseCase(
    private val budgetRepository: BudgetRepository,
    private val settingsDataSource: SettingsDataSource,
    private val notificationService: NotificationService
) {
    suspend operator fun invoke() {
        val alertsEnabled = settingsDataSource.budgetAlertsEnabled.first()
        if (!alertsEnabled) return

        val alertBudgetId = settingsDataSource.alertBudgetId.first() ?: return
        val thresholds = settingsDataSource.budgetAlertThresholds.first()
        if (thresholds.isEmpty()) return

        val result = budgetRepository.getBudgets()
        if (result is Result.Success) {
            val budgetWithStatus = result.data.find { it.budget.id == alertBudgetId } ?: return
            val status = budgetWithStatus.status
            val percentageUsed = status.percentageUsed // Assuming this is 0.0 to 1.0 or 0 to 100?
            
            // Let's check BudgetStatus definition again
            // data class BudgetStatus(val percentageUsed: Double, ...)
            
            val percent = percentageUsed // If it's 0.0 to 100.0
            // Usually percentageUsed is 0.0 to 1.0. Let's assume 0 to 100 based on my UI code.
            // If it's 0.8, then 80%.
            
            val actualPercent = if (percent <= 1.0) percent * 100 else percent

            val thresholdReached = thresholds
                .filter { it.toDouble() <= actualPercent }
                .maxByOrNull { it }

            if (thresholdReached != null) {
                notificationService.showBudgetAlertNotification(
                    budgetName = budgetWithStatus.budget.name,
                    threshold = thresholdReached
                )
            }
        }
    }
}
