package com.fintrack.shared.feature.settings.domain.datasource

import com.fintrack.shared.feature.settings.domain.model.Currency
import kotlinx.coroutines.flow.Flow

interface SettingsDataSource {
    val currency: Flow<Currency>
    suspend fun setCurrency(currency: Currency)
}