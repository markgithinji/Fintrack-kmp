package com.fintrack.shared.feature.account.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.account.domain.model.AccountType
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.account.domain.usecase.GetAccountsUseCase
import com.fintrack.shared.feature.core.domain.usecase.ClearAllUserDataUseCase
import com.fintrack.shared.feature.core.util.GlobalRefreshManager
import com.fintrack.shared.feature.core.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AccountsViewModel(
    private val repo: AccountRepository,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val globalRefreshManager: GlobalRefreshManager,
    private val clearAllUserDataUseCase: ClearAllUserDataUseCase
) : ViewModel() {

    private val logger = com.fintrack.shared.feature.core.logger.KMPLogger()

    private val _accounts = MutableStateFlow<Result<List<Account>>>(Result.Loading)
    val accounts: StateFlow<Result<List<Account>>> = _accounts

    private val _selectedAccount = MutableStateFlow<Result<Account>>(Result.Loading)
    val selectedAccount: StateFlow<Result<Account>> = _selectedAccount

    private val _saveResult = MutableStateFlow<Result<Account>?>(null)
    val saveResult: StateFlow<Result<Account>?> = _saveResult

    private val _deleteResult = MutableStateFlow<Result<Unit>?>(null)
    val deleteResult: StateFlow<Result<Unit>?> = _deleteResult

    private val _clearDataResult = MutableStateFlow<Result<Unit>?>(null)
    val clearDataResult: StateFlow<Result<Unit>?> = _clearDataResult.asStateFlow()

    init {
        viewModelScope.launch {
            globalRefreshManager.refreshEvent.collect {
                reloadAccounts(force = true, showLoading = false)
            }
        }

        reloadAccounts(force = false)
    }

    // Reload all accounts
    fun reloadAccounts(force: Boolean = true, showLoading: Boolean = true) {
        val currentAccounts = _accounts.value
        if (!force && currentAccounts is Result.Success && currentAccounts.data.isNotEmpty()) return

        viewModelScope.launch {
            if (showLoading) {
                _accounts.value = Result.Loading
            }

            val result = getAccountsUseCase()
            _accounts.value = result
            
            // If we have a selected account, update it from the new list if possible
            val currentSelectedId = (_selectedAccount.value as? Result.Success)?.data?.id
            if (result is Result.Success && currentSelectedId != null) {
                val updatedAccount = result.data.find { it.id == currentSelectedId }
                if (updatedAccount != null) {
                    _selectedAccount.value = Result.Success(updatedAccount)
                }
            }
        }
    }

    fun selectAccount(id: String?) {
        if (id == null) {
            _selectedAccount.value = Result.Loading
            return
        }
        loadAccountById(id)
    }

    private fun loadAccountById(id: String) {
        viewModelScope.launch {
            _selectedAccount.value = Result.Loading
            val accounts = (_accounts.value as? Result.Success)?.data
            val account = accounts?.firstOrNull { it.id == id }
            if (account != null) {
                logger.debug("AccountsViewModel", "Loading account from cache: ${account.name} (type=${account.type})")
                _selectedAccount.value = Result.Success(account)
            } else {
                logger.debug("AccountsViewModel", "Fetching account from repo: $id")
                val result = repo.getAccountById(id)
                if (result is Result.Success) {
                    logger.debug("AccountsViewModel", "Fetched account: ${result.data.name} (type=${result.data.type})")
                }
                _selectedAccount.value = result
            }
        }
    }

    fun saveAccount(account: Account) {
        viewModelScope.launch {
            logger.info("AccountsViewModel", "Saving account: ${account.name} (type=${account.type})")
            _saveResult.value = Result.Loading
            val result = repo.addOrUpdateAccount(account)
            _saveResult.value = result
            if (result is Result.Success) {
                logger.info("AccountsViewModel", "Account saved successfully: ${result.data.name}")
                // Update local state immediately for a smooth transition
                val currentResult = _accounts.value
                if (currentResult is Result.Success) {
                    val updatedList = if (currentResult.data.any { it.id == result.data.id }) {
                        currentResult.data.map { if (it.id == result.data.id) result.data else it }
                    } else {
                        currentResult.data + result.data
                    }
                    _accounts.value = Result.Success(updatedList)
                }

                // If the updated account is the currently selected one, update it immediately
                val currentSelectedId = (selectedAccount.value as? Result.Success)?.data?.id
                if (result.data.id == currentSelectedId) {
                    logger.debug("AccountsViewModel", "Updating current selected account state immediately")
                    _selectedAccount.value = Result.Success(result.data)
                }

                globalRefreshManager.triggerRefresh()
                reloadAccounts(showLoading = false)
            }
        }
    }

    fun removeAccount(id: String) {
        val account = (accounts.value as? Result.Success)?.data?.find { it.id == id }
        if (account?.isDefault == true) {
            _deleteResult.value = Result.Error(Exception("Default accounts cannot be removed"))
            return
        }

        viewModelScope.launch {
            _deleteResult.value = Result.Loading
            val result = repo.deleteAccount(id)
            _deleteResult.value = result
            if (result is Result.Success) {
                // Update local state immediately for a smooth transition
                val currentResult = _accounts.value
                if (currentResult is Result.Success) {
                    _accounts.value = Result.Success(currentResult.data.filter { it.id != id })
                }
                reloadAccounts(showLoading = false)
            }
        }
    }

    fun clearAccountData(id: String) {
        viewModelScope.launch {
            _clearDataResult.value = Result.Loading
            _clearDataResult.value = clearAllUserDataUseCase(listOf(id))
            // Note: clearAllUserDataUseCase triggers globalRefreshManager, 
            // which our init block observes to reload accounts without flickering.
        }
    }

    fun clearResults() {
        _saveResult.value = null
        _deleteResult.value = null
        _clearDataResult.value = null
    }
}
