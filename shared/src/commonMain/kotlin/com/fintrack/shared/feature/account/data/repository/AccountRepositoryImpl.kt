package com.fintrack.shared.feature.account.data.repository

import com.fintrack.shared.feature.account.data.model.toCreateRequest
import com.fintrack.shared.feature.account.data.model.toDomain
import com.fintrack.shared.feature.account.data.model.toUpdateRequest
import com.fintrack.shared.feature.account.data.remote.AccountsApi
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.core.util.Result
import com.fintrack.shared.feature.core.util.safeApiCall

class AccountRepositoryImpl(
    private val api: AccountsApi
) : AccountRepository {

    override suspend fun getAccounts(): Result<List<Account>> = safeApiCall {
        val accountsDto = api.getAccounts()
        val accounts = accountsDto.map { it.toDomain() }
        
        // Custom sorting: M-Pesa first, Bank second, others in middle, Cash last.
        accounts.sortedWith { a, b ->
            val weightA = when {
                a.isMpesa || a.name.contains("mpesa", ignoreCase = true) -> 0
                a.name.contains("bank", ignoreCase = true) -> 1
                a.name.contains("cash", ignoreCase = true) -> 3
                else -> 2
            }
            val weightB = when {
                b.isMpesa || b.name.contains("mpesa", ignoreCase = true) -> 0
                b.name.contains("bank", ignoreCase = true) -> 1
                b.name.contains("cash", ignoreCase = true) -> 3
                else -> 2
            }
            
            if (weightA != weightB) {
                weightA.compareTo(weightB)
            } else {
                a.name.compareTo(b.name, ignoreCase = true)
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