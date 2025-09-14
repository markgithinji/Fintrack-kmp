package com.fintrack.shared.feature.transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.transaction.data.DistributionSummary
import com.fintrack.shared.feature.transaction.data.HighlightsSummary
import com.fintrack.shared.feature.transaction.data.Result
import com.fintrack.shared.feature.transaction.data.TransactionApi
import com.fintrack.shared.feature.transaction.data.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StatisticsViewModel : ViewModel() {
    private val repo = TransactionRepository(TransactionApi())

    // --- Highlights state ---
    private val _highlights = MutableStateFlow<Result<HighlightsSummary>>(Result.Loading)
    val highlights: StateFlow<Result<HighlightsSummary>> = _highlights

    // --- Distribution state ---
    private val _distribution = MutableStateFlow<Result<DistributionSummary>>(Result.Loading)
    val distribution: StateFlow<Result<DistributionSummary>> = _distribution

    // --- Available weeks state ---
    private val _availableWeeks = MutableStateFlow<List<String>>(emptyList())
    val availableWeeks: StateFlow<List<String>> = _availableWeeks

    // --- Available months state ---
    private val _availableMonths = MutableStateFlow<List<String>>(emptyList())
    val availableMonths: StateFlow<List<String>> = _availableMonths

    // --- UI state for StatisticsScreen ---
    private val _selectedTab = MutableStateFlow<TabType>(TabType.Expense)
    val selectedTab: StateFlow<TabType> = _selectedTab

    private val _selectedPeriod = MutableStateFlow(Period.WEEK) // default
    val selectedPeriod: StateFlow<Period> = _selectedPeriod

    private val _selectedWeek = MutableStateFlow<String?>(null)
    val selectedWeek: StateFlow<String?> = _selectedWeek

    private val _selectedMonth = MutableStateFlow<String?>(null)
    val selectedMonth: StateFlow<String?> = _selectedMonth

    // --- Load highlights summary ---
    fun loadHighlights() {
        viewModelScope.launch {
            _highlights.value = Result.Loading
            _highlights.value = repo.getHighlightsSummary()
        }
    }

    // --- Load distribution summary ---
    fun loadDistribution(
        weekOrMonthCode: String,
        type: String? = null,
        start: String? = null,
        end: String? = null
    ) {
        viewModelScope.launch {
            _distribution.value = Result.Loading
            _distribution.value = repo.getDistributionSummary(
                weekOrMonthCode = weekOrMonthCode,
                type = type,
                start = start,
                end = end
            )
        }
    }

    // --- Available weeks ---
    fun loadAvailableWeeks() {
        viewModelScope.launch {
            try {
                val result = repo.getAvailableWeeks()
                val weeks = if (result is Result.Success) result.data else emptyList()
                _availableWeeks.value = weeks

                _selectedWeek.value = weeks.firstOrNull()
                if (_selectedPeriod.value == Period.WEEK) {
                    reloadDistributionForCurrentSelection()
                }
            } catch (e: Exception) {
                _availableWeeks.value = emptyList()
                _selectedWeek.value = null
            }
        }
    }

    // --- Available months ---
    fun loadAvailableMonths() {
        viewModelScope.launch {
            try {
                val result = repo.getAvailableMonths()
                val months = if (result is Result.Success) result.data.months else emptyList()
                _availableMonths.value = months

                _selectedMonth.value = months.firstOrNull()
                if (_selectedPeriod.value == Period.MONTH) {
                    reloadDistributionForCurrentSelection()
                }
            } catch (e: Exception) {
                _availableMonths.value = emptyList()
                _selectedMonth.value = null
            }
        }
    }

    // --- UI state helpers ---
    fun onTabChanged(tab: TabType) {
        _selectedTab.value = tab
        reloadDistributionForCurrentSelection()
    }

    fun onWeekChanged(week: String) {
        _selectedWeek.value = week
        _selectedPeriod.value = Period.WEEK
        reloadDistributionForCurrentSelection()
    }

    fun onMonthChanged(month: String) {
        _selectedMonth.value = month
        _selectedPeriod.value = Period.MONTH
        reloadDistributionForCurrentSelection()
    }

    fun onPeriodChanged(period: Period) {
        _selectedPeriod.value = period

        when (period) {
            Period.WEEK -> {
                if (_selectedWeek.value == null && _availableWeeks.value.isNotEmpty()) {
                    _selectedWeek.value = _availableWeeks.value.first()
                }
            }
            Period.MONTH -> {
                if (_selectedMonth.value == null && _availableMonths.value.isNotEmpty()) {
                    _selectedMonth.value = _availableMonths.value.first()
                }
            }
        }

        reloadDistributionForCurrentSelection()
    }

    private fun reloadDistributionForCurrentSelection() {
        val type = when (_selectedTab.value) {
            is TabType.Income -> "income"
            is TabType.Expense -> "expense"
        }

        when (_selectedPeriod.value) {
            Period.WEEK -> _selectedWeek.value?.let {
                loadDistribution(weekOrMonthCode = it, type = type)
            }
            Period.MONTH -> _selectedMonth.value?.let {
                loadDistribution(weekOrMonthCode = it, type = type)
            }
        }
    }
}


/** Sealed class for tab selection */
sealed class TabType(val displayName: String) {
    data object Income : TabType("Income")
    data object Expense : TabType("Expenses")
}

/** Enum for period selection */
enum class Period { WEEK, MONTH }
