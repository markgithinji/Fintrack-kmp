package com.fintrack.shared.feature.core.domain.service

import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource

expect fun createNotificationService(settingsDataSource: SettingsDataSource): NotificationService
