package com.fintrack.shared.feature.account.domain.model

data class Account(
    val id: String,
    val name: String,
    val balance: Double? = null,
    val income: Double? = null,
    val expense: Double? = null
)