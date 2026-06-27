package com.fintrack.shared.feature.account.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.ui.CommonErrorState
import com.fintrack.shared.feature.core.data.domain.ApiException
import com.fintrack.shared.feature.core.data.domain.getUserFriendlyMessage
import com.fintrack.shared.feature.settings.ui.toCurrencyString
import com.fintrack.shared.feature.transaction.ui.home.AccountIcon
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    viewModel: AccountsViewModel = koinViewModel()
) {
    val accountsState by viewModel.accounts.collectAsStateWithLifecycle()
    val deleteResult by viewModel.deleteResult.collectAsStateWithLifecycle()
    val saveResult by viewModel.saveResult.collectAsStateWithLifecycle()
    
    var showAccountDialog by remember { mutableStateOf<Account?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val isOperating = deleteResult is Result.Loading || saveResult is Result.Loading

    LaunchedEffect(deleteResult) {
        when (val result = deleteResult) {
            is Result.Success -> {
                snackbarHostState.showSnackbar("Account deleted successfully")
                viewModel.clearResults()
            }
            is Result.Error -> {
                val message = (result.exception as? ApiException)?.getUserFriendlyMessage()
                    ?: result.exception.message ?: "Error deleting account"
                snackbarHostState.showSnackbar(message)
                viewModel.clearResults()
            }
            else -> Unit
        }
    }

    LaunchedEffect(saveResult) {
        when (val result = saveResult) {
            is Result.Success -> {
                snackbarHostState.showSnackbar(if (isEditing) "Account updated" else "Account added")
                viewModel.clearResults()
                showAccountDialog = null
            }
            is Result.Error -> {
                val message = (result.exception as? ApiException)?.getUserFriendlyMessage()
                    ?: result.exception.message ?: "Error saving account"
                snackbarHostState.showSnackbar(message)
                viewModel.clearResults()
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!isOperating) {
                FloatingActionButton(
                    onClick = { 
                        showAccountDialog = Account(id = "", name = "")
                        isEditing = false
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Account")
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                if (isOperating) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                when (val state = accountsState) {
                    is Result.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is Result.Success -> {
                        AccountList(
                            accounts = state.data,
                            onDeleteAccount = { viewModel.removeAccount(it.id) },
                            onEditAccount = { 
                                if (!isOperating) {
                                    showAccountDialog = it
                                    isEditing = true
                                }
                            }
                        )
                    }
                    is Result.Error -> {
                        CommonErrorState(
                            modifier = Modifier.fillMaxSize(),
                            title = "Failed to load accounts",
                            error = state.exception,
                            onRetry = { viewModel.reloadAccounts() }
                        )
                    }
                }
            }
        }
    }

    showAccountDialog?.let { account ->
        AccountDialog(
            account = account,
            isEditing = isEditing,
            isLoading = saveResult is Result.Loading,
            onDismiss = { if (!isOperating) showAccountDialog = null },
            onConfirm = { name ->
                viewModel.saveAccount(account.copy(name = name))
            }
        )
    }
}

@Composable
fun AccountList(
    accounts: List<Account>,
    onDeleteAccount: (Account) -> Unit,
    onEditAccount: (Account) -> Unit
) {
    if (accounts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No accounts added yet.")
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            items(accounts) { account ->
                AccountItem(
                    account = account,
                    onDelete = { onDeleteAccount(account) },
                    onEdit = { onEditAccount(account) }
                )
            }
        }
    }
}

@Composable
fun AccountItem(
    account: Account,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val accountIcon = AccountIcon.fromAccountName(account.name)
    
    Surface(
        onClick = onEdit,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accountIcon.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = accountIcon.icon,
                    contentDescription = null,
                    tint = accountIcon.color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = (account.balance ?: 0.0).toCurrencyString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (account.isDefault) {
                Text(
                    text = "Default",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(end = 4.dp)
                )
            } else {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Account",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDialog(
    account: Account,
    isEditing: Boolean,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var accountName by remember { mutableStateOf(account.name) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(if (isEditing) "Edit Account" else "Add New Account") },
        text = {
            OutlinedTextField(
                value = accountName,
                onValueChange = { accountName = it },
                label = { Text("Account Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )
        },
        confirmButton = {
            Button(
                onClick = { if (accountName.isNotBlank()) onConfirm(accountName) },
                enabled = accountName.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isEditing) "Save" else "Add")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}
