package com.fintrack.shared.feature.account.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.account.data.repository.AccountRepositoryImpl
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.core.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AccountsViewModel(private val repo: AccountRepository) : ViewModel() {

    private val _accounts = MutableStateFlow<Result<List<Account>>>(Result.Loading)
    val accounts: StateFlow<Result<List<Account>>> = _accounts

    private val _selectedAccount = MutableStateFlow<Result<Account>>(Result.Loading)
    val selectedAccount: StateFlow<Result<Account>> = _selectedAccount

    private val _saveResult = MutableStateFlow<Result<Account>?>(null)
    val saveResult: StateFlow<Result<Account>?> = _saveResult

    private val _deleteResult = MutableStateFlow<Result<Unit>?>(null)
    val deleteResult: StateFlow<Result<Unit>?> = _deleteResult

    init {
        reloadAccounts()
    }

    // Reload all accounts
    fun reloadAccounts() {
        viewModelScope.launch {
            _accounts.value = Result.Loading
            _selectedAccount.value = Result.Loading

            val result = repo.getAccounts()
            _accounts.value = result

            if (result is Result.Success && result.data.isNotEmpty()) {
                // pick first account by default
                _selectedAccount.value = Result.Success(result.data.first())
            } else if (result is Result.Success && result.data.isEmpty()) {
                _selectedAccount.value = Result.Error(Exception("No accounts available"))
            } else if (result is Result.Error) {
                _selectedAccount.value = result
            }
        }
    }

    fun selectAccount(id: Int) {
        viewModelScope.launch {
            _selectedAccount.value = Result.Loading
            val accounts = (_accounts.value as? Result.Success)?.data
            val account = accounts?.firstOrNull { it.id == id }
            if (account != null) {
                _selectedAccount.value = Result.Success(account)
            } else {
                _selectedAccount.value = repo.getAccountById(id)
            }
        }
    }

    fun saveAccount(account: Account) {
        viewModelScope.launch {
            _saveResult.value = Result.Loading
            _saveResult.value = repo.addOrUpdateAccount(account)
            reloadAccounts()
        }
    }

    fun removeAccount(id: Int) {
        viewModelScope.launch {
            _deleteResult.value = Result.Loading
            _deleteResult.value = repo.deleteAccount(id)
            reloadAccounts()
        }
    }
}