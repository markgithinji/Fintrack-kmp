package com.fintrack.shared.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            SettingsItem(
                title = "Change Password",
                subtitle = "Update your account password",
                icon = Icons.Default.Lock,
                onClick = onNavigateToChangePassword
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            SettingsItem(
                title = "Biometric Lock",
                subtitle = "Use FaceID or Fingerprint to open the app",
                icon = Icons.Default.Fingerprint,
                onClick = { /* Will be implemented next */ }
            )
        }
    }
}
