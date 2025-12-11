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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StatisticsViewModel(private val repo: SummaryRepository) : ViewModel() {

    private val _highlights = MutableStateFlow<Result<StatisticsSummary>>(Result.Loading)
    val highlights: StateFlow<Result<StatisticsSummary>> = _highlights

    private val _distribution = MutableStateFlow<Result<DistributionSummary>>(Result.Loading)
    val distribution: StateFlow<Result<DistributionSummary>> = _distribution

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
        reloadDistributionForCurrentSelection(accountId)
    }

    fun onPeriodChanged(period: Period, accountId: String? = null) {
        _selectedPeriod.value = period
        reloadDistributionForCurrentSelection(accountId)
    }

    fun reloadDistributionForCurrentSelection(accountId: String? = null) {
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

    fun loadTransactionCounts(accountId: String, isIncome: Boolean? = null) {
        viewModelScope.launch {
            _transactionCounts.value = Result.Loading
            _transactionCounts.value = repo.getTransactionCounts(accountId, isIncome)
        }
    }
}