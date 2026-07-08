package com.fintrack.shared.feature.account.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class Account @OptIn(ExperimentalTime::class) constructor(
    val id: String,
    val name: String,
    val balance: Double? = null,
    val income: Double? = null,
    val expense: Double? = null,
    val isDefault: Boolean = false,
    val isMpesa: Boolean = false,
    val isEquity: Boolean = false,
    val createdAt: Instant? = null
)
