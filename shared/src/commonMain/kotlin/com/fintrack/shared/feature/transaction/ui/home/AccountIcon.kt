package com.fintrack.shared.feature.transaction.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Savings
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AccountIcon(val icon: ImageVector, val color: Color = Color.Companion.Unspecified) {
    object Bank : AccountIcon(Icons.Default.AccountBalance, Color.Companion.Blue)
    object Wallet : AccountIcon(Icons.Default.AccountCircle, Color.Companion.Green)
    object Cash : AccountIcon(Icons.Default.Money, Color.Companion.Yellow)
    object Savings : AccountIcon(Icons.Default.Savings, Color.Companion.Cyan) // new Savings account
    object Default : AccountIcon(Icons.Default.AccountBalance, Color.Companion.Gray)

    companion object {
        /** Map account names or types to icons */
        fun fromAccountName(name: String): AccountIcon = when (name.lowercase()) {
            "bank" -> Bank
            "wallet" -> Wallet
            "cash" -> Cash
            "savings" -> Savings
            else -> Default
        }
    }
}