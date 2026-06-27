package com.fintrack.shared.feature.budget.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import com.fintrack.shared.feature.settings.ui.LocalCurrency
import com.fintrack.shared.feature.settings.ui.toCurrencyString
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.compose.GreenIncome
import com.example.compose.PinkExpense
import com.example.compose.accountChipBorder
import com.example.compose.accountChipSelectedBg
import com.example.compose.currencyTextColor
import com.example.compose.incomeButtonColor
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.account.ui.AccountsViewModel
import com.fintrack.shared.feature.budget.domain.model.Budget
import com.fintrack.shared.feature.budget.domain.model.BudgetFormState
import com.fintrack.shared.feature.budget.domain.model.BudgetWithStatus
import com.fintrack.shared.feature.core.data.domain.ApiException
import com.fintrack.shared.feature.core.data.domain.getUserFriendlyMessage
import com.fintrack.shared.feature.core.ui.AnimatedNumber
import com.fintrack.shared.feature.core.ui.FinanceNumpad
import com.fintrack.shared.feature.core.ui.ThousandsSeparatorTransformation
import com.fintrack.shared.feature.core.domain.SaveState
import com.fintrack.shared.feature.core.domain.ValidationResult
import com.fintrack.shared.feature.core.ui.KMPBackHandler
import com.fintrack.shared.feature.core.ui.MaterialToast
import com.fintrack.shared.feature.core.ui.CommonErrorState
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.util.formatAsShortDateWithYear
import com.fintrack.shared.feature.core.util.formatToCurrency
import com.fintrack.shared.feature.transaction.domain.model.Category
import com.fintrack.shared.feature.core.ui.FintrackDatePickerDialog
import com.fintrack.shared.feature.navigation.AppBarState
import com.fintrack.shared.feature.navigation.LocalSharedTransitionScope
import com.fintrack.shared.feature.transaction.ui.addtransaction.CategoryChip
import com.fintrack.shared.feature.transaction.ui.addtransaction.TypeToggleButton
import com.fintrack.shared.feature.transaction.ui.home.AccountIcon
import com.fintrack.shared.feature.transaction.ui.util.toColor
import com.fintrack.shared.feature.transaction.ui.util.toIcon
import kotlinx.coroutines.delay
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalSharedTransitionApi::class)
@Composable
fun BudgetDetailScreen(
    budgetId: String?,
    viewModel: BudgetViewModel = koinViewModel(),
    accountsViewModel: AccountsViewModel = koinViewModel(),
    paddingValues: PaddingValues = PaddingValues(0.dp),
    animatedVisibilityScope: AnimatedVisibilityScope,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onUpdateAppBarState: (AppBarState) -> Unit
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedTransitionScope found")

    val selectedBudgetResult by viewModel.selectedBudget.collectAsStateWithLifecycle()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    val deleteResult by viewModel.deleteResult.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val allCategories by viewModel.categories.collectAsStateWithLifecycle()
    val validationState by viewModel.validationState.collectAsStateWithLifecycle()
    val accountsResult by accountsViewModel.accounts.collectAsStateWithLifecycle()

    var showNumpad by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    KMPBackHandler(enabled = showNumpad) {
        showNumpad = false
    }

    val initialFormState = remember(budgetId, selectedBudgetResult, accountsResult) {
        computeInitialFormState(budgetId, selectedBudgetResult, accountsResult)
    }

    LaunchedEffect(initialFormState) {
        viewModel.setFormState(initialFormState)
    }

    LaunchedEffect(budgetId) {
        viewModel.resetDeleteResult()
        budgetId?.let { viewModel.loadBudgetById(it) }
    }

    LaunchedEffect(showNumpad, budgetId) {
        onUpdateAppBarState(
            AppBarState(
                title = if (budgetId == null) "Add Budget" else "Edit Budget",
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

    LaunchedEffect(saveState) {
        if (saveState is SaveState.Success) {
            delay(1000)
            onSave()
            viewModel.resetSaveState()
        }
    }

    LaunchedEffect(deleteResult) {
        if (deleteResult is Result.Success) {
            onBack()
        }
    }

    val themeColor by animateColorAsState(
        targetValue = if (formState.isExpense) PinkExpense else GreenIncome,
        animationSpec = tween(durationMillis = 500)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            budgetId != null && selectedBudgetResult is Result.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = themeColor)
                }
            }

            budgetId != null && selectedBudgetResult is Result.Error -> {
                CommonErrorState(
                    title = "Failed to load budget",
                    error = (selectedBudgetResult as Result.Error).exception,
                    onRetry = { budgetId.let { viewModel.loadBudgetById(it) } }
                )
            }

            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    with(sharedTransitionScope) {
                        BudgetAmountHeader(
                            amount = formState.amount,
                            onAmountChange = { viewModel.setAmount(it) },
                            isExpense = formState.isExpense,
                            themeColor = themeColor,
                            paddingValues = paddingValues,
                            showNumpad = showNumpad,
                            onToggleNumpad = { showNumpad = it },
                            modifier = Modifier.sharedBounds(
                                rememberSharedContentState(key = "budget_header_${budgetId ?: "new"}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = { _, _ ->
                                    spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                },
                                clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
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
                        BudgetTypeSection(
                            isExpense = formState.isExpense,
                            onExpenseChange = { viewModel.setIsExpense(it) },
                            themeColor = themeColor
                        )

                        BudgetNameSection(
                            name = formState.name,
                            onNameChange = { viewModel.setName(it) },
                            onFocus = { showNumpad = false }
                        )

                        AccountSelectionSection(
                            accountsResult = accountsResult,
                            selectedAccount = formState.selectedAccount,
                            onAccountSelected = { viewModel.setAccount(it) },
                            onRetry = { accountsViewModel.reloadAccounts() }
                        )

                        CategorySelectionSection(
                            isExpense = formState.isExpense,
                            selectedCategories = formState.selectedCategories,
                            categories = allCategories,
                            onCategoryChange = { viewModel.setCategories(it) }
                        )

                        PeriodSelectionSection(
                            startDate = formState.startDate,
                            endDate = formState.endDate,
                            onPeriodChange = { start, end -> viewModel.setPeriod(start, end) }
                        )

                        Spacer(modifier = Modifier.height(140.dp))
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = paddingValues.calculateBottomPadding())
                .padding(20.dp)
        ) {
            SaveBudgetButton(
                saveState = saveState,
                validationState = validationState,
                themeColor = themeColor,
                isExpense = formState.isExpense,
                onSaveClick = { 
                    showNumpad = false
                    viewModel.saveBudget() 
                }
            )
        }

        // Delete Button (Top Right)
        if (budgetId != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = paddingValues.calculateTopPadding())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f), CircleShape)
                ) {
                    if (deleteResult is Result.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Budget",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        if (showDeleteDialog) {
            DeleteConfirmationDialog(
                onDismiss = { showDeleteDialog = false },
                onConfirm = {
                    showDeleteDialog = false
                    budgetId?.let { viewModel.removeBudget(it) }
                }
            )
        }

        // Custom Numpad
        androidx.compose.animation.AnimatedVisibility(
            visible = showNumpad,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            FinanceNumpad(
                onNumberClick = { num ->
                    if (formState.amount.length < 12) {
                        viewModel.setAmount(formState.amount + num)
                    }
                },
                onBackspaceClick = {
                    if (formState.amount.isNotEmpty()) {
                        viewModel.setAmount(formState.amount.dropLast(1))
                    }
                },
                onDoneClick = { showNumpad = false }
            )
        }

        if (saveState is SaveState.Error) {
                val message = (saveState as SaveState.Error).exception.let {
                    (it as? ApiException)?.getUserFriendlyMessage() ?: it.message ?: "Failed to save budget"
                }
            MaterialToast(
                message = message,
                isError = true,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun BudgetAmountHeader(
    amount: String,
    onAmountChange: (String) -> Unit,
    isExpense: Boolean,
    themeColor: Color,
    showNumpad: Boolean,
    onToggleNumpad: (Boolean) -> Unit,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    val amountFontSize by animateDpAsState(
        targetValue = when {
            amount.length >= 10 -> 32.dp
            amount.length >= 8 -> 38.dp
            amount.length >= 6 -> 44.dp
            else -> 48.dp
        }.value.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    Surface(
        color = themeColor,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp + paddingValues.calculateTopPadding())
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = { 
                    onToggleNumpad(true)
                    focusRequester.requestFocus() 
                }
            ),
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = isExpense,
                transitionSpec = {
                    if (targetState) {
                        (slideInVertically { height -> height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> -height } + fadeOut())
                    } else {
                        (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> height } + fadeOut())
                    }
                }
            ) { targetIsExpense ->
                Text(
                    text = if (targetIsExpense) "Expense Budget Limit" else "Income Target Limit",
                    color = (if (targetIsExpense) Color.White else MaterialTheme.colorScheme.onTertiary).copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Increased touch target area for the amount
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = { 
                            onToggleNumpad(true)
                            focusRequester.requestFocus() 
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .animateContentSize()
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = { 
                                onToggleNumpad(true)
                                focusRequester.requestFocus() 
                            }
                        )
                ) {
                    Text(
                        text = LocalCurrency.current.symbol,
                        color = (if (isExpense) Color.White else MaterialTheme.colorScheme.onTertiary).copy(alpha = 0.7f),
                        fontSize = (amountFontSize.value * 0.5f).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = (amountFontSize.value * 0.2f).dp, end = 8.dp)
                    )

                    BasicTextField(
                        value = amount,
                        onValueChange = { /* Controlled by Numpad */ },
                        readOnly = true,
                        textStyle = TextStyle(
                            color = Color.Transparent,
                            fontSize = amountFontSize.value.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Start,
                            letterSpacing = 0.sp
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = ThousandsSeparatorTransformation(),
                        cursorBrush = SolidColor(if (isExpense) Color.White else MaterialTheme.colorScheme.onTertiary),
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
                                        color = (if (isExpense) Color.White else MaterialTheme.colorScheme.onTertiary).copy(alpha = 0.4f),
                                        fontSize = amountFontSize.value.sp,
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
                                            color = if (isExpense) Color.White else MaterialTheme.colorScheme.onTertiary,
                                            fontSize = amountFontSize.value.sp,
                                            fontWeight = FontWeight.Black,
                                            textAlign = TextAlign.Start,
                                            letterSpacing = 0.sp
                                        )
                                    )
                                }
                                innerTextField()
                                
                                // Overlay to capture clicks specifically on the amount text area
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable(
                                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                            indication = null,
                                            onClick = { 
                                                onToggleNumpad(true)
                                                focusRequester.requestFocus() 
                                            }
                                        )
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun computeInitialFormState(
    budgetId: String?,
    selectedBudgetResult: Result<BudgetWithStatus>?,
    accountsResult: Result<List<Account>>
): BudgetFormState {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val isAccountsSuccess = accountsResult is Result.Success
    val accountsData = if (isAccountsSuccess) accountsResult.data else emptyList()
    val firstExpenseCategory = Category.expenseCategories.firstOrNull()

    return if (budgetId == null) {
        BudgetFormState(
            name = "",
            amount = "",
            selectedCategories = if (firstExpenseCategory != null) {
                setOf(firstExpenseCategory)
            } else {
                emptySet()
            },
            isExpense = true,
            startDate = today,
            endDate = today.plus(DatePeriod(months = 1)),
            selectedAccount = accountsData.firstOrNull()
        )
    } else {
        when (selectedBudgetResult) {
            is Result.Success -> {
                val budgetWithStatus = selectedBudgetResult.data
                val budget = budgetWithStatus.budget
                BudgetFormState(
                    id = budget.id,
                    name = budget.name,
                    amount = budget.limit.toLong().toString().let { if (it == "0") "" else it },
                    selectedCategories = budget.categories.toSet(),
                    isExpense = budget.isExpense,
                    startDate = budget.startDate,
                    endDate = budget.endDate,
                    selectedAccount = if (isAccountsSuccess) {
                        accountsData.firstOrNull { it.id == budget.accountId }
                    } else {
                        null
                    }
                )
            }
            else -> BudgetFormState()
        }
    }
}

@Composable
fun AccountSelectionSection(
    accountsResult: Result<List<Account>>,
    selectedAccount: Account?,
    onAccountSelected: (Account) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedAccountId by remember(selectedAccount) { mutableStateOf(selectedAccount?.id) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Select Account",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )

        when (accountsResult) {
            is Result.Loading -> {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            is Result.Error -> {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Failed to load accounts",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accountChipBorder
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            is Result.Success -> {
                if (accountsResult.data.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        ) {
                            Text(
                                text = "No accounts available. Create an account first.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(accountsResult.data) { account ->
                            AccountChip(
                                account = account,
                                isSelected = selectedAccountId == account.id,
                                onClick = { onAccountSelected(account) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountChip(
    account: Account,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accountIcon = AccountIcon.fromAccountName(account.name)

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        animationSpec = tween(300)
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        animationSpec = tween(300)
    )
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 0.dp,
        animationSpec = tween(300)
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable { onClick() }
            .widthIn(min = 120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = BorderStroke(
            width = 2.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = accountIcon.icon,
                contentDescription = account.name,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = account.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = account.balance?.toCurrencyString() ?: "${LocalCurrency.current.symbol} --",
                fontSize = 12.sp,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SaveBudgetButton(
    saveState: SaveState<Budget>,
    validationState: ValidationResult,
    themeColor: Color,
    isExpense: Boolean,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFormValid = validationState is ValidationResult.Success
    val isInProgress = saveState is SaveState.Loading
    val isSuccess = saveState is SaveState.Success<*>

    Button(
        onClick = onSaveClick,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = themeColor,
            contentColor = if (isExpense) Color.White else MaterialTheme.colorScheme.onTertiary,
            disabledContainerColor = if (isSuccess) themeColor else themeColor.copy(alpha = 0.5f),
            disabledContentColor = if (isSuccess) Color.White else Color.White.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
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
                    Text("Saved", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }

            else -> {
                Text("Save Budget", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun BudgetTypeSection(
    isExpense: Boolean,
    onExpenseChange: (Boolean) -> Unit,
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
            isSelected = isExpense,
            selectedColor = PinkExpense,
            modifier = Modifier.weight(1f),
            onClick = { onExpenseChange(true) }
        )
        TypeToggleButton(
            text = "Income",
            isSelected = !isExpense,
            selectedColor = GreenIncome,
            modifier = Modifier.weight(1f),
            onClick = { onExpenseChange(false) }
        )
    }
}

@Composable
fun BudgetNameSection(
    name: String,
    onNameChange: (String) -> Unit,
    onFocus: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Budget Name", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = { Text("Enter budget name") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (it.isFocused) onFocus() },
                leadingIcon = {
                    Icon(
                        Icons.Default.Edit,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    cursorColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun CategorySelectionSection(
    isExpense: Boolean,
    selectedCategories: Set<Category>,
    categories: List<Category>,
    onCategoryChange: (Set<Category>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Categories", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
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
                val filteredCategories = categories.filter { it.isExpense == isExpense }

                item {
                    val allSelected = selectedCategories.size == filteredCategories.size
                    CategoryChip(
                        text = "All",
                        icon = Icons.Default.SelectAll,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        selected = allSelected,
                        onClick = {
                            val newSelection = if (allSelected) {
                                emptySet()
                            } else {
                                filteredCategories.toSet()
                            }
                            onCategoryChange(newSelection)
                        }
                    )
                }

                items(filteredCategories.size) { index ->
                    val cat = filteredCategories[index]
                    val selected = selectedCategories.contains(cat)
                    CategoryChip(
                        text = cat.name,
                        icon = cat.toIcon(),
                        color = cat.toColor(),
                        selected = selected,
                        onClick = {
                            val newSelection =
                                if (selected) selectedCategories - cat else selectedCategories + cat
                            onCategoryChange(newSelection)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun PeriodSelectionSection(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onPeriodChange: (LocalDate?, LocalDate?) -> Unit
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Budget Period", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
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
                        .clickable { showStartPicker = true },
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Start Date", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = startDate?.formatAsShortDateWithYear() ?: "Select",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Pick Start Date",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showEndPicker = true },
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("End Date", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = endDate?.formatAsShortDateWithYear() ?: "Select",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Pick End Date",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (showStartPicker) {
        FintrackDatePickerDialog(
            initialDate = startDate ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
            onDateSelected = {
                onPeriodChange(it, endDate)
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false }
        )
    }

    if (showEndPicker) {
        FintrackDatePickerDialog(
            initialDate = endDate ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.plus(DatePeriod(months = 1)),
            onDateSelected = {
                onPeriodChange(startDate, it)
                showEndPicker = false
            },
            onDismiss = { showEndPicker = false }
        )
    }
}

@Composable
fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Delete Budget?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Are you sure you want to delete this budget? This action cannot be undone and you will lose all tracking for this period.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            "Cancel",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text(
                            "Delete",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
