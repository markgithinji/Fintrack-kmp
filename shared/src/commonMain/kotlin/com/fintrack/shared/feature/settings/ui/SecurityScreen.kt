package com.fintrack.shared.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.shared.feature.auth.ui.common.ErrorDialog
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    viewModel: SecurityViewModel = koinViewModel(),
) {
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val biometricError by viewModel.biometricError.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme

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
            
            SettingsToggleItem(
                title = "Biometric Lock",
                subtitle = "Use FaceID or Fingerprint to open the app",
                icon = Icons.Default.Fingerprint,
                checked = isBiometricEnabled,
                onCheckedChange = viewModel::toggleBiometric
            )
        }
    }

    if (biometricError != null) {
        ErrorDialog(
            title = "Biometric Error",
            errorMessage = biometricError!!,
            onDismiss = viewModel::clearBiometricError,
            onRetry = {
                viewModel.clearBiometricError()
                viewModel.toggleBiometric(enabled = true)
            },
            colorScheme = colorScheme
        )
    }
}


