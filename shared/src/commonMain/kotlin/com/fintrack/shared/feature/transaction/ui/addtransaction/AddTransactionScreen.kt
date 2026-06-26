package com.fintrack.shared.feature.transaction.ui.addtransaction

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import com.fintrack.shared.feature.settings.ui.LocalCurrency
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.OverlayClip
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.fintrack.shared.feature.core.ui.AnimatedNumber
import com.fintrack.shared.feature.core.ui.ThousandsSeparatorTransformation
import com.fintrack.shared.feature.core.ui.FintrackDatePickerDialog
import com.fintrack.shared.feature.core.ui.FintrackTimePickerDialog
import com.fintrack.shared.feature.core.ui.MaterialToast
import com.fintrack.shared.feature.core.domain.SaveState
import com.fintrack.shared.feature.core.util.Result
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
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AddTransactionScreen(
    transactionId: String? = null,
    transactionsViewModel: TransactionViewModel = koinViewModel(),
    accountsViewModel: AccountsViewModel = koinViewModel(),
    paddingValues: PaddingValues = PaddingValues(0.dp),
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit = {}
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedTransitionScope found")

    val saveState by transactionsViewModel.saveState.collectAsStateWithLifecycle()
    val accountsResult by accountsViewModel.accounts.collectAsStateWithLifecycle()
    val validationError by transactionsViewModel.validationError.collectAsStateWithLifecycle()
    val selectedTransactionResult by transactionsViewModel.selectedTransaction.collectAsStateWithLifecycle()

    var amount by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf<Category?>(null) }
    var description by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var dateTime by remember {
        mutableStateOf(
            kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var isDataLoaded by remember(transactionId) { mutableStateOf(false) }

    LaunchedEffect(transactionId) {
        if (transactionId != null) {
            transactionsViewModel.loadTransactionById(transactionId)
        } else {
            transactionsViewModel.resetSelectedTransaction()
            isDataLoaded = true // Nothing to load for new transaction
        }
    }

    LaunchedEffect(selectedTransactionResult, accountsResult) {
        if (transactionId != null && 
            selectedTransactionResult is Result.Success && 
            !isDataLoaded) {
            
            val transaction = (selectedTransactionResult as Result.Success<Transaction>).data
            
            // Wait for accounts to be loaded to fully initialize
            if (accountsResult is Result.Success) {
                val accounts = (accountsResult as Result.Success<List<Account>>).data
                
                amount = transaction.amount.toLong().toString()
                isIncome = transaction.isIncome
                category = Category.fromName(transaction.category, !transaction.isIncome)
                description = transaction.description ?: ""
                dateTime = transaction.dateTime
                selectedAccount = accounts.find { it.id == transaction.accountId }
                
                isDataLoaded = true
            }
        }
    }

    val themeColor by animateColorAsState(
        targetValue = if (isIncome) GreenIncome else PinkExpense,
        animationSpec = tween(durationMillis = 500)
    )

    LaunchedEffect(saveState) {
        when (saveState) {
            is SaveState.Success<*> -> {
                delay(1000)
                onBack()
                transactionsViewModel.resetSaveState()
            }
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with Amount
            with(sharedTransitionScope) {
                AmountHeader(
                    amount = amount,
                    onAmountChange = { amount = it },
                    isIncome = isIncome,
                    themeColor = themeColor,
                    paddingValues = paddingValues,
                    modifier = Modifier.sharedBounds(
                        rememberSharedContentState(key = "transaction_header_${transactionId ?: "new"}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ ->
                            spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        },
                        clipInOverlayDuringTransition = OverlayClip(
                            RoundedCornerShape(
                                bottomStart = 32.dp,
                                bottomEnd = 32.dp
                            )
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

                AccountSelectionSection(
                    accountsResult = accountsResult,
                    selectedAccount = selectedAccount,
                    onAccountSelected = { selectedAccount = it },
                    onRetry = { accountsViewModel.reloadAccounts() }
                )

                CategorySelectionSection(
                    isIncome = isIncome,
                    selectedCategory = category,
                    onCategorySelected = { category = it }
                )

                DescriptionInputSection(
                    description = description,
                    onDescriptionChange = { description = it }
                )

                DateTimeSelectionSection(
                    dateTime = dateTime,
                    onDateClicked = { showDatePicker = true },
                    onTimeClicked = { showTimePicker = true }
                )

                Spacer(modifier = Modifier.height(80.dp)) // Space for FAB-like button
            }
        }

        // Prominent Save Button at bottom
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
                selectedAccount = selectedAccount,
                themeColor = themeColor,
                isEditing = transactionId != null,
                onSaveClick = {
                    if (transactionId != null) {
                        transactionsViewModel.updateTransaction(
                            id = transactionId,
                            amount = amount,
                            isIncome = isIncome,
                            category = category,
                            description = description,
                            selectedAccount = selectedAccount,
                            dateTime = dateTime
                        )
                    } else {
                        transactionsViewModel.addTransaction(
                            amount = amount,
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

        // Show validation error from ViewModel
        if (validationError != null) {
            MaterialToast(
                message = validationError!!,
                isError = true,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        if (saveState is SaveState.Error) {
            val error = (saveState as SaveState.Error).exception
            MaterialToast(
                message = error.message ?: "Failed to save transaction",
                isError = true,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun AmountHeader(
    amount: String,
    onAmountChange: (String) -> Unit,
    isIncome: Boolean,
    themeColor: Color,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    Surface(
        color = themeColor,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp + paddingValues.calculateTopPadding())
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = { focusRequester.requestFocus() }
            ),
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedContent(
                    targetState = isIncome,
                    transitionSpec = {
                        if (targetState) {
                            (slideInVertically { height -> height } + fadeIn()).togetherWith(
                                slideOutVertically { height -> -height } + fadeOut())
                        } else {
                            (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                                slideOutVertically { height -> height } + fadeOut())
                        }
                    }
                ) { targetIsIncome ->
                    Text(
                        text = if (targetIsIncome) "Income Amount" else "Expense Amount",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    Text(
                        text = LocalCurrency.current.symbol,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp, end = 8.dp)
                    )

                    BasicTextField(
                        value = amount,
                        onValueChange = { newAmount ->
                            val filtered = newAmount.filter { it.isDigit() }
                            onAmountChange(filtered)
                        },
                        textStyle = TextStyle(
                            color = Color.Transparent,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Start,
                            letterSpacing = 0.sp
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = ThousandsSeparatorTransformation(),
                        cursorBrush = SolidColor(Color.White),
                        singleLine = true,
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .width(IntrinsicSize.Min)
                            .widthIn(min = 16.dp),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (amount.isEmpty()) {
                                    Text(
                                        "0",
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                } else {
                                    val formattedAmount = amount.reversed()
                                        .chunked(3)
                                        .joinToString(",")
                                        .reversed()
                                    
                                    AnimatedNumber(
                                        value = formattedAmount,
                                        style = TextStyle(
                                            color = Color.White,
                                            fontSize = 48.sp,
                                            fontWeight = FontWeight.Black,
                                            textAlign = TextAlign.Start,
                                            letterSpacing = 0.sp
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionTypeSection(
    isIncome: Boolean,
    onIncomeChange: (Boolean) -> Unit,
    themeColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TypeToggleButton(
            text = "Expense",
            isSelected = !isIncome,
            selectedColor = PinkExpense,
            modifier = Modifier.weight(1f),
            onClick = { onIncomeChange(false) }
        )
        TypeToggleButton(
            text = "Income",
            isSelected = isIncome,
            selectedColor = GreenIncome,
            modifier = Modifier.weight(1f),
            onClick = { onIncomeChange(true) }
        )
    }
}

@Composable
fun TypeToggleButton(
    text: String,
    isSelected: Boolean,
    selectedColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else Color.Transparent,
        animationSpec = tween(300)
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300)
    )

    Box(
        modifier = modifier
            .height(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 15.sp
        )
    }
}

@Composable
fun CategorySelectionSection(
    isIncome: Boolean,
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Category", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            LazyHorizontalStaggeredGrid(
                rows = StaggeredGridCells.Adaptive(48.dp),
                horizontalItemSpacing = 8.dp,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp)
            ) {
                val categories =
                    if (isIncome) Category.incomeCategories else Category.expenseCategories
                items(categories.size) { index ->
                    val cat = categories[index]
                    val selected = selectedCategory == cat
                    CategoryChip(
                        text = cat.name,
                        icon = cat.toIcon(),
                        color = cat.toColor(),
                        selected = selected,
                        onClick = { onCategorySelected(if (selected) null else cat) }
                    )
                }
            }
        }
    }
}

@Composable
fun DescriptionInputSection(
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Description", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = description,
                onValueChange = onDescriptionChange,
                placeholder = { Text("Enter description") },
                singleLine = false,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        Icons.AutoMirrored.Filled.Notes,
                        null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = Color.Gray
                )
            )
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
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onDateClicked),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Date", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${dateTime.date}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Pick Date",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onTimeClicked),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Time", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val hourStr = dateTime.time.hour.toString().padStart(2, '0')
                        val minuteStr = dateTime.time.minute.toString().padStart(2, '0')
                        Text(
                            text = "$hourStr:$minuteStr",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = "Pick Time",
                            tint = MaterialTheme.colorScheme.primary
                        )
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
    selectedAccount: Account?,
    themeColor: Color,
    isEditing: Boolean = false,
    onSaveClick: () -> Unit
) {
    val isFormValid = amount.isNotBlank() && category != null && selectedAccount != null
    val isInProgress = saveState is SaveState.Loading
    val isSuccess = saveState is SaveState.Success<*>

    Button(
        onClick = onSaveClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = themeColor,
            contentColor = Color.White,
            disabledContainerColor = if (isSuccess) themeColor else themeColor.copy(alpha = 0.5f),
            disabledContentColor = if (isSuccess) Color.White else Color.White.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
        enabled = !isInProgress && !isSuccess && isFormValid
    ) {
        when (saveState) {
            is SaveState.Loading -> {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            is SaveState.Success<*> -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Saved",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(if (isEditing) "Updated" else "Saved", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }

            else -> {
                Text(if (isEditing) "Update Transaction" else "Save Transaction", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ToggleChip(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    color: Color? = null
) {
    val chipColor = color ?: when (text.lowercase()) {
        "expense" -> PinkExpense
        "income" -> GreenIncome
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) chipColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (selected) chipColor else Color.Transparent)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Icon(
                icon,
                null,
                tint = if (selected) chipColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text,
                color = if (selected) chipColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CategoryChip(
    text: String,
    icon: ImageVector,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    val animatedBgColor by animateColorAsState(
        targetValue = if (selected) color else MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 300)
    )
    val animatedContentColor by animateColorAsState(
        targetValue = if (selected) Color.White else color,
        animationSpec = tween(durationMillis = 300)
    )
    val animatedTextColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color.Black,
        animationSpec = tween(durationMillis = 300)
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = if (selected) color else Color.LightGray.copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 300)
    )
    val animatedElevation by animateDpAsState(
        targetValue = if (selected) 6.dp else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = animatedBgColor,
        border = BorderStroke(
            width = 1.dp,
            color = animatedBorderColor
        ),
        shadowElevation = animatedElevation,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, tint = animatedContentColor)
            Text(text, color = animatedTextColor)
        }
    }
}
