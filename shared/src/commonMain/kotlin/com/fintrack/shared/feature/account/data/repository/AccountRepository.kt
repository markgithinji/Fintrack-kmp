package com.fintrack.shared.feature.account.data.repository

import com.fintrack.shared.feature.account.data.model.toDomain
import com.fintrack.shared.feature.account.data.model.toDto
import com.fintrack.shared.feature.account.data.remote.AccountsApi
import com.fintrack.shared.feature.account.domain.model.Account
import com.fintrack.shared.feature.account.domain.repository.AccountRepository
import com.fintrack.shared.feature.core.Result

class AccountRepositoryImpl(
    private val api: AccountsApi
) : AccountRepository {

    override suspend fun getAccounts(): Result<List<Account>> = try {
        val accountsDto = api.getAccounts()
        val accounts = accountsDto.map { it.toDomain() }
        Result.Success(accounts)
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun getAccountById(id: Int): Result<Account> = try {
        val accountDto = api.getAccountById(id)
        Result.Success(accountDto.toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun addOrUpdateAccount(account: Account): Result<Account> = try {
        val dto = account.toDto()
        val updatedDto = if (account.id == 0) {
            api.addAccount(dto)
        } else {
            api.updateAccount(account.id, dto)
        }
        Result.Success(updatedDto.toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun deleteAccount(id: Int): Result<Unit> = try {
        api.deleteAccount(id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }
}