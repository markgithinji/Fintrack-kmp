package com.fintrack.shared.feature.account.data.repository

import com.fintrack.shared.feature.account.data.model.toDomain
import com.fintrack.shared.feature.account.data.model.toDto
import com.fintrack.shared.feature.account.data.remote.AccountsApi
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.core.Result
import com.fintrack.shared.feature.core.safeApiCall

class AccountRepositoryImpl(
    private val api: AccountsApi
) : AccountRepository {

    override suspend fun getAccounts(): Result<List<Account>> = safeApiCall {
        val accountsDto = api.getAccounts()
        accountsDto.map { it.toDomain() }
    }

    override suspend fun getAccountById(id: Int): Result<Account> = safeApiCall {
        val accountDto = api.getAccountById(id)
        accountDto.toDomain()
    }

    override suspend fun addOrUpdateAccount(account: Account): Result<Account> = safeApiCall {
        val dto = account.toDto()
        val updatedDto = if (account.id == 0) {
            api.addAccount(dto)
        } else {
            api.updateAccount(account.id, dto)
        }
        updatedDto.toDomain()
    }

    override suspend fun deleteAccount(id: Int): Result<Unit> = safeApiCall {
        api.deleteAccount(id)
    }
}