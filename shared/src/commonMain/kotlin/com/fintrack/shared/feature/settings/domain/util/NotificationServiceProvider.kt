package com.fintrack.shared.feature.settings.domain.util

import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource

expect fun createNotificationService(settingsDataSource: SettingsDataSource): NotificationService
