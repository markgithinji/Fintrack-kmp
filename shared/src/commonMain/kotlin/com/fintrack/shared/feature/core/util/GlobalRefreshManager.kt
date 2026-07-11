package com.fintrack.shared.feature.core.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class GlobalRefreshManager {
    private val _refreshEvent = MutableSharedFlow<Unit>(replay = 0)
    val refreshEvent: Flow<Unit> = _refreshEvent.asSharedFlow()

    suspend fun triggerRefresh() {
        _refreshEvent.emit(Unit)
    }
}
