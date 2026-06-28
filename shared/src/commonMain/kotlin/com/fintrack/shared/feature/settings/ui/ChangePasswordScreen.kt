package com.fintrack.shared.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.shared.feature.auth.ui.common.ErrorDialog
import com.fintrack.shared.feature.auth.ui.common.FinanceTextField
import com.fintrack.shared.feature.core.domain.SaveState
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: SecurityViewModel = koinViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val changePasswordState by viewModel.changePasswordState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()

    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(changePasswordState) {
        if (changePasswordState is SaveState.Success) {
            delay(1000)
            onNavigateBack()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Password") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            FinanceTextField(
                value = formState.currentPassword,
                onValueChange = viewModel::updateCurrentPassword,
                label = "Current Password",
                leadingIcon = Icons.Default.Lock,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
                isPassword = true,
                passwordVisible = currentPasswordVisible,
                onPasswordToggle = { currentPasswordVisible = !currentPasswordVisible },
                colorScheme = colorScheme,
                isError = formState.currentPasswordError != null,
                errorMessage = formState.currentPasswordError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            FinanceTextField(
                value = formState.newPassword,
                onValueChange = viewModel::updateNewPassword,
                label = "New Password",
                leadingIcon = Icons.Default.Lock,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
                isPassword = true,
                passwordVisible = newPasswordVisible,
                onPasswordToggle = { newPasswordVisible = !newPasswordVisible },
                colorScheme = colorScheme,
                isError = formState.newPasswordError != null,
                errorMessage = formState.newPasswordError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            FinanceTextField(
                value = formState.confirmPassword,
                onValueChange = viewModel::updateConfirmPassword,
                label = "Confirm New Password",
                leadingIcon = Icons.Default.Lock,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                isPassword = true,
                passwordVisible = confirmPasswordVisible,
                onPasswordToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                colorScheme = colorScheme,
                isError = formState.confirmPasswordError != null,
                errorMessage = formState.confirmPasswordError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = viewModel::changePassword,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = changePasswordState !is SaveState.Loading && changePasswordState !is SaveState.Success,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (changePasswordState is SaveState.Success) colorScheme.secondary else colorScheme.primary,
                    contentColor = if (changePasswordState is SaveState.Success) colorScheme.onSecondary else colorScheme.onPrimary,
                    disabledContainerColor = if (changePasswordState is SaveState.Success) colorScheme.secondary else colorScheme.primary.copy(alpha = 0.5f),
                    disabledContentColor = if (changePasswordState is SaveState.Success) colorScheme.onSecondary else colorScheme.onPrimary.copy(alpha = 0.7f)
                )
            ) {
                when (changePasswordState) {
                    is SaveState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    }
                    is SaveState.Success -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Success",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                    else -> {
                        Text(
                            "Update Password",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

    if (changePasswordState is SaveState.Error) {
        ErrorDialog(
            title = "Update Failed",
            errorMessage = (changePasswordState as SaveState.Error).exception.message ?: "An unknown error occurred",
            onDismiss = viewModel::resetState,
            onRetry = viewModel::changePassword,
            colorScheme = colorScheme
        )
    }
}
