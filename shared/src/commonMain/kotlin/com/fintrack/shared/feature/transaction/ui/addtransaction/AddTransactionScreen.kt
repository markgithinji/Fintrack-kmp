package com.fintrack.shared.feature.transaction.ui.addtransaction

import androidx.compose.animation.*
import androidx.compose.animation.SharedTransitionScope.OverlayClip
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import com.fintrack.shared.feature.settings.ui.LocalCurrency
import com.fintrack.shared.feature.settings.ui.toCurrencyString
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.compose.GreenIncome
import com.example.compose.PinkExpense
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.account.ui.AccountsViewModel
import com.fintrack.shared.feature.budget.ui.AccountSelectionSection
import com.fintrack.shared.feature.core.data.domain.ApiException
import com.fintrack.shared.feature.core.data.domain.getUserFriendlyMessage
import com.fintrack.shared.feature.core.ui.*
import com.fintrack.shared.feature.core.domain.SaveState
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.navigation.AppBarState
import com.fintrack.shared.feature.navigation.LocalSharedTransitionScope
import com.fintrack.shared.feature.transaction.domain.model.Category
import com.fintrack.shared.feature.transaction.domain.model.Transaction
import com.fintrack.shared.feature.transaction.ui.TransactionViewModel
import com.fintrack.shared.feature.transaction.ui.util.toColor
import com.fintrack.shared.feature.transaction.ui.util.toIcon
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

enum class NumpadTarget {
    Amount, TransactionCost
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AddTransactionScreen(
    transactionId: String? = null,
    transactionsViewModel: TransactionViewModel = koinViewModel(),
    accountsViewModel: AccountsViewModel = koinViewModel(),
    paddingValues: PaddingValues = PaddingValues(0.dp),
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit = {},
    onUpdateAppBarState: (AppBarState) -> Unit
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedTransitionScope found")

    val saveState by transactionsViewModel.saveState.collectAsStateWithLifecycle()
    val deleteResult by transactionsViewModel.deleteResult.collectAsStateWithLifecycle()
    val accountsResult by accountsViewModel.accounts.collectAsStateWithLifecycle()
    val validationError by transactionsViewModel.validationError.collectAsStateWithLifecycle()
    val allCategories by transactionsViewModel.categories.collectAsStateWithLifecycle()
    val selectedTransactionResult by transactionsViewModel.selectedTransaction.collectAsStateWithLifecycle()

    val amount by transactionsViewModel.amount.collectAsStateWithLifecycle()
    val transactionCost by transactionsViewModel.transactionCost.collectAsStateWithLifecycle()
    val category by transactionsViewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedAccount by transactionsViewModel.selectedAccount.collectAsStateWithLifecycle()
    val description by transactionsViewModel.description.collectAsStateWithLifecycle()

    var isIncome by remember { mutableStateOf(false) }
    var dateTime by remember {
        mutableStateOf(
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showNumpad by remember { mutableStateOf(false) }
    var numpadTarget by remember { mutableStateOf(NumpadTarget.Amount) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var isDataLoaded by remember(transactionId) { mutableStateOf(false) }

    KMPBackHandler(enabled = showNumpad) {
        showNumpad = false
    }

    LaunchedEffect(showNumpad, transactionId) {
        onUpdateAppBarState(
            AppBarState(
                title = if (transactionId.isNullOrBlank()) "Create Transaction" else "Edit Transaction",
                showBackButton = true,
                onBack = {
                    if (showNumpad) {
                        showNumpad = false
                    } else {
                        onBack()
                    }
                }
            )
        )
    }

    LaunchedEffect(transactionId) {
        transactionsViewModel.resetDeleteResult()
        if (transactionId != null) {
            transactionsViewModel.loadTransactionById(transactionId)
        } else {
            transactionsViewModel.resetSelectedTransaction()
            isDataLoaded = true
        }
    }

    LaunchedEffect(deleteResult) {
        if (deleteResult is Result.Success) {
            onBack()
        }
    }

    LaunchedEffect(selectedTransactionResult, accountsResult) {
        if (transactionId != null && 
            selectedTransactionResult is Result.Success && 
            !isDataLoaded) {
            
            val transaction = (selectedTransactionResult as Result.Success<Transaction>).data
            
            if (accountsResult is Result.Success) {
                val accounts = (accountsResult as Result.Success<List<Account>>).data
                
                transactionsViewModel.onAmountChange(transaction.amount.toString().removeSuffix(".0"))
                transactionsViewModel.onTransactionCostChange(transaction.transactionCost.toString().removeSuffix(".0"))
                transactionsViewModel.onDescriptionChange(transaction.description ?: "")
                isIncome = transaction.isIncome
                transactionsViewModel.onCategoryChange(Category.fromName(transaction.category, !transaction.isIncome))
                dateTime = transaction.dateTime
                transactionsViewModel.onAccountChange(accounts.find { it.id == transaction.accountId })
                
                isDataLoaded = true
            }
        }
    }

    val themeColor by animateColorAsState(
        targetValue = if (isIncome) GreenIncome else PinkExpense,
        animationSpec = tween(durationMillis = 500)
    )

    LaunchedEffect(saveState) {
        if (saveState is SaveState.Success<*>) {
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
                AmountHeader(
                    amount = amount,
                    isIncome = isIncome,
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
                TransactionTypeSection(
                    isIncome = isIncome,
                    onIncomeChange = { isIncome = it },
                    themeColor = themeColor
                )

                TransactionCostSection(
                    cost = transactionCost,
                    onClick = {
                        numpadTarget = NumpadTarget.TransactionCost
                        showNumpad = true
                    }
                )

                AccountSelectionSection(
                    accountsResult = accountsResult,
                    selectedAccount = selectedAccount,
                    onAccountSelected = { transactionsViewModel.onAccountChange(it) },
                    onRetry = { accountsViewModel.reloadAccounts() }
                )

                CategorySelectionSection(
                    isIncome = isIncome,
                    selectedCategory = category,
                    categories = allCategories,
                    onCategorySelected = { transactionsViewModel.onCategoryChange(it) }
                )

                DescriptionInputSection(
                    description = description,
                    onDescriptionChange = { transactionsViewModel.onDescriptionChange(it) },
                    onFocus = { showNumpad = false }
                )

                DateTimeSelectionSection(
                    dateTime = dateTime,
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
            SaveTransactionButton(
                saveState = saveState,
                amount = amount,
                category = category,
                description = description,
                selectedAccount = selectedAccount,
                isIncome = isIncome,
                themeColor = themeColor,
                isEditing = transactionId != null,
                onSaveClick = { 
                    showNumpad = false
                    if (transactionId != null) {
                        transactionsViewModel.updateTransaction(
                            id = transactionId,
                            amount = amount,
                            transactionCost = transactionCost,
                            isIncome = isIncome,
                            category = category,
                            description = description,
                            selectedAccount = selectedAccount,
                            dateTime = dateTime
                        )
                    } else {
                        transactionsViewModel.addTransaction(
                            amount = amount,
                            transactionCost = transactionCost,
                            isIncome = isIncome,
                            category = category,
                            description = description,
                            selectedAccount = selectedAccount,
                            dateTime = dateTime
                        )
                    }
                }
            )
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = showNumpad,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            FinanceNumpad(
                onNumberClick = { num ->
                    when (numpadTarget) {
                        NumpadTarget.Amount -> {
                            if (num == "." && amount.contains(".")) return@FinanceNumpad
                            if (amount.length < 12) transactionsViewModel.onAmountChange(amount + num)
                        }
                        NumpadTarget.TransactionCost -> {
                            if (num == "." && transactionCost.contains(".")) return@FinanceNumpad
                            if (transactionCost.length < 10) transactionsViewModel.onTransactionCostChange(transactionCost + num)
                        }
                    }
                },
                onBackspaceClick = {
                    when (numpadTarget) {
                        NumpadTarget.Amount -> {
                            if (amount.isNotEmpty()) transactionsViewModel.onAmountChange(amount.dropLast(1))
                        }
                        NumpadTarget.TransactionCost -> {
                            if (transactionCost.isNotEmpty()) transactionsViewModel.onTransactionCostChange(transactionCost.dropLast(1))
                        }
                    }
                },
                onDoneClick = { showNumpad = false }
            )
        }

        if (showDatePicker) {
            FintrackDatePickerDialog(
                initialDate = dateTime.date,
                onDateSelected = { selectedDate ->
                    dateTime = LocalDateTime(date = selectedDate, time = dateTime.time)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }

        if (showTimePicker) {
            FintrackTimePickerDialog(
                initialTime = dateTime.time,
                onTimeSelected = { selectedTime ->
                    dateTime = LocalDateTime(date = dateTime.date, time = selectedTime)
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
fun AmountHeader(
    amount: String,
    isIncome: Boolean,
    themeColor: Color,
    onToggleNumpad: (Boolean) -> Unit,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier
) {
    Surface(
        color = themeColor,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp + paddingValues.calculateTopPadding())
            .clickable { onToggleNumpad(true) },
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding()).padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isIncome) "Income Amount" else "Expense Amount",
                    color = (if (isIncome) MaterialTheme.colorScheme.onTertiary else Color.White).copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = LocalCurrency.current.symbol,
                        color = (if (isIncome) MaterialTheme.colorScheme.onTertiary else Color.White).copy(alpha = 0.7f),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp, end = 8.dp)
                    )

                    val formattedAmount = if (amount.isEmpty()) {
                        "0"
                    } else {
                        val parts = amount.split(".")
                        val integerPart = parts[0].reversed().chunked(3).joinToString(",").reversed()
                        if (parts.size > 1) "$integerPart.${parts[1]}" else integerPart
                    }
                    
                    AnimatedNumber(
                        value = formattedAmount,
                        style = TextStyle(
                            color = if (isIncome) MaterialTheme.colorScheme.onTertiary else Color.White,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Start,
                            letterSpacing = 0.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionTypeSection(isIncome: Boolean, onIncomeChange: (Boolean) -> Unit, themeColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TypeToggleButton(text = "Expense", isSelected = !isIncome, selectedColor = PinkExpense, modifier = Modifier.weight(1f), onClick = { onIncomeChange(false) })
        TypeToggleButton(text = "Income", isSelected = isIncome, selectedColor = GreenIncome, modifier = Modifier.weight(1f), onClick = { onIncomeChange(true) })
    }
}

@Composable
fun TypeToggleButton(text: String, isSelected: Boolean, selectedColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val backgroundColor by animateColorAsState(targetValue = if (isSelected) selectedColor else Color.Transparent, animationSpec = tween(300))
    val contentColor by animateColorAsState(targetValue = if (isSelected) (if (text.lowercase() == "income") MaterialTheme.colorScheme.onTertiary else Color.White) else MaterialTheme.colorScheme.onSurfaceVariant, animationSpec = tween(300))

    Box(
        modifier = modifier.height(48.dp).clip(RoundedCornerShape(16.dp)).background(backgroundColor).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = contentColor, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, fontSize = 15.sp)
    }
}

@Composable
fun TransactionCostSection(cost: String, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Transaction Cost", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
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
                Text(LocalCurrency.current.symbol, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun CategorySelectionSection(isIncome: Boolean, selectedCategory: Category?, categories: List<Category>, onCategorySelected: (Category?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Category", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth().height(180.dp)
        ) {
            LazyHorizontalStaggeredGrid(
                rows = StaggeredGridCells.Adaptive(48.dp),
                horizontalItemSpacing = 8.dp,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.fillMaxSize().padding(vertical = 8.dp)
            ) {
                val filteredCategories = categories.filter { it.isExpense == !isIncome }
                items(filteredCategories.size) { index ->
                    val cat = filteredCategories[index]
                    val selected = selectedCategory == cat
                    CategoryChip(text = cat.name, icon = cat.toIcon(), color = cat.toColor(), selected = selected, onClick = { onCategorySelected(if (selected) null else cat) })
                }
            }
        }
    }
}

@Composable
fun CategoryChip(text: String, icon: ImageVector, color: Color, selected: Boolean, onClick: () -> Unit) {
    val animatedBgColor by animateColorAsState(targetValue = if (selected) color else MaterialTheme.colorScheme.surface, animationSpec = tween(300))
    val animatedContentColor by animateColorAsState(targetValue = if (selected) (if (Category.incomeCategories.any { it.name == text }) MaterialTheme.colorScheme.onTertiary else Color.White) else color, animationSpec = tween(300))
    val animatedTextColor by animateColorAsState(targetValue = if (selected) (if (Category.incomeCategories.any { it.name == text }) MaterialTheme.colorScheme.onTertiary else Color.White) else MaterialTheme.colorScheme.onSurface, animationSpec = tween(300))

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = animatedBgColor,
        border = BorderStroke(width = 1.dp, color = if (selected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.graphicsLayer { scaleX = if (selected) 1.05f else 1f; scaleY = if (selected) 1.05f else 1f }
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null, tint = animatedContentColor, modifier = Modifier.size(18.dp))
            Text(text, color = animatedTextColor, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun DescriptionInputSection(description: String, onDescriptionChange: (String) -> Unit, onFocus: () -> Unit = {}) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Description", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = description,
                onValueChange = onDescriptionChange,
                placeholder = { Text("Enter description") },
                singleLine = false,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().onFocusChanged { if (it.isFocused) onFocus() },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun DateTimeSelectionSection(dateTime: LocalDateTime, onDateClicked: () -> Unit, onTimeClicked: () -> Unit) {
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
                        Text(text = "${dateTime.date}", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.DateRange, contentDescription = "Pick Date", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f).clickable(onClick = onTimeClicked), horizontalAlignment = Alignment.Start) {
                    Text("Time", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val hourStr = dateTime.time.hour.toString().padStart(2, '0')
                        val minuteStr = dateTime.time.minute.toString().padStart(2, '0')
                        Text(text = "$hourStr:$minuteStr", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.AccessTime, contentDescription = "Pick Time", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SaveTransactionButton(
    saveState: SaveState<Transaction>,
    amount: String,
    category: Category?,
    description: String,
    selectedAccount: Account?,
    isIncome: Boolean,
    themeColor: Color,
    isEditing: Boolean = false,
    onSaveClick: () -> Unit
) {
    val isFormValid = amount.isNotBlank() && category != null && selectedAccount != null && description.isNotBlank()
    val isInProgress = saveState is SaveState.Loading
    val isSuccess = saveState is SaveState.Success<*>

    Button(
        onClick = onSaveClick,
        modifier = Modifier.fillMaxWidth().height(64.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFormValid || isSuccess) themeColor else themeColor.copy(alpha = 0.5f),
            contentColor = (if (isIncome) MaterialTheme.colorScheme.onTertiary else Color.White).let { if (isFormValid || isSuccess) it else it.copy(alpha = 0.5f) },
            disabledContainerColor = if (isSuccess) themeColor else themeColor.copy(alpha = 0.5f),
            disabledContentColor = if (isSuccess) (if (isIncome) MaterialTheme.colorScheme.onTertiary else Color.White) else (if (isIncome) MaterialTheme.colorScheme.onTertiary else Color.White).copy(alpha = 0.5f)
        ),
        enabled = !isInProgress && !isSuccess
    ) {
        if (isInProgress) {
            CircularProgressIndicator(color = if (isIncome) MaterialTheme.colorScheme.onTertiary else Color.White, modifier = Modifier.size(22.dp))
        } else if (isSuccess) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.CheckCircle, "Saved", tint = if (isIncome) MaterialTheme.colorScheme.onTertiary else Color.White, modifier = Modifier.size(20.dp))
                Text(if (isEditing) "Updated" else "Saved", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        } else {
            Text(if (isEditing) "Update Transaction" else "Save Transaction", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}
