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

    private val _selectedPeriod = MutableStateFlow<Period?>(null)
    val selectedPeriod: StateFlow<Period?> = _selectedPeriod

    // --- Load highlights summary ---
    fun loadHighlights() {
        viewModelScope.launch {
            _highlights.value = Result.Loading
            _highlights.value = repo.getHighlightsSummary()
        }
    }

    // --- Load distribution summary ---
    private fun loadDistribution(
        weekOrMonthCode: String,
        type: TransactionType,
        start: String? = null,
        end: String? = null
    ) {
        viewModelScope.launch {
            _distribution.value = Result.Loading
            _distribution.value = repo.getDistributionSummary(
                weekOrMonthCode = weekOrMonthCode,
                type = type.apiName,
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

                if (_selectedPeriod.value == null && weeks.isNotEmpty()) {
                    _selectedPeriod.value = Period.Week(weeks.first())
                    reloadDistributionForCurrentSelection()
                }
            } catch (e: Exception) {
                _availableWeeks.value = emptyList()
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

                if (_selectedPeriod.value == null && months.isNotEmpty()) {
                    _selectedPeriod.value = Period.Month(months.first())
                    reloadDistributionForCurrentSelection()
                }
            } catch (e: Exception) {
                _availableMonths.value = emptyList()
            }
        }
    }

    // --- UI state helpers ---
    fun onTabChanged(tab: TabType) {
        _selectedTab.value = tab
        reloadDistributionForCurrentSelection()
    }

    fun onWeekChanged(week: String) {
        _selectedPeriod.value = Period.Week(week)
        reloadDistributionForCurrentSelection()
    }

    fun onMonthChanged(month: String) {
        _selectedPeriod.value = Period.Month(month)
        reloadDistributionForCurrentSelection()
    }

    fun onPeriodChanged(period: Period) {
        _selectedPeriod.value = period
        reloadDistributionForCurrentSelection()
    }

    private fun reloadDistributionForCurrentSelection() {
        val type = when (_selectedTab.value) {
            is TabType.Income -> TransactionType.Income
            is TabType.Expense -> TransactionType.Expense
        }

        when (val period = _selectedPeriod.value) {
            is Period.Week -> loadDistribution(period.code, type)
            is Period.Month -> loadDistribution(period.code, type)
            null -> Unit
        }
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
}

sealed class TransactionType(val apiName: String) {
    object Income : TransactionType("income")
    object Expense : TransactionType("expense")
}