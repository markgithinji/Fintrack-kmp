package com.fintrack.shared.feature.summary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.summary.domain.model.CategoryComparison
import com.fintrack.shared.feature.summary.domain.model.CategoryComparisonSummary
import com.fintrack.shared.feature.summary.domain.model.DistributionSummary
import com.fintrack.shared.feature.summary.domain.model.OverviewSummary
import com.fintrack.shared.feature.summary.domain.model.Period
import com.fintrack.shared.feature.summary.domain.model.StatisticsSummary
import com.fintrack.shared.feature.summary.domain.model.TabType
import com.fintrack.shared.feature.summary.domain.model.TransactionCountSummary
import com.fintrack.shared.feature.summary.domain.model.TransactionType
import com.fintrack.shared.feature.summary.domain.repository.SummaryRepository
import com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class StatisticsViewModel(
    private val repo: SummaryRepository,
    private val transactionRepository: com.fintrack.shared.feature.transaction.domain.repository.TransactionRepository
) : ViewModel() {

    private val _highlights = MutableStateFlow<Result<StatisticsSummary>>(Result.Loading)
    val highlights: StateFlow<Result<StatisticsSummary>> = _highlights

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
        MutableStateFlow<Result<CategoryComparisonSummary>>(Result.Loading)
    val categoryComparisons: StateFlow<Result<CategoryComparisonSummary>> = _categoryComparisons

    private val _categoryComparisonPeriod = MutableStateFlow<String?>(null)
    val categoryComparisonPeriod: StateFlow<String?> = _categoryComparisonPeriod

    private val _transactionCounts =
        MutableStateFlow<Result<TransactionCountSummary>>(Result.Loading)
    val transactionCounts: StateFlow<Result<TransactionCountSummary>> = _transactionCounts

    init {
        // Observe transaction changes to refresh all statistics
        viewModelScope.launch {
            transactionRepository.dataChangedEvent.collect {
                // Refresh everything currently loaded
                lastHighlightsAccountId?.let { id -> loadHighlights(id, lastHighlightsPeriod, force = true) }
                lastOverviewAccountId?.let { id -> loadOverview(id, force = true) }
                lastCategoryComparisonAccountId?.let { id -> loadCategoryComparisons(id, lastCategoryComparisonPeriod, force = true) }
                lastAvailablePeriodsAccountId?.let { id -> loadAvailablePeriods(id, force = true) }
                
                // If counting transactions, refresh that too
                lastTransactionCountsAccountId?.let { id -> 
                    loadTransactionCounts(id, lastTransactionCountsIsIncome, lastTransactionCountsCategory, 
                        lastTransactionCountsStart, lastTransactionCountsEnd, lastTransactionCountsHasCost, force = true) 
                }
            }
        }
    }

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

    private var lastHighlightsAccountId: String? = null
    private var lastHighlightsPeriod: String? = null
    private var highlightsJob: Job? = null

    fun loadHighlights(accountId: String? = null, period: String? = null, force: Boolean = false) {
        if (period == null && lastHighlightsPeriod != null && !force) {
            return
        }

        val paramsChanged = lastHighlightsAccountId != accountId || lastHighlightsPeriod != period
        val current = _highlights.value
        
        if (!force && current is Result.Success && !paramsChanged) {
            return
        }

        highlightsJob?.cancel()
        highlightsJob = viewModelScope.launch {
            if (current !is Result.Success || paramsChanged) {
                _highlights.value = Result.Loading
            }
            
            lastHighlightsAccountId = accountId
            lastHighlightsPeriod = period
            
            _highlights.value = repo.getHighlightsSummary(accountId, period)
        }
    }

    private var lastIncomeDistributionParams: String? = null
    private var lastExpenseDistributionParams: String? = null
    private var incomeDistributionJob: Job? = null
    private var expenseDistributionJob: Job? = null

    private fun loadDistribution(
        weekOrMonthCode: String,
        type: TransactionType,
        start: String? = null,
        end: String? = null,
        accountId: String? = null,
        force: Boolean = false
    ) {
        val paramKey = "$weekOrMonthCode|$start|$end|$accountId"
        val targetFlow = when (type) {
            TransactionType.Income -> _incomeDistribution
            TransactionType.Expense -> _expenseDistribution
        }
        val lastParams = when (type) {
            TransactionType.Income -> lastIncomeDistributionParams
            TransactionType.Expense -> lastExpenseDistributionParams
        }

        val paramsChanged = lastParams != paramKey
        val current = targetFlow.value
        
        if (!force && current is Result.Success && !paramsChanged) return

        val job = viewModelScope.launch {
            when (type) {
                TransactionType.Income -> lastIncomeDistributionParams = paramKey
                TransactionType.Expense -> lastExpenseDistributionParams = paramKey
            }

            if (current !is Result.Success || paramsChanged) {
                targetFlow.value = Result.Loading
            }
            
            targetFlow.value = repo.getDistributionSummary(
                weekOrMonthCode = weekOrMonthCode,
                type = type.apiName,
                start = start,
                end = end,
                accountId = accountId
            )
        }

        when (type) {
            TransactionType.Income -> {
                incomeDistributionJob?.cancel()
                incomeDistributionJob = job
            }
            TransactionType.Expense -> {
                expenseDistributionJob?.cancel()
                expenseDistributionJob = job
            }
        }
    }

    private var lastAvailablePeriodsAccountId: String? = null
    fun loadAvailablePeriods(accountId: String? = null, force: Boolean = false) {
        if (!force && lastAvailablePeriodsAccountId == accountId && (_availableWeeks.value.isNotEmpty() || _availableMonths.value.isNotEmpty() || _availableYears.value.isNotEmpty())) {
            reloadDistributionForCurrentSelection(accountId, force = false)
            return
        }

        viewModelScope.launch {
            try {
                lastAvailablePeriodsAccountId = accountId
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
                val weeks = weeksDeferred.await()
                val months = monthsDeferred.await()
                val years = yearsDeferred.await()

                _availableWeeks.value = weeks
                _availableMonths.value = months
                _availableYears.value = years

                // Pick initial selection only if none exists
                if (_selectedPeriod.value == null) {
                    _selectedPeriod.value = when {
                        weeks.isNotEmpty() -> Period.Week(weeks.first())
                        months.isNotEmpty() -> Period.Month(months.first())
                        years.isNotEmpty() -> Period.Year(years.first())
                        else -> null
                    }
                }

                // Load BOTH income and expense data for the selection
                reloadDistributionForCurrentSelection(accountId, force = force)
            } catch (e: Exception) {
                _availableWeeks.value = emptyList()
                _availableMonths.value = emptyList()
                _availableYears.value = emptyList()
            }
        }
    }

    private var lastOverviewAccountId: String? = null
    fun loadOverview(accountId: String? = null, force: Boolean = false) {
        val paramsChanged = lastOverviewAccountId != accountId
        val current = _overview.value
        
        if (!force && current is Result.Success && !paramsChanged) return
        
        viewModelScope.launch {
            if (current !is Result.Success || paramsChanged) {
                _overview.value = Result.Loading
            }
            lastOverviewAccountId = accountId
            _overview.value = repo.getOverviewSummary(accountId)
        }
    }

    private var lastCategoryComparisonAccountId: String? = null
    private var lastCategoryComparisonPeriod: String? = null
    fun loadCategoryComparisons(
        accountId: String? = null,
        period: String? = null,
        force: Boolean = false
    ) {
        val paramsChanged = lastCategoryComparisonAccountId != accountId || lastCategoryComparisonPeriod != period
        val current = _categoryComparisons.value
        
        if (!force && current is Result.Success && !paramsChanged) return

        viewModelScope.launch {
            if (current !is Result.Success || paramsChanged) {
                _categoryComparisons.value = Result.Loading
            }

            lastCategoryComparisonAccountId = accountId
            lastCategoryComparisonPeriod = period

            // If no period provided, try to find the last active month
            val targetPeriod = period ?: if (_availableMonths.value.isNotEmpty()) {
                _availableMonths.value.first()
            } else {
                // Fetch available months if they aren't loaded yet
                val result = repo.getAvailableMonths(accountId)
                if (result is Result.Success && result.data.months.isNotEmpty()) {
                    val months = result.data.months
                    _availableMonths.value = months
                    months.first()
                } else {
                    null
                }
            }

            val result = repo.getCategoryComparisons(accountId, targetPeriod)

            _categoryComparisons.value = result
            if (result is Result.Success) {
                _categoryComparisonPeriod.value = result.data.period
            }
        }
    }

    fun onTabChanged(tab: TabType, accountId: String? = null) {
        _selectedTab.value = tab
    }

    fun onPeriodChanged(period: Period, accountId: String? = null) {
        _selectedPeriod.value = period
        // Load BOTH income and expense data for the new period
        reloadDistributionForCurrentSelection(accountId, force = true)
    }

    fun reloadDistributionForCurrentSelection(accountId: String? = null, force: Boolean = false) {
        val currentPeriod = _selectedPeriod.value
        if (currentPeriod != null) {
            val periodCode = when (currentPeriod) {
                is Period.Week -> currentPeriod.code
                is Period.Month -> currentPeriod.code
                is Period.Year -> currentPeriod.code
            }

            // Load BOTH income and expense data for this period
            loadDistribution(periodCode, TransactionType.Income, accountId = accountId, force = force)
            loadDistribution(periodCode, TransactionType.Expense, accountId = accountId, force = force)

            val yearCode = when (currentPeriod) {
                is Period.Week -> currentPeriod.code.substringBefore("-")
                is Period.Month -> currentPeriod.code.substringBefore("-")
                is Period.Year -> currentPeriod.code
            }
            loadHighlights(accountId = accountId, period = yearCode, force = force)
            
            // Also load category comparisons if the period is a month
            if (currentPeriod is Period.Month) {
                loadCategoryComparisons(accountId = accountId, period = periodCode, force = force)
            }
        } else {
            // Clear distributions if no period is selected (e.g., after data deletion)
            lastIncomeDistributionParams = null
            lastExpenseDistributionParams = null
            _incomeDistribution.value = Result.Success(
                DistributionSummary(
                    period = "",
                    incomeCategories = emptyList(),
                    expenseCategories = emptyList()
                )
            )
            _expenseDistribution.value = Result.Success(
                DistributionSummary(
                    period = "",
                    incomeCategories = emptyList(),
                    expenseCategories = emptyList()
                )
            )
        }
    }

    private var lastTransactionCountsAccountId: String? = null
    private var lastTransactionCountsIsIncome: Boolean? = null
    private var lastTransactionCountsCategory: String? = null
    private var lastTransactionCountsStart: String? = null
    private var lastTransactionCountsEnd: String? = null
    private var lastTransactionCountsHasCost: Boolean? = null

    fun loadTransactionCounts(
        accountId: String,
        isIncome: Boolean? = null,
        category: String? = null,
        start: String? = null,
        end: String? = null,
        hasCost: Boolean? = null,
        force: Boolean = false
    ) {
        if (!force && _transactionCounts.value is Result.Success &&
            lastTransactionCountsAccountId == accountId &&
            lastTransactionCountsIsIncome == isIncome &&
            lastTransactionCountsCategory == category &&
            lastTransactionCountsStart == start &&
            lastTransactionCountsEnd == end &&
            lastTransactionCountsHasCost == hasCost
        ) return

        viewModelScope.launch {
            _transactionCounts.value = Result.Loading
            lastTransactionCountsAccountId = accountId
            lastTransactionCountsIsIncome = isIncome
            lastTransactionCountsCategory = category
            lastTransactionCountsStart = start
            lastTransactionCountsEnd = end
            lastTransactionCountsHasCost = hasCost
            _transactionCounts.value = repo.getTransactionCounts(accountId, isIncome, category, start, end, hasCost)
        }
    }
}