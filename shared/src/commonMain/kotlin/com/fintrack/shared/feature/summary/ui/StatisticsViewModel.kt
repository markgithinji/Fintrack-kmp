package com.fintrack.shared.feature.summary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.summary.domain.model.CategoryComparison
import com.fintrack.shared.feature.summary.domain.model.DistributionSummary
import com.fintrack.shared.feature.summary.domain.model.OverviewSummary
import com.fintrack.shared.feature.summary.domain.model.Period
import com.fintrack.shared.feature.summary.domain.model.StatisticsSummary
import com.fintrack.shared.feature.summary.domain.model.TabType
import com.fintrack.shared.feature.summary.domain.model.TransactionCountSummary
import com.fintrack.shared.feature.summary.domain.model.TransactionType
import com.fintrack.shared.feature.summary.domain.repository.SummaryRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StatisticsViewModel(private val repo: SummaryRepository) : ViewModel() {

    private val _highlights = MutableStateFlow<Result<StatisticsSummary>>(Result.Loading)
    val highlights: StateFlow<Result<StatisticsSummary>> = _highlights

    // Store separate distribution results for Income and Expense
    private val _incomeDistribution = MutableStateFlow<Result<DistributionSummary>>(Result.Loading)
    private val _expenseDistribution = MutableStateFlow<Result<DistributionSummary>>(Result.Loading)

    private val _availableWeeks = MutableStateFlow<List<String>>(emptyList())
    val availableWeeks: StateFlow<List<String>> = _availableWeeks

    private val _availableMonths = MutableStateFlow<List<String>>(emptyList())
    val availableMonths: StateFlow<List<String>> = _availableMonths

    private val _availableYears = MutableStateFlow<List<String>>(emptyList())
    val availableYears: StateFlow<List<String>> = _availableYears

    private val _overview = MutableStateFlow<Result<OverviewSummary>>(Result.Loading)
    val overview: StateFlow<Result<OverviewSummary>> = _overview

    private val _selectedTab = MutableStateFlow<TabType>(TabType.Expense)
    val selectedTab: StateFlow<TabType> = _selectedTab

    private val _selectedPeriod = MutableStateFlow<Period?>(null)
    val selectedPeriod: StateFlow<Period?> = _selectedPeriod

    private val _categoryComparisons =
        MutableStateFlow<Result<List<CategoryComparison>>>(Result.Loading)
    val categoryComparisons: StateFlow<Result<List<CategoryComparison>>> = _categoryComparisons

    private val _transactionCounts =
        MutableStateFlow<Result<TransactionCountSummary>>(Result.Loading)
    val transactionCounts: StateFlow<Result<TransactionCountSummary>> = _transactionCounts

    // Simple distribution flow that switches between income and expense
    val distribution: StateFlow<Result<DistributionSummary>> =
        combine(
            selectedTab,
            _incomeDistribution,
            _expenseDistribution
        ) { tab, income, expense ->
            when (tab) {
                is TabType.Income -> income
                is TabType.Expense -> expense
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Result.Loading
        )

    fun loadHighlights(accountId: String? = null) {
        viewModelScope.launch {
            _highlights.value = Result.Loading
            _highlights.value = repo.getHighlightsSummary(accountId)
        }
    }

    private fun loadDistribution(
        weekOrMonthCode: String,
        type: TransactionType,
        start: String? = null,
        end: String? = null,
        accountId: String? = null
    ) {
        viewModelScope.launch {
            val targetFlow = when (type) {
                TransactionType.Income -> _incomeDistribution
                TransactionType.Expense -> _expenseDistribution
            }

            targetFlow.value = Result.Loading
            targetFlow.value = repo.getDistributionSummary(
                weekOrMonthCode = weekOrMonthCode,
                type = type.apiName,
                start = start,
                end = end,
                accountId = accountId
            )
        }
    }

    fun loadAvailablePeriods(accountId: String? = null) {
        viewModelScope.launch {
            try {
                val weeksDeferred = viewModelScope.async {
                    val result = repo.getAvailableWeeks(accountId)
                    if (result is Result.Success) result.data.weeks else emptyList()
                }

                val monthsDeferred = viewModelScope.async {
                    val result = repo.getAvailableMonths(accountId)
                    if (result is Result.Success) result.data.months else emptyList()
                }

                val yearsDeferred = viewModelScope.async {
                    val result = repo.getAvailableYears(accountId)
                    if (result is Result.Success) result.data.years else emptyList()
                }

                // Wait for all results
                _availableWeeks.value = weeksDeferred.await()
                _availableMonths.value = monthsDeferred.await()
                _availableYears.value = yearsDeferred.await()

                // --- Pick initial selection ---
                _selectedPeriod.value = when {
                    _availableWeeks.value.isNotEmpty() -> Period.Week(_availableWeeks.value.first())
                    _availableMonths.value.isNotEmpty() -> Period.Month(_availableMonths.value.first())
                    _availableYears.value.isNotEmpty() -> Period.Year(_availableYears.value.first())
                    else -> null
                }

                // Load BOTH income and expense data for the initial period
                reloadDistributionForCurrentSelection(accountId)

            } catch (e: Exception) {
                _availableWeeks.value = emptyList()
                _availableMonths.value = emptyList()
                _availableYears.value = emptyList()
            }
        }
    }

    fun loadOverview(accountId: String? = null) {
        viewModelScope.launch {
            _overview.value = Result.Loading
            _overview.value = repo.getOverviewSummary(accountId)
        }
    }

    fun loadCategoryComparisons(accountId: String? = null) {
        viewModelScope.launch {
            _categoryComparisons.value = Result.Loading
            _categoryComparisons.value = repo.getCategoryComparisons(accountId)
        }
    }

    fun onTabChanged(tab: TabType, accountId: String? = null) {
        _selectedTab.value = tab
    }

    fun onPeriodChanged(period: Period, accountId: String? = null) {
        _selectedPeriod.value = period
        // Load BOTH income and expense data for the new period
        reloadDistributionForCurrentSelection(accountId)
    }

    fun reloadDistributionForCurrentSelection(accountId: String? = null) {
        val currentPeriod = _selectedPeriod.value
        if (currentPeriod != null) {
            val periodCode = when (currentPeriod) {
                is Period.Week -> currentPeriod.code
                is Period.Month -> currentPeriod.code
                is Period.Year -> currentPeriod.code
            }

            // Load BOTH income and expense data for this period
            loadDistribution(periodCode, TransactionType.Income, accountId = accountId)
            loadDistribution(periodCode, TransactionType.Expense, accountId = accountId)
        }
    }

    fun loadTransactionCounts(accountId: String, isIncome: Boolean? = null) {
        viewModelScope.launch {
            _transactionCounts.value = Result.Loading
            _transactionCounts.value = repo.getTransactionCounts(accountId, isIncome)
        }
    }
}