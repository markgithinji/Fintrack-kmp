package com.fintrack.shared.feature.budget.ui

import androidx.compose.animation.AnimatedVisibility
import com.fintrack.shared.feature.navigation.LocalCurrency
import com.fintrack.shared.feature.navigation.toCurrencyString
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.shared.ui.theme.GreenIncome
import com.fintrack.shared.ui.theme.PinkExpense
import com.fintrack.shared.ui.theme.accountChipBorder
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.account.ui.AccountsViewModel
import com.fintrack.shared.feature.budget.domain.model.BudgetFormState
import com.fintrack.shared.feature.budget.domain.model.BudgetWithStatus
import com.fintrack.shared.feature.core.data.model.ApiException
import com.fintrack.shared.feature.core.data.model.getUserFriendlyMessage
import com.fintrack.shared.feature.core.ui.FinanceAmountHeader
import com.fintrack.shared.feature.core.ui.FinanceCategorySelection
import com.fintrack.shared.feature.core.ui.FinanceInputSection
import com.fintrack.shared.feature.core.ui.FinanceNumpad
import com.fintrack.shared.feature.core.ui.FinanceSaveButton
import com.fintrack.shared.feature.core.ui.FinanceTypeSection
import com.fintrack.shared.feature.core.domain.SaveState
import com.fintrack.shared.feature.core.domain.ValidationResult
import com.fintrack.shared.feature.core.ui.KMPBackHandler
import com.fintrack.shared.feature.core.ui.MaterialToast
import com.fintrack.shared.feature.core.ui.CommonErrorState
import com.fintrack.shared.feature.core.ui.ConfirmationDialog
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.util.formatAsShortDateWithYear
import com.fintrack.shared.feature.category.domain.model.Category
import com.fintrack.shared.feature.core.ui.FintrackDatePickerDialog
import com.fintrack.shared.feature.navigation.AppBarState
import com.fintrack.shared.feature.navigation.LocalSharedTransitionScope
import com.fintrack.shared.feature.navigation.MainViewModel
import com.fintrack.shared.feature.transaction.ui.home.AccountIcon
import kotlinx.coroutines.delay
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BudgetDetailScreen(
    budgetId: String?,
    viewModel: BudgetViewModel = koinViewModel(),
    accountsViewModel: AccountsViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel(),
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
    val validationError by viewModel.validationError.collectAsStateWithLifecycle()
    val accountsResult by accountsViewModel.accounts.collectAsStateWithLifecycle()

    var showNumpad by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    KMPBackHandler(enabled = showNumpad) {
        showNumpad = false
    }

    val initialFormState = remember(budgetId, selectedBudgetResult, accountsResult, allCategories) {
        computeInitialFormState(budgetId, selectedBudgetResult, accountsResult, allCategories)
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
            mainViewModel.triggerGlobalRefresh()
            delay(1000)
            onSave()
            viewModel.resetSaveState()
        }
    }

    LaunchedEffect(deleteResult) {
        if (deleteResult is Result.Success) {
            mainViewModel.triggerGlobalRefresh()
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
                        FinanceAmountHeader(
                            amount = formState.amount,
                            label = if (formState.isExpense) "Expense Budget Limit" else "Income Target Limit",
                            isIncome = !formState.isExpense,
                            themeColor = themeColor,
                            paddingValues = paddingValues,
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
                        FinanceTypeSection(
                            isIncome = !formState.isExpense,
                            onTypeChange = { viewModel.setIsExpense(!it) }
                        )

                        FinanceInputSection(
                            label = "Budget Name",
                            value = formState.name,
                            onValueChange = { viewModel.setName(it) },
                            placeholder = "Enter budget name",
                            icon = Icons.Default.Edit,
                            onFocus = { showNumpad = false }
                        )

                        AccountSelectionSection(
                            accountsResult = accountsResult,
                            selectedAccount = formState.selectedAccount,
                            onAccountSelected = { viewModel.setAccount(it) },
                            onRetry = { accountsViewModel.reloadAccounts() }
                        )

                        FinanceCategorySelection(
                            label = "Categories",
                            categories = allCategories,
                            selectedCategories = formState.selectedCategories,
                            onCategorySelectionChange = { viewModel.setCategories(it) },
                            isExpense = formState.isExpense,
                            multiSelect = true
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
            FinanceSaveButton(
                saveState = saveState,
                isFormValid = validationState is ValidationResult.Success,
                themeColor = themeColor,
                contentColor = if (formState.isExpense) Color.White else MaterialTheme.colorScheme.onTertiary,
                onSaveClick = { 
                    showNumpad = false
                    viewModel.saveBudget() 
                },
                label = "Save Budget",
                successLabel = "Saved"
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
            ConfirmationDialog(
                title = "Delete Budget?",
                message = "Are you sure you want to delete this budget? This action cannot be undone and you will lose all tracking for this period.",
                confirmLabel = "Delete",
                isDestructive = true,
                onConfirm = { budgetId?.let { viewModel.removeBudget(it) } },
                onDismiss = { showDeleteDialog = false }
            )
        }

        // Custom Numpad
        AnimatedVisibility(
            visible = showNumpad,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            FinanceNumpad(
                onNumberClick = { num ->
                    if (num == "." && formState.amount.contains(".")) return@FinanceNumpad
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
                onDismiss = { viewModel.resetSaveState() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = paddingValues.calculateBottomPadding() + 84.dp)
            )
        }

        if (validationError != null) {
            MaterialToast(
                message = validationError!!,
                isError = true,
                onDismiss = { viewModel.clearValidationError() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = paddingValues.calculateBottomPadding() + 84.dp)
            )
        }
    }
}

private fun computeInitialFormState(
    budgetId: String?,
    selectedBudgetResult: Result<BudgetWithStatus>?,
    accountsResult: Result<List<Account>>,
    allCategories: List<Category>
): BudgetFormState {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val isAccountsSuccess = accountsResult is Result.Success
    val accountsData = if (isAccountsSuccess) accountsResult.data else emptyList()
    val firstExpenseCategory = allCategories.firstOrNull { it.isExpense } ?: Category.expenseCategories.firstOrNull()

    return if (budgetId == null) {
        BudgetFormState(
            name = "",
            amount = "",
            selectedCategories = emptySet(),
            isExpense = true,
            startDate = today,
            endDate = today.plus(DatePeriod(months = 1)),
            selectedAccount = null
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
                    selectedCategories = budget.categories.map { budgetCat ->
                        allCategories.find { it.name == budgetCat.name && it.isExpense == budgetCat.isExpense } ?: budgetCat
                    }.toSet(),
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
