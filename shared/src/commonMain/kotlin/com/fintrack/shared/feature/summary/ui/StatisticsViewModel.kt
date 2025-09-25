package com.fintrack.shared.feature.summary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.summary.data.repository.SummaryRepository
import com.fintrack.shared.feature.summary.domain.CategoryComparison
import com.fintrack.shared.feature.summary.domain.DistributionSummary
import com.fintrack.shared.feature.summary.domain.StatisticsSummary
import com.fintrack.shared.feature.summary.domain.OverviewSummary
import com.fintrack.shared.feature.summary.domain.TransactionCountSummary
import com.fintrack.shared.feature.transaction.data.TransactionApi
import com.fintrack.shared.feature.transaction.data.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StatisticsViewModel : ViewModel() {
    private val repo = SummaryRepository()

    // --- Highlights state ---
    private val _highlights = MutableStateFlow<Result<StatisticsSummary>>(Result.Loading)
    val highlights: StateFlow<Result<StatisticsSummary>> = _highlights

    // --- Distribution state ---
    private val _distribution = MutableStateFlow<Result<DistributionSummary>>(Result.Loading)
    val distribution: StateFlow<Result<DistributionSummary>> = _distribution

    // --- Available weeks state ---
    private val _availableWeeks = MutableStateFlow<List<String>>(emptyList())
    val availableWeeks: StateFlow<List<String>> = _availableWeeks

    // --- Available months state ---
    private val _availableMonths = MutableStateFlow<List<String>>(emptyList())
    val availableMonths: StateFlow<List<String>> = _availableMonths

    // --- Available years state ---
    private val _availableYears = MutableStateFlow<List<String>>(emptyList())
    val availableYears: StateFlow<List<String>> = _availableYears

    // --- Overview state ---
    private val _overview = MutableStateFlow<Result<OverviewSummary>>(Result.Loading)
    val overview: StateFlow<Result<OverviewSummary>> = _overview

    // --- UI state for StatisticsScreen ---
    private val _selectedTab = MutableStateFlow<TabType>(TabType.Expense)
    val selectedTab: StateFlow<TabType> = _selectedTab

    private val _selectedPeriod = MutableStateFlow<Period?>(null)
    val selectedPeriod: StateFlow<Period?> = _selectedPeriod

    private val _categoryComparisons = MutableStateFlow<Result<List<CategoryComparison>>?>(null)
    val categoryComparisons: StateFlow<Result<List<CategoryComparison>>?> = _categoryComparisons

    private val _transactionCounts = MutableStateFlow<Result<TransactionCountSummary>>(Result.Loading)
    val transactionCounts: StateFlow<Result<TransactionCountSummary>> = _transactionCounts

    // --- Load highlights summary for optional account ---
    fun loadHighlights(accountId: Int? = null) {
        viewModelScope.launch {
            _highlights.value = Result.Loading
            _highlights.value = repo.getHighlightsSummary(accountId)
        }
    }

    // --- Load distribution summary for optional account ---
    private fun loadDistribution(
        weekOrMonthCode: String,
        type: TransactionType,
        start: String? = null,
        end: String? = null,
        accountId: Int? = null
    ) {
        viewModelScope.launch {
            _distribution.value = Result.Loading
            _distribution.value = repo.getDistributionSummary(
                weekOrMonthCode = weekOrMonthCode,
                type = type.apiName,
                start = start,
                end = end,
                accountId = accountId
            )
        }
    }

    fun loadAvailablePeriods(accountId: Int? = null) {
        viewModelScope.launch {
            try {
                // --- Weeks ---
                val weeksResult = repo.getAvailableWeeks(accountId)
                _availableWeeks.value = if (weeksResult is Result.Success) weeksResult.data.weeks else emptyList()

                // --- Months ---
                val monthsResult = repo.getAvailableMonths(accountId)
                _availableMonths.value = if (monthsResult is Result.Success) monthsResult.data.months else emptyList()

                // --- Years ---
                val yearsResult = repo.getAvailableYears(accountId)
                _availableYears.value = if (yearsResult is Result.Success) yearsResult.data.years else emptyList()

                // --- Pick initial selection ---
                _selectedPeriod.value = when {
                    _availableWeeks.value.isNotEmpty() -> Period.Week(_availableWeeks.value.first())
                    _availableMonths.value.isNotEmpty() -> Period.Month(_availableMonths.value.first())
                    _availableYears.value.isNotEmpty() -> Period.Year(_availableYears.value.first())
                    else -> null
                }

                reloadDistributionForCurrentSelection(accountId)

            } catch (e: Exception) {
                _availableWeeks.value = emptyList()
                _availableMonths.value = emptyList()
                _availableYears.value = emptyList()
            }
        }
    }

    // --- Overview summary ---
    fun loadOverview(accountId: Int? = null) {
        viewModelScope.launch {
            _overview.value = Result.Loading
            _overview.value = repo.getOverviewSummary(accountId)
        }
    }

    // --- Category comparisons ---
    fun loadCategoryComparisons(accountId: Int? = null) {
        viewModelScope.launch {
            _categoryComparisons.value = repo.getCategoryComparisons(accountId)
        }
    }

    // --- UI state helpers ---
    fun onTabChanged(tab: TabType, accountId: Int? = null) {
        _selectedTab.value = tab
        reloadDistributionForCurrentSelection(accountId)
    }

    fun onPeriodChanged(period: Period, accountId: Int? = null) {
        _selectedPeriod.value = period
        reloadDistributionForCurrentSelection(accountId)
    }

    fun reloadDistributionForCurrentSelection(accountId: Int? = null) {
        val type = when (_selectedTab.value) {
            is TabType.Income -> TransactionType.Income
            is TabType.Expense -> TransactionType.Expense
        }

        _selectedPeriod.value?.let { period ->
            when (period) {
                is Period.Week -> loadDistribution(period.code, type, accountId = accountId)
                is Period.Month -> loadDistribution(period.code, type, accountId = accountId)
                is Period.Year -> loadDistribution(period.code, type, accountId = accountId)
            }
        }
    }

    fun loadTransactionCounts(accountId: Int) {
        viewModelScope.launch {
            _transactionCounts.value = Result.Loading
            _transactionCounts.value = repo.getTransactionCounts(accountId)
        }
    }

    /** Call this when the selected account changes */
    fun reloadAllForAccount(accountId: Int?) {
        loadHighlights(accountId)
        loadOverview(accountId)
        loadCategoryComparisons(accountId)
        loadAvailablePeriods(accountId)
        accountId?.let { loadTransactionCounts(it) }
    }
}



/** Sealed class for tab selection */
sealed class TabType(val displayName: String) {
    data object Income : TabType("Income")
    data object Expense : TabType("Expenses")
}

sealed class Period {
    data class Week(val code: String) : Period()
    data class Month(val code: String) : Period()
    data class Year(val code: String) : Period()
}

sealed class TransactionType(val apiName: String) {
    object Income : TransactionType("income")
    object Expense : TransactionType("expense")
}