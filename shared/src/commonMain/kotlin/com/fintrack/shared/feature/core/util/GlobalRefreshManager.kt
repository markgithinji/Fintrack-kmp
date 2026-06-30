package com.fintrack.shared.feature.core.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * A central manager to coordinate global data refresh events across the application.
 * This is useful for events like "Clear All User Data" where multiple independent
 * features need to reload their state.
 */
class GlobalRefreshManager {
    private val _refreshEvent = MutableSharedFlow<Unit>(replay = 0)
    
    /**
     * A flow that emits whenever a global refresh is requested.
     */
    val refreshEvent: SharedFlow<Unit> = _refreshEvent.asSharedFlow()

    /**
     * Triggers a global refresh event.
     */
    suspend fun triggerRefresh() {
        _refreshEvent.emit(Unit)
    }
}
