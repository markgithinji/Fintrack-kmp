package com.fintrack.shared.feature.navigation

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AppTopBar(
    appBarState: AppBarState,
    onUpdateAppBarState: (AppBarState) -> Unit
) {
    TopBar(
        title = appBarState.title,
        showBackButton = appBarState.showBackButton,
        onBack = appBarState.onBack
    )
}

@Composable
fun AddTransactionFAB(
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(60.dp).offset(y = 60.dp),
        containerColor = Color.Black,
        contentColor = Color.White,
        shape = CircleShape
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add Transaction")
    }
}