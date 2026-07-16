package com.fintrack.shared.feature.account.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.account.domain.model.AccountType
import com.fintrack.shared.feature.core.data.model.ApiException
import com.fintrack.shared.feature.core.data.model.getUserFriendlyMessage
import com.fintrack.shared.feature.core.ui.CommonErrorState
import com.fintrack.shared.feature.core.ui.ConfirmationDialog
import com.fintrack.shared.feature.core.ui.MaterialToast
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.util.toRelativeString
import com.fintrack.shared.feature.navigation.ui.LocalBiometricAuthenticator
import com.fintrack.shared.feature.navigation.ui.MainViewModel
import com.fintrack.shared.feature.navigation.ui.toCurrencyString
import com.fintrack.shared.feature.settings.domain.util.BiometricResult
import com.fintrack.shared.feature.settings.ui.SettingsViewModel
import com.fintrack.shared.feature.transaction.ui.home.components.AccountIcon
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    accountsViewModel: AccountsViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinInject()
) {
    val accountsState by accountsViewModel.accounts.collectAsStateWithLifecycle()
    val deleteResult by accountsViewModel.deleteResult.collectAsStateWithLifecycle()
    val saveResult by accountsViewModel.saveResult.collectAsStateWithLifecycle()
    val clearDataResult by accountsViewModel.clearDataResult.collectAsStateWithLifecycle()
    val refreshTrigger by mainViewModel.refreshTrigger.collectAsStateWithLifecycle()

    val biometricAuthenticator = LocalBiometricAuthenticator.current
    val scope = rememberCoroutineScope()

    var showAccountDialog by remember { mutableStateOf<Account?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    val isOperating =
        (deleteResult is Result.Loading) || (saveResult is Result.Loading) || (clearDataResult is Result.Loading)

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            accountsViewModel.reloadAccounts(showLoading = false)
        }
    }

    var pendingDefaultAccountId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(saveResult) {
        val result = saveResult
        if (result is Result.Success) {
            toastMessage = (if (isEditing) "Account updated" else "Account added") to false
            
            // Handle pending default account assignment for new accounts
            pendingDefaultAccountId?.let {
                if (it == "NEW_ACCOUNT_PENDING") {
                    settingsViewModel.setDefaultAccountId(result.data.id)
                }
                pendingDefaultAccountId = null
            }
        }
    }

    LaunchedEffect(saveResult, deleteResult, clearDataResult) {
        if (saveResult is Result.Success || deleteResult is Result.Success || clearDataResult is Result.Success) {
            mainViewModel.triggerGlobalRefresh()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = accountsState) {
                is Result.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is Result.Success -> {
                    val defaultAccountId by settingsViewModel.defaultAccountId.collectAsStateWithLifecycle()
                    val effectiveDefaultAccountId =
                        defaultAccountId ?: state.data.find { it.type == AccountType.MPESA }?.id

                    AccountList(
                        accounts = state.data,
                        defaultAccountId = effectiveDefaultAccountId,
                        topPadding = paddingValues.calculateTopPadding() + 12.dp,
                        bottomPadding = paddingValues.calculateBottomPadding() + 32.dp,
                        onEditAccount = {
                            if (!isOperating) {
                                accountsViewModel.clearResults()
                                showAccountDialog = it
                                isEditing = true
                            }
                        },
                        onAddAccount = {
                            if (!isOperating) {
                                accountsViewModel.clearResults()
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
                        onRetry = { accountsViewModel.reloadAccounts() }
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
                    .padding(bottom = paddingValues.calculateBottomPadding() + 32.dp)
            )
        }
    }

    showAccountDialog?.let { account ->
        val defaultAccountId by settingsViewModel.defaultAccountId.collectAsStateWithLifecycle()
        val isOtherAccountDefault = defaultAccountId != null && defaultAccountId != account.id

        AccountDialog(
            account = account,
            isEditing = isEditing,
            isLoading = saveResult is Result.Loading ||
                    deleteResult is Result.Loading ||
                    clearDataResult is Result.Loading,
            saveResult = saveResult,
            deleteResult = deleteResult,
            clearDataResult = clearDataResult,
            accountType = account.type,
            isDefaultSelection = account.id == defaultAccountId,
            isOtherAccountDefault = isOtherAccountDefault,
            onDismiss = {
                if (!isOperating) {
                    showAccountDialog = null
                    accountsViewModel.clearResults()
                }
            },
            onDelete = {
                scope.launch {
                    val authResult = biometricAuthenticator.authenticate(
                        title = "Delete Account",
                        subtitle = "Confirm your identity to delete this account"
                    )
                    if (authResult is BiometricResult.Success || authResult is BiometricResult.NotAvailable) {
                        accountsViewModel.removeAccount(account.id)
                    }
                }
            },
            onClearData = {
                scope.launch {
                    val authResult = biometricAuthenticator.authenticate(
                        title = "Clear Account Data",
                        subtitle = "Confirm your identity to delete all transactions for this account"
                    )
                    if (authResult is BiometricResult.Success || authResult is BiometricResult.NotAvailable) {
                        accountsViewModel.clearAccountData(account.id)
                    }
                }
            },
            onClearResults = { accountsViewModel.clearResults() },
            onConfirm = { name, type, sources, isDefault ->
                println("ACCOUNTS_DEBUG: onConfirm clicked - Name: $name, Sources: $sources, isDefault: $isDefault")
                accountsViewModel.saveAccount(account.copy(
                    name = name, 
                    type = type, 
                    isDefault = isDefault,
                    linkedSources = sources
                ))
                if (account.id.isNotEmpty()) {
                    if (isDefault) {
                        println("ACCOUNTS_DEBUG: Setting default account ID: ${account.id}")
                        settingsViewModel.setDefaultAccountId(account.id)
                    } else if (account.id == defaultAccountId) {
                        println("ACCOUNTS_DEBUG: Clearing default account ID")
                        settingsViewModel.setDefaultAccountId(null)
                    }
                } else if (isDefault) {
                    println("ACCOUNTS_DEBUG: New account pending default assignment")
                    pendingDefaultAccountId = "NEW_ACCOUNT_PENDING"
                }
            }
        )
    }
}

@Composable
fun AccountList(
    accounts: List<Account>,
    defaultAccountId: String?,
    topPadding: Dp = 0.dp,
    bottomPadding: Dp = 0.dp,
    onEditAccount: (Account) -> Unit,
    onAddAccount: () -> Unit
) {
    val totalBalance = remember(accounts) { 
        accounts.fold(BigDecimal.ZERO) { acc, account -> acc + (account.balance ?: BigDecimal.ZERO) } 
    }

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
        item(span = { GridItemSpan(2) }) {
            NetWorthHeader(totalBalance)
        }

        item(span = { GridItemSpan(2) }) {
            Text(
                text = "MY ACCOUNTS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
            )
        }

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
private fun NetWorthHeader(totalBalance: BigDecimal) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TOTAL BALANCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = totalBalance.toCurrencyString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
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
        modifier = Modifier.fillMaxWidth().height(110.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
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
        }
    }
}

@Composable
fun AccountItem(
    account: Account,
    isStartAccount: Boolean,
    onEdit: () -> Unit
) {
    val accountIcon = AccountIcon.fromAccountType(account.type, account.name)

    Surface(
        onClick = onEdit,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        ),
        modifier = Modifier.fillMaxWidth().height(110.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
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
                text = (account.balance ?: BigDecimal.ZERO).toCurrencyString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            account.lastSyncedAt?.let { lastSync ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Synced ${lastSync.toRelativeString()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
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
    saveResult: Result<Account>?,
    deleteResult: Result<Unit>?,
    clearDataResult: Result<Unit>?,
    accountType: AccountType,
    isDefaultSelection: Boolean,
    isOtherAccountDefault: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onClearData: () -> Unit,
    onClearResults: () -> Unit,
    onConfirm: (String, AccountType, List<String>, Boolean) -> Unit
) {
    var accountName by remember(account.id) { mutableStateOf(account.name) }
    var type by remember(account.id) { mutableStateOf(accountType) }
    var linkedSources by remember(account.id) { mutableStateOf(account.linkedSources.toSet()) }
    var isDefault by remember(account.id) { 
        mutableStateOf(isDefaultSelection || (type == AccountType.MPESA && !isOtherAccountDefault))
    }
    var showClearDataConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Stay in loading state if save was successful to avoid flicker before dismissal
    val isEffectivelyLoading = isLoading || saveResult is Result.Success

    val hasChanges = accountName != account.name ||
            type != accountType ||
            isDefault != isDefaultSelection ||
            linkedSources != account.linkedSources.toSet()

    println("ACCOUNTS_DEBUG: Dialog State - NameChanged: ${accountName != account.name}, TypeChanged: ${type != accountType}, DefaultChanged: ${isDefault != isDefaultSelection}, hasChanges: $hasChanges")

    val saveError = (saveResult as? Result.Error)?.let {
        (it.exception as? ApiException)?.getUserFriendlyMessage()
            ?: it.exception.message
            ?: "An error occurred"
    }

    LaunchedEffect(saveResult) {
        if (saveResult is Result.Success) {
            onDismiss()
        }
    }

    if (showClearDataConfirm) {
        ConfirmationDialog(
            title = "Clear Account Data",
            message = "Are you sure you want to delete all transactions and budgets for '${account.name}'? This action cannot be undone.",
            confirmLabel = "Clear Data",
            isDestructive = true,
            autoDismiss = false,
            isLoading = clearDataResult is Result.Loading,
            isSuccess = clearDataResult is Result.Success,
            errorMessage = (clearDataResult as? Result.Error)?.exception?.message,
            successTitle = "Data Cleared",
            successMessage = "All transactions and budgets for '${account.name}' have been removed.",
            onConfirm = onClearData,
            onDismiss = {
                showClearDataConfirm = false
                onClearResults()
            }
        )
    }

    if (showDeleteConfirm) {
        ConfirmationDialog(
            title = "Delete Account",
            message = "Are you sure you want to permanently delete '${account.name}' and all its associated data?",
            confirmLabel = "Delete Account",
            isDestructive = true,
            autoDismiss = false,
            isLoading = deleteResult is Result.Loading,
            isSuccess = deleteResult is Result.Success,
            errorMessage = (deleteResult as? Result.Error)?.exception?.message,
            successTitle = "Account Deleted",
            successMessage = "The account '${account.name}' has been successfully removed.",
            onConfirm = onDelete,
            onDismiss = {
                showDeleteConfirm = false
                if (deleteResult is Result.Success) {
                    onDismiss() // Close the AccountDialog as well
                }
                onClearResults()
            }
        )
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
                            onClick = { showDeleteConfirm = true },
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
                    onValueChange = {
                        accountName = it
                        if (saveResult is Result.Error) onClearResults()
                    },
                    label = { Text("Account Name") },
                    placeholder = { Text("e.g. Personal Savings") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading && !account.isDefault,
                    isError = saveError != null,
                    supportingText = saveError?.let { { Text(it) } },
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = if (account.isDefault) MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.38f
                            )
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
                                checked = linkedSources.contains("mpesa"),
                                onCheckedChange = { checked ->
                                    linkedSources = if (checked) linkedSources + "mpesa" else linkedSources - "mpesa"
                                    // Only auto-set default if the actual account type is MPESA
                                    if (checked && type == AccountType.MPESA && !isOtherAccountDefault) {
                                        isDefault = true
                                    }
                                },
                                enabled = !isEffectivelyLoading
                            )

                            AccountOptionRow(
                                title = "Equity Bank SMS Link",
                                subtitle = "Auto-track bank transactions",
                                icon = Icons.Default.AccountBalance,
                                checked = linkedSources.contains("equity"),
                                onCheckedChange = { checked ->
                                    linkedSources = if (checked) linkedSources + "equity" else linkedSources - "equity"
                                },
                                enabled = !isEffectivelyLoading
                            )

                            val isDefaultLocked = type == AccountType.MPESA && !isOtherAccountDefault
                            AccountOptionRow(
                                title = "Set as Default",
                                subtitle = if (isDefaultLocked) "M-Pesa is default when no other account is set" else "Loads this account first",
                                icon = Icons.Default.Star,
                                checked = isDefault,
                                onCheckedChange = { isDefault = it },
                                enabled = !isEffectivelyLoading && !isDefaultLocked
                            )

                            if (isEditing) {
                                AccountActionRow(
                                    title = "Clear Data",
                                    subtitle = "Delete all transactions",
                                    icon = Icons.Default.Delete,
                                    onClick = { showClearDataConfirm = true },
                                    enabled = !isEffectivelyLoading,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
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
                        onClick = {
                            if (accountName.isNotBlank()) onConfirm(
                                accountName,
                                type,
                                linkedSources.toList(),
                                isDefault
                            )
                        },
                        enabled = accountName.isNotBlank() && !isLoading && (hasChanges || !isEditing),
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
private fun AccountActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = tint.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = tint
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun AccountOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean
) {
    // Optimistic update state to prevent flicker
    var internalChecked by remember(checked) { mutableStateOf(checked) }

    fun handleToggle(newValue: Boolean) {
        if (enabled && newValue != internalChecked) {
            internalChecked = newValue
            onCheckedChange(newValue)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { handleToggle(!internalChecked) }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = if (internalChecked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (internalChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = internalChecked,
            onCheckedChange = { handleToggle(it) },
            enabled = enabled,
            modifier = Modifier.scale(0.8f)
        )
    }
}
