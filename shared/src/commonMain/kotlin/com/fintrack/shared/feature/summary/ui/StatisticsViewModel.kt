package com.fintrack.shared.feature.summary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.core.util.GlobalRefreshManager
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
    private val transactionRepo: TransactionRepository,
    private val globalRefreshManager: GlobalRefreshManager
) : ViewModel() {

    init {
        viewModelScope.launch {
            merge(globalRefreshManager.refreshEvent, transactionRepo.refreshSignal)
                .debounce(500)
                .collect {
                    // Force reload all data when global refresh is triggered
                    loadHighlights(lastHighlightsAccountId, lastHighlightsPeriod, force = true)
                    loadAvailablePeriods(lastAvailablePeriodsAccountId, force = true)
                    loadOverview(lastOverviewAccountId, force = true)
                    loadCategoryComparisons(lastCategoryComparisonAccountId, force = true)
                    
                    lastTransactionCountsAccountId?.let { accountId ->
                        loadTransactionCounts(
                            accountId = accountId,
                            isIncome = lastTransactionCountsIsIncome,
                            category = lastTransactionCountsCategory,
                            start = lastTransactionCountsStart,
                            end = lastTransactionCountsEnd,
                            force = true
                        )
                    }
                }
        }
    }

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
        MutableStateFlow<Result<CategoryComparisonSummary>>(Result.Loading)
    val categoryComparisons: StateFlow<Result<CategoryComparisonSummary>> = _categoryComparisons

    private val _categoryComparisonPeriod = MutableStateFlow<String?>(null)
    val categoryComparisonPeriod: StateFlow<String?> = _categoryComparisonPeriod

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

    private var lastHighlightsAccountId: String? = null
    private var lastHighlightsPeriod: String? = null
    private var highlightsJob: kotlinx.coroutines.Job? = null

    fun loadHighlights(accountId: String? = null, period: String? = null, force: Boolean = false) {
        println("StatisticsViewModel: loadHighlights called. accountId=$accountId, period=$period, force=$force")
        
        // Prevent overwriting a specific period with a null period (all-time) during initial loads
        if (period == null && lastHighlightsPeriod != null && !force) {
            println("StatisticsViewModel: loadHighlights ignored null period because we already have a specific period: $lastHighlightsPeriod")
            return
        }

        if (!force && _highlights.value is Result.Success && 
            lastHighlightsAccountId == accountId && 
            lastHighlightsPeriod == period) {
            println("StatisticsViewModel: loadHighlights skipped (already loaded)")
            return
        }

        highlightsJob?.cancel()
        highlightsJob = viewModelScope.launch {
            // Only show loading if we don't have success data yet, to prevent flicker
            if (_highlights.value !is Result.Success) {
                println("StatisticsViewModel: Setting Highlights to Loading")
                _highlights.value = Result.Loading
            }
            
            lastHighlightsAccountId = accountId
            lastHighlightsPeriod = period
            
            println("StatisticsViewModel: Fetching highlights for period: $period")
            val result = repo.getHighlightsSummary(accountId, period)
            
            if (result is Result.Success) {
                println("StatisticsViewModel: Successfully fetched highlights for $period")
            } else if (result is Result.Error) {
                println("StatisticsViewModel: Error fetching highlights: ${result.exception.message}")
            }
            
            _highlights.value = result
        }
    }

    private var lastIncomeDistributionParams: String? = null
    private var lastExpenseDistributionParams: String? = null
    private var incomeDistributionJob: kotlinx.coroutines.Job? = null
    private var expenseDistributionJob: kotlinx.coroutines.Job? = null

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

        if (!force && targetFlow.value is Result.Success && lastParams == paramKey) return

        val job = viewModelScope.launch {
            when (type) {
                TransactionType.Income -> lastIncomeDistributionParams = paramKey
                TransactionType.Expense -> lastExpenseDistributionParams = paramKey
            }

            if (targetFlow.value !is Result.Success) {
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
            // Already populated, just ensure distribution is ready
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

                // --- Pick initial selection only if none exists ---
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
        if (!force && _overview.value is Result.Success && lastOverviewAccountId == accountId) return
        
        viewModelScope.launch {
            if (_overview.value !is Result.Success) {
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
        if (!force && _categoryComparisons.value is Result.Success &&
            lastCategoryComparisonAccountId == accountId &&
            lastCategoryComparisonPeriod == period
        ) return

        viewModelScope.launch {
            if (_categoryComparisons.value !is Result.Success) {
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