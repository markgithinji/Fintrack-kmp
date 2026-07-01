package com.fintrack.shared.feature.account.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.fintrack.shared.feature.core.ui.MaterialToast
import com.fintrack.shared.feature.core.ui.CommonErrorState
import com.fintrack.shared.feature.core.data.domain.ApiException
import com.fintrack.shared.feature.core.data.domain.getUserFriendlyMessage
import com.fintrack.shared.feature.settings.ui.SettingsViewModel
import com.fintrack.shared.feature.settings.ui.toCurrencyString
import com.fintrack.shared.feature.transaction.ui.home.AccountIcon
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    viewModel: AccountsViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel()
) {
    val accountsState by viewModel.accounts.collectAsStateWithLifecycle()
    val deleteResult by viewModel.deleteResult.collectAsStateWithLifecycle()
    val saveResult by viewModel.saveResult.collectAsStateWithLifecycle()
    
    var showAccountDialog by remember { mutableStateOf<Account?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    val isOperating = deleteResult is Result.Loading || saveResult is Result.Loading

    LaunchedEffect(deleteResult) {
        when (val result = deleteResult) {
            is Result.Success -> {
                toastMessage = "Account deleted successfully" to false
                viewModel.clearResults()
            }
            is Result.Error -> {
                val message = (result.exception as? ApiException)?.getUserFriendlyMessage()
                    ?: result.exception.message ?: "Error deleting account"
                toastMessage = message to true
                viewModel.clearResults()
            }
            else -> Unit
        }
    }

    LaunchedEffect(saveResult) {
        when (val result = saveResult) {
            is Result.Success -> {
                toastMessage = (if (isEditing) "Account updated" else "Account added") to false
                viewModel.clearResults()
                showAccountDialog = null
            }
            is Result.Error -> {
                val message = (result.exception as? ApiException)?.getUserFriendlyMessage()
                    ?: result.exception.message ?: "Error saving account"
                toastMessage = message to true
                viewModel.clearResults()
            }
            else -> Unit
        }
    }

    Scaffold(
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
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                if (isOperating) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = innerPadding.calculateTopPadding() + paddingValues.calculateTopPadding())
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
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
                            topPadding = innerPadding.calculateTopPadding() + paddingValues.calculateTopPadding() - 4.dp,
                            bottomPadding = innerPadding.calculateBottomPadding() + paddingValues.calculateBottomPadding(),
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

            toastMessage?.let { (message, isError) ->
                MaterialToast(
                    message = message,
                    isError = isError,
                    onDismiss = { toastMessage = null },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 84.dp)
                )
            }
        }
    }

    showAccountDialog?.let { account ->
        AccountDialog(
            account = account,
            isEditing = isEditing,
            isLoading = saveResult is Result.Loading,
            isMpesaLinked = account.isMpesa,
            onDismiss = { if (!isOperating) showAccountDialog = null },
            onConfirm = { name, isMpesa ->
                viewModel.saveAccount(account.copy(name = name, isMpesa = isMpesa))
            }
        )
    }
}

@Composable
fun AccountList(
    accounts: List<Account>,
    topPadding: androidx.compose.ui.unit.Dp = 0.dp,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onDeleteAccount: (Account) -> Unit,
    onEditAccount: (Account) -> Unit
) {
    if (accounts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(top = topPadding), contentAlignment = Alignment.Center) {
            Text("No accounts added yet.")
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, 
                end = 16.dp, 
                top = topPadding, 
                bottom = bottomPadding + 80.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
    val accountIcon = if (account.isMpesa) AccountIcon.Mpesa else AccountIcon.fromAccountName(account.name)
    
    Surface(
        onClick = onEdit,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accountIcon.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = accountIcon.icon,
                        contentDescription = null,
                        tint = accountIcon.color,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (!account.isDefault) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Account",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Default",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = account.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            
            Text(
                text = (account.balance ?: 0.0).toCurrencyString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDialog(
    account: Account,
    isEditing: Boolean,
    isLoading: Boolean,
    isMpesaLinked: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, Boolean) -> Unit
) {
    var accountName by remember { mutableStateOf(account.name) }
    var isMpesa by remember { mutableStateOf(isMpesaLinked) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(if (isEditing) "Edit Account" else "Add New Account") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text("Account Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isMpesa,
                        onCheckedChange = { isMpesa = it },
                        enabled = !isLoading
                    )
                    Text(
                        text = "Link to M-Pesa SMS Import",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (accountName.isNotBlank()) onConfirm(accountName, isMpesa) },
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
