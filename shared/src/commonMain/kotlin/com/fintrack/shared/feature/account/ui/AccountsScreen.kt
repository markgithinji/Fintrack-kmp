package com.fintrack.shared.feature.account.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
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
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
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

    Scaffold { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = accountsState) {
                    is Result.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is Result.Success -> {
                        val defaultAccountId by settingsViewModel.defaultAccountId.collectAsStateWithLifecycle()
                        val effectiveDefaultAccountId = defaultAccountId ?: state.data.find { it.isMpesa }?.id
                        
                        AccountList(
                            accounts = state.data,
                            defaultAccountId = effectiveDefaultAccountId,
                            topPadding = innerPadding.calculateTopPadding() + paddingValues.calculateTopPadding() - 4.dp,
                            bottomPadding = innerPadding.calculateBottomPadding() + paddingValues.calculateBottomPadding(),
                            onEditAccount = { 
                                if (!isOperating) {
                                    showAccountDialog = it
                                    isEditing = true
                                }
                            },
                            onAddAccount = {
                                if (!isOperating) {
                                    showAccountDialog = Account(id = "", name = "")
                                    isEditing = false
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
        val defaultAccountId by settingsViewModel.defaultAccountId.collectAsStateWithLifecycle()
        val isOtherAccountDefault = defaultAccountId != null && defaultAccountId != account.id
        
        AccountDialog(
            account = account,
            isEditing = isEditing,
            isLoading = saveResult is Result.Loading || deleteResult is Result.Loading,
            isMpesaLinked = account.isMpesa,
            isDefaultSelection = account.id == defaultAccountId,
            isOtherAccountDefault = isOtherAccountDefault,
            onDismiss = { if (!isOperating) showAccountDialog = null },
            onDelete = { viewModel.removeAccount(account.id) },
            onConfirm = { name, isMpesa, isDefault ->
                viewModel.saveAccount(account.copy(name = name, isMpesa = isMpesa))
                if (isDefault) {
                    settingsViewModel.setDefaultAccountId(account.id)
                } else if (account.id == defaultAccountId) {
                    settingsViewModel.setDefaultAccountId(null)
                }
            }
        )
    }
}

@Composable
fun AccountList(
    accounts: List<Account>,
    defaultAccountId: String?,
    topPadding: androidx.compose.ui.unit.Dp = 0.dp,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onEditAccount: (Account) -> Unit,
    onAddAccount: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = topPadding,
            bottom = bottomPadding + 16.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = accounts,
            key = { it.id }
        ) { account ->
            AccountItem(
                account = account,
                isStartAccount = account.id == defaultAccountId,
                onEdit = { onEditAccount(account) }
            )
        }
        item(key = "add_account") {
            AddAccountItem(onClick = onAddAccount)
        }
    }
}

@Composable
fun AddAccountItem(
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
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
                .padding(10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Add Account",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = " ",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun AccountItem(
    account: Account,
    isStartAccount: Boolean,
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
                .padding(10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accountIcon.color.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = accountIcon.icon,
                        contentDescription = null,
                        tint = accountIcon.color,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (isStartAccount) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Default",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = account.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = (account.balance ?: 0.0).toCurrencyString(),
                style = MaterialTheme.typography.bodyLarge,
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
    isDefaultSelection: Boolean,
    isOtherAccountDefault: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onConfirm: (String, Boolean, Boolean) -> Unit
) {
    var accountName by remember { mutableStateOf(account.name) }
    var isMpesa by remember { mutableStateOf(isMpesaLinked) }
    var isDefault by remember { mutableStateOf(isDefaultSelection || (!isOtherAccountDefault && isMpesa)) }

    LaunchedEffect(isMpesa, isOtherAccountDefault) {
        if (isMpesa && !isOtherAccountDefault) {
            isDefault = true
        }
    }

    BasicAlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(28.dp)
            .widthIn(max = 420.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Edit Account" else "Add New Account",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (isEditing && !account.isDefault) {
                        IconButton(
                            onClick = onDelete,
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Account",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text("Account Name") },
                    placeholder = { Text("e.g. Personal Savings") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading && !account.isDefault,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = if (account.isDefault) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f) 
                                   else MaterialTheme.colorScheme.primary
                        )
                    }
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "ACCOUNT OPTIONS",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)) {
                            AccountOptionRow(
                                title = "M-Pesa SMS Link",
                                subtitle = "Auto-track transactions",
                                icon = Icons.Default.Smartphone,
                                checked = isMpesa,
                                onCheckedChange = { isMpesa = it },
                                enabled = !isLoading
                            )
                            
                            val isDefaultLocked = isMpesa && !isOtherAccountDefault
                            AccountOptionRow(
                                title = "Set as Default",
                                subtitle = if (isDefaultLocked) "Always default for M-Pesa" else "Loads this account first",
                                icon = Icons.Default.Star,
                                checked = isDefault,
                                onCheckedChange = { isDefault = it },
                                enabled = !isLoading && !isDefaultLocked
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { if (accountName.isNotBlank()) onConfirm(accountName, isMpesa, isDefault) },
                        enabled = accountName.isNotBlank() && !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = if (isEditing) "Save Changes" else "Create Account",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountOptionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            modifier = Modifier.scale(0.8f)
        )
    }
}
