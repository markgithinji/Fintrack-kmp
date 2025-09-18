package com.fintrack.shared.feature.transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.shared.feature.transaction.data.Account
import com.fintrack.shared.feature.transaction.data.AccountsRepository
import com.fintrack.shared.feature.transaction.data.BudgetRepository
import com.fintrack.shared.feature.transaction.data.Result
import com.fintrack.shared.feature.transaction.model.Budget
import com.fintrack.shared.feature.transaction.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate


class AccountsViewModel(
) : ViewModel() {

    private val repo: AccountsRepository = AccountsRepository()

    private val _accounts = MutableStateFlow<Result<List<Account>>>(Result.Loading)
    val accounts: StateFlow<Result<List<Account>>> = _accounts

    private val _saveResult = MutableStateFlow<Result<Account>?>(null)
    val saveResult: StateFlow<Result<Account>?> = _saveResult

    private val _deleteResult = MutableStateFlow<Result<Unit>?>(null)
    val deleteResult: StateFlow<Result<Unit>?> = _deleteResult

    private val _selectedAccount = MutableStateFlow<Result<Account>?>(null)
    val selectedAccount: StateFlow<Result<Account>?> = _selectedAccount

    init {
        reloadAccounts()
    }

    // Reload all accounts
    fun reloadAccounts() {
        viewModelScope.launch {
            _accounts.value = repo.getAccounts()
        }
    }

    // Add or update account
    fun saveAccount(account: Account) {
        viewModelScope.launch {
            _saveResult.value = repo.addOrUpdateAccount(account)
            reloadAccounts()
        }
    }

    // Delete an account
    fun removeAccount(id: Int) {
        viewModelScope.launch {
            _deleteResult.value = repo.deleteAccount(id)
            reloadAccounts()
        }
    }

    // Load single account by ID
    fun loadAccountById(id: Int) {
        viewModelScope.launch {
            // First check in cached accounts
            val current = _accounts.value
            if (current is Result.Success) {
                val found = current.data.firstOrNull { it.id == id }
                if (found != null) {
                    _selectedAccount.value = Result.Success(found)
                    return@launch
                }
            }

            // If not found locally, fetch from repo
            _selectedAccount.value = repo.getAccountById(id)
        }
    }
}
