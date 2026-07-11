package com.fintrack.shared.feature.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.shared.feature.core.data.model.ApiException
import com.fintrack.shared.feature.core.data.model.getUserFriendlyMessage
import com.fintrack.shared.feature.core.domain.SaveState
import com.fintrack.shared.feature.core.ui.MaterialToast
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val editState by viewModel.editState.collectAsStateWithLifecycle()

    LaunchedEffect(editState) {
        if (editState is SaveState.Success) {
            onNavigateBack()
            viewModel.resetEditState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = formState.name,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = formState.nameError != null,
                    supportingText = formState.nameError?.let { { Text(it) } }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = formState.email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = formState.emailError != null,
                    supportingText = formState.emailError?.let { { Text(it) } }
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.updateProfile() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = editState !is SaveState.Loading
                ) {
                    if (editState is SaveState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text("Save Changes")
                }
            }

            if (editState is SaveState.Error) {
                val error = (editState as SaveState.Error).exception
                val message = (error as? ApiException)?.getUserFriendlyMessage() ?: error.message ?: "An error occurred"
                MaterialToast(
                    message = message,
                    isError = true,
                    onDismiss = { viewModel.resetEditState() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                )
            }
        }
    }
}
