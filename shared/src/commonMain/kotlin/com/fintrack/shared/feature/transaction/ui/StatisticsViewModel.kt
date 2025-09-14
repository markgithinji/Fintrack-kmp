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

    // --- UI state for StatisticsScreen ---
    private val _selectedTab = MutableStateFlow(TabType.EXPENSE)
    val selectedTab: StateFlow<TabType> = _selectedTab

    private val _selectedPeriod = MutableStateFlow(Period.WEEK)
    val selectedPeriod: StateFlow<Period> = _selectedPeriod

    private val _selectedWeek = MutableStateFlow<String?>(null)
    val selectedWeek: StateFlow<String?> = _selectedWeek

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
            _distribution.value = repo.getDistributionSummary(weekOrMonthCode, type, start, end)
        }
    }

    fun loadAvailableWeeks() {
        viewModelScope.launch {
            try {
                val result = repo.getAvailableWeeks()
                val weeks = if (result is Result.Success) result.data else emptyList()
                _availableWeeks.value = weeks

                // Always set the first week as default if list is not empty
                _selectedWeek.value = weeks.firstOrNull()

                // Load distribution for the default week immediately
                _selectedWeek.value?.let { week ->
                    reloadDistributionForCurrentSelection()
                }
            } catch (e: Exception) {
                _availableWeeks.value = emptyList()
                _selectedWeek.value = null
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
        reloadDistributionForCurrentSelection()
    }

    private fun reloadDistributionForCurrentSelection() {
        val week = _selectedWeek.value
        val tab = _selectedTab.value
        val type = when (tab) {
            TabType.INCOME -> "income"
            TabType.EXPENSE -> "expense"
        }

        if (week != null) {
            loadDistribution(weekOrMonthCode = week, type = type)
        }
    }
}

/** Enums for tab and period selection */
enum class TabType { INCOME, EXPENSE }
enum class Period { WEEK, MONTH }
