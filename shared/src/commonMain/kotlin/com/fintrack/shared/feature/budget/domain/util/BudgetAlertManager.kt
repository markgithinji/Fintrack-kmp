package com.fintrack.shared.feature.budget.domain.util

import com.fintrack.shared.feature.budget.domain.usecase.CheckBudgetThresholdsUseCase
import com.fintrack.shared.feature.core.util.GlobalRefreshManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BudgetAlertManager(
    private val globalRefreshManager: GlobalRefreshManager,
    private val checkBudgetThresholdsUseCase: CheckBudgetThresholdsUseCase
) {
    private val scope = CoroutineScope(SupervisorJob())

    init {
        scope.launch {
            globalRefreshManager.refreshEvent.collectLatest {
                try {
                    checkBudgetThresholdsUseCase()
                } catch (e: Exception) {
                    // Log error
                }
            }
        }
    }
}
