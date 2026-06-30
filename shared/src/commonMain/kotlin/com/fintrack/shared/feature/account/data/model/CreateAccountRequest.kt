package com.fintrack.shared.feature.account.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateAccountRequest(
    val name: String,
    val isMpesa: Boolean = false
)