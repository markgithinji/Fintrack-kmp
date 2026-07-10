package com.fintrack.shared.feature.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.settings.domain.datasource.SettingsDataSource
import com.fintrack.shared.feature.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val settingsDataSource: SettingsDataSource,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _selectedAccountId = MutableStateFlow<String?>(null)
    val selectedAccountId: StateFlow<String?> = _selectedAccountId.asStateFlow()

    // Global states that multiple screens care about
    val isBalanceHidden = settingsDataSource.isBalanceHidden
    val currency = settingsDataSource.currency
    val userProfile = userRepository.getUserProfile()

    init {
        // Initialize selected account from default settings if not already set
        viewModelScope.launch {
            settingsDataSource.defaultAccountId.collect { id ->
                if (_selectedAccountId.value == null && id != null) {
                    _selectedAccountId.value = id
                }
            }
        }
    }

    fun onAccountSelected(id: String?) {
        _selectedAccountId.value = id
    }
}
