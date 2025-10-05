package com.fintrack.shared.feature.account.domain.model

data class Account(
    val id: Int,
    val name: String,
    val balance: Double? = null,
    val income: Double? = null,
    val expense: Double? = null
)