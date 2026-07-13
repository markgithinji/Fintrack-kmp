package com.fintrack.shared.feature.transaction.ui.addtransaction

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import com.fintrack.shared.feature.navigation.ui.toCurrencyString
import com.fintrack.shared.feature.navigation.ui.MainViewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.shared.ui.theme.GreenIncome
import com.fintrack.shared.ui.theme.PinkExpense
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.account.ui.AccountsViewModel
import com.fintrack.shared.feature.budget.ui.AccountSelectionSection
import com.fintrack.shared.feature.core.data.model.ApiException
import com.fintrack.shared.feature.core.data.model.getUserFriendlyMessage
import com.fintrack.shared.feature.core.ui.*
import com.fintrack.shared.feature.core.domain.SaveState
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.util.formatAsShortDateWithYear
import com.fintrack.shared.feature.settings.domain.util.format
import com.fintrack.shared.feature.navigation.ui.LocalTimeFormat
import com.fintrack.shared.feature.navigation.ui.LocalSharedTransitionScope
import com.fintrack.shared.feature.category.domain.model.Category
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import com.fintrack.shared.feature.transaction.ui.TransactionViewModel
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AddTransactionScreen(
    transactionId: String? = null,
    transactionsViewModel: TransactionViewModel = koinViewModel(),
    accountsViewModel: AccountsViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinInject(),
    paddingValues: PaddingValues = PaddingValues(0.dp),
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit = {}
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedTransitionScope found")

    val saveState by transactionsViewModel.saveState.collectAsStateWithLifecycle()
    val deleteResult by transactionsViewModel.deleteResult.collectAsStateWithLifecycle()
    val accountsResult by accountsViewModel.accounts.collectAsStateWithLifecycle()
    val validationError by transactionsViewModel.validationError.collectAsStateWithLifecycle()
    val allCategories by transactionsViewModel.categories.collectAsStateWithLifecycle()
    val formState by transactionsViewModel.formState.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showNumpad by remember { mutableStateOf(false) }
    var numpadTarget by remember { mutableStateOf(NumpadTarget.Amount) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    KMPBackHandler(enabled = showNumpad) {
        showNumpad = false
    }

    LaunchedEffect(transactionId, accountsResult) {
        transactionsViewModel.resetDeleteResult()
        if (transactionId != null) {
            if (accountsResult is Result.Success) {
                transactionsViewModel.loadTransactionById(
                    transactionId,
                    (accountsResult as Result.Success).data
                )
            }
        } else {
            transactionsViewModel.resetSelectedTransaction()
        }
    }

    LaunchedEffect(deleteResult) {
        if (deleteResult is Result.Success) {
            mainViewModel.triggerGlobalRefresh()
            onBack()
        }
    }

    val themeColor by animateColorAsState(
        targetValue = if (formState.isIncome) GreenIncome else PinkExpense,
        animationSpec = tween(durationMillis = 500)
    )

    LaunchedEffect(saveState) {
        if (saveState is SaveState.Success<*>) {
            mainViewModel.triggerGlobalRefresh()
            delay(1000)
            onBack()
            transactionsViewModel.resetSaveState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            with(sharedTransitionScope) {
                FinanceAmountHeader(
                    amount = formState.amount,
                    label = if (formState.isIncome) "Income Amount" else "Expense Amount",
                    isIncome = formState.isIncome,
                    themeColor = themeColor,
                    paddingValues = paddingValues,
                    onToggleNumpad = {
                        numpadTarget = NumpadTarget.Amount
                        showNumpad = it
                    },
                    modifier = Modifier.sharedBounds(
                        rememberSharedContentState(key = "transaction_header_${transactionId ?: "new"}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ ->
                            spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
                        },
                        clipInOverlayDuringTransition = OverlayClip(
                            RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        )
                    )
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                FinanceTypeSection(
                    isIncome = formState.isIncome,
                    onTypeChange = { transactionsViewModel.onTypeChange(it) }
                )

                TransactionCostSection(
                    cost = formState.transactionCost,
                    onClick = {
                        numpadTarget = NumpadTarget.TransactionCost
                        showNumpad = true
                    }
                )

                AccountSelectionSection(
                    accountsResult = accountsResult,
                    selectedAccount = formState.selectedAccount,
                    onAccountSelected = { transactionsViewModel.onAccountChange(it) },
                    onRetry = { accountsViewModel.reloadAccounts() }
                )

                FinanceCategorySelection(
                    label = "Category",
                    categories = allCategories,
                    selectedCategories = formState.selectedCategory?.let { setOf(it) } ?: emptySet(),
                    onCategorySelectionChange = { transactionsViewModel.onCategoryChange(it.firstOrNull()) },
                    isExpense = !formState.isIncome,
                    multiSelect = false
                )

                FinanceInputSection(
                    label = "Description",
                    value = formState.description,
                    onValueChange = { transactionsViewModel.onDescriptionChange(it) },
                    placeholder = "Enter description",
                    icon = Icons.AutoMirrored.Filled.Notes,
                    singleLine = false,
                    onFocus = { showNumpad = false }
                )

                DateTimeSelectionSection(
                    dateTime = formState.dateTime.toLocalDateTime(TimeZone.currentSystemDefault()),
                    onDateClicked = { showDatePicker = true },
                    onTimeClicked = { showTimePicker = true }
                )

                Spacer(modifier = Modifier.height(140.dp))
            }
        }

        if (transactionId != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = paddingValues.calculateTopPadding())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f), CircleShape)
                ) {
                    if (deleteResult is Result.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onSurface, strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Transaction", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        if (showDeleteDialog) {
            ConfirmationDialog(
                title = "Delete Transaction?",
                message = "Are you sure you want to delete this transaction? This action cannot be undone.",
                confirmLabel = "Delete",
                isDestructive = true,
                onConfirm = { transactionId?.let { transactionsViewModel.deleteTransaction(it) } },
                onDismiss = { showDeleteDialog = false }
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = paddingValues.calculateBottomPadding())
                .padding(20.dp)
        ) {
            FinanceSaveButton(
                saveState = saveState,
                isFormValid = formState.amount.isNotBlank() && formState.selectedCategory != null && formState.selectedAccount != null && formState.description.isNotBlank(),
                themeColor = themeColor,
                contentColor = if (formState.isIncome) MaterialTheme.colorScheme.onTertiary else Color.White,
                onSaveClick = { 
                    showNumpad = false
                    if (transactionId != null) {
                        transactionsViewModel.updateTransaction(id = transactionId)
                    } else {
                        transactionsViewModel.addTransaction()
                    }
                },
                label = if (transactionId != null) "Update Transaction" else "Save Transaction",
                successLabel = if (transactionId != null) "Updated" else "Saved"
            )
        }

        AnimatedVisibility(
            visible = showNumpad,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            FinanceNumpad(
                onNumberClick = { num ->
                    when (numpadTarget) {
                        NumpadTarget.Amount -> {
                            if (num == "." && formState.amount.contains(".")) return@FinanceNumpad
                            if (formState.amount.length < 12) transactionsViewModel.onAmountChange(formState.amount + num)
                        }
                        NumpadTarget.TransactionCost -> {
                            if (num == "." && formState.transactionCost.contains(".")) return@FinanceNumpad
                            if (formState.transactionCost.length < 10) transactionsViewModel.onTransactionCostChange(formState.transactionCost + num)
                        }
                    }
                },
                onBackspaceClick = {
                    when (numpadTarget) {
                        NumpadTarget.Amount -> {
                            if (formState.amount.isNotEmpty()) transactionsViewModel.onAmountChange(formState.amount.dropLast(1))
                        }
                        NumpadTarget.TransactionCost -> {
                            if (formState.transactionCost.isNotEmpty()) transactionsViewModel.onTransactionCostChange(formState.transactionCost.dropLast(1))
                        }
                    }
                },
                onDoneClick = { showNumpad = false }
            )
        }

        if (showDatePicker) {
            val localDateTime = formState.dateTime.toLocalDateTime(TimeZone.currentSystemDefault())
            FintrackDatePickerDialog(
                initialDate = localDateTime.date,
                onDateSelected = { selectedDate ->
                    transactionsViewModel.onDateTimeChange(
                        LocalDateTime(date = selectedDate, time = localDateTime.time).toInstant(TimeZone.currentSystemDefault())
                    )
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }

        if (showTimePicker) {
            val localDateTime = formState.dateTime.toLocalDateTime(TimeZone.currentSystemDefault())
            FintrackTimePickerDialog(
                initialTime = localDateTime.time,
                onTimeSelected = { selectedTime ->
                    transactionsViewModel.onDateTimeChange(
                        LocalDateTime(date = localDateTime.date, time = selectedTime).toInstant(TimeZone.currentSystemDefault())
                    )
                    showTimePicker = false
                },
                onDismiss = { showTimePicker = false }
            )
        }

        if (validationError != null) {
            MaterialToast(
                message = validationError!!,
                isError = true,
                onDismiss = { transactionsViewModel.clearValidationError() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = paddingValues.calculateBottomPadding() + 84.dp)
            )
        }

        if (saveState is SaveState.Error) {
            val error = (saveState as SaveState.Error).exception
            val message = (error as? ApiException)?.getUserFriendlyMessage() ?: error.message ?: "Failed to save transaction"
            MaterialToast(
                message = message,
                isError = true,
                onDismiss = { transactionsViewModel.resetSaveState() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = paddingValues.calculateBottomPadding() + 84.dp)
            )
        }
    }
}

@Composable
fun TransactionCostSection(
    cost: String,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Transaction Fees", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payments, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (cost.isEmpty()) "0.00" else cost.toDoubleOrNull()?.toCurrencyString() ?: cost,
                        color = if (cost.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun DateTimeSelectionSection(
    dateTime: LocalDateTime,
    onDateClicked: () -> Unit,
    onTimeClicked: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Date & Time", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f).clickable(onClick = onDateClicked), horizontalAlignment = Alignment.Start) {
                    Text("Date", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = dateTime.date.formatAsShortDateWithYear(), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.DateRange, contentDescription = "Pick Date", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f).clickable(onClick = onTimeClicked), horizontalAlignment = Alignment.Start) {
                    Text("Time", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val timeFormat = LocalTimeFormat.current
                        Text(text = dateTime.time.format(timeFormat), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.AccessTime, contentDescription = "Pick Time", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

enum class NumpadTarget {
    Amount, TransactionCost
}
