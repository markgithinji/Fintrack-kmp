package com.fintrack.shared.feature.transaction.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Savings
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.fintrack.shared.feature.account.domain.Account

sealed class AccountIcon(val icon: ImageVector, val color: Color = Color.Unspecified) {
    object Bank : AccountIcon(Icons.Default.AccountBalance, Color.Blue)
    object Wallet : AccountIcon(Icons.Default.AccountCircle, Color.Green)
    object Cash : AccountIcon(Icons.Default.Money, Color.Yellow)
    object Savings : AccountIcon(Icons.Default.Savings, Color.Cyan) // new Savings account
    object Default : AccountIcon(Icons.Default.AccountBalance, Color.Gray)

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