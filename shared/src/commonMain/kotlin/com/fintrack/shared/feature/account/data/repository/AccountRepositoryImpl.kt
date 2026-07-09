package com.fintrack.shared.feature.account.data.repository

import com.fintrack.shared.feature.account.data.model.toCreateRequest
import com.fintrack.shared.feature.account.data.model.toDomain
import com.fintrack.shared.feature.account.data.model.toUpdateRequest
import com.fintrack.shared.feature.account.data.remote.AccountsApi
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.account.domain.model.AccountType
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.util.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class AccountRepositoryImpl(
    private val api: AccountsApi
) : AccountRepository {

    private val _selectedAccountId = MutableStateFlow<String?>(null)
    override val selectedAccountId: StateFlow<String?> = _selectedAccountId.asStateFlow()

    override fun setSelectedAccountId(id: String?) {
        _selectedAccountId.value = id
    }

    override suspend fun getAccounts(): Result<List<Account>> = safeApiCall {
        val accountsDto = api.getAccounts()
        val accounts = accountsDto.map { it.toDomain() }
        
        // Priority sorting:
        // 1. System/Default accounts first
        // 2. Within defaults, M-Pesa then Equity first
        // 3. Followed by creation time (if available)
        accounts.sortedWith { a, b ->
            when {
                a.isDefault != b.isDefault -> if (a.isDefault) -1 else 1
                a.isDefault && a.type != b.type -> {
                    // M-Pesa first, then Equity, then others
                    when {
                        a.type == AccountType.MPESA -> -1
                        b.type == AccountType.MPESA -> 1
                        a.type == AccountType.EQUITY -> -1
                        b.type == AccountType.EQUITY -> 1
                        else -> 0
                    }
                }
                else -> {
                    val timeA = a.createdAt
                    val timeB = b.createdAt
                    if (timeA != null && timeB != null) {
                        timeA.compareTo(timeB)
                    } else {
                        0
                    }
                }
            }
        }
    }

    override suspend fun getAccountById(id: String): Result<Account> = safeApiCall {
        val accountDto = api.getAccountById(id)
        accountDto.toDomain()
    }

    override suspend fun addOrUpdateAccount(account: Account): Result<Account> = safeApiCall {
        if (account.id.isEmpty()) {
            // Create account
            val createRequest = account.toCreateRequest()
            val dto = api.addAccount(createRequest)
            dto.toDomain()
        } else {
            // Update account
            val updateRequest = account.toUpdateRequest()
            val dto = api.updateAccount(account.id, updateRequest)
            dto.toDomain()
        }
    }

    override suspend fun deleteAccount(id: String): Result<Unit> = safeApiCall {
        api.deleteAccount(id)
    }
}