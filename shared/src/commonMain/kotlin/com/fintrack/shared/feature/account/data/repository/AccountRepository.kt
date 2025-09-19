package com.fintrack.shared.feature.account.data.repository

import com.fintrack.shared.feature.account.data.remote.AccountsApi
import com.fintrack.shared.feature.account.data.model.toDomain
import com.fintrack.shared.feature.account.data.model.toDto
import com.fintrack.shared.feature.account.domain.Account
import com.fintrack.shared.feature.core.Result

class AccountRepository {

    private val api: AccountsApi = AccountsApi()

    // Get all accounts
    suspend fun getAccounts(): Result<List<Account>> = try {
        val accountsDto = api.getAccounts()
        val accounts = accountsDto.map { it.toDomain() }
        Result.Success(accounts)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // Get account by ID
    suspend fun getAccountById(id: Int): Result<Account> = try {
        val accountDto = api.getAccountById(id)
        Result.Success(accountDto.toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    // Add or update account
    suspend fun addOrUpdateAccount(account: Account): Result<Account> = try {
        val dto = account.toDto()
        val updatedDto = if (account.id == 0) {
            // No id -> create new account
            api.addAccount(dto)
        } else {
            // Existing id -> update
            api.updateAccount(account.id, dto)
        }
        Result.Success(updatedDto.toDomain())
    } catch (e: Exception) {
        Result.Error(e)
    }

    // Delete account
    suspend fun deleteAccount(id: Int): Result<Unit> = try {
        api.deleteAccount(id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }
}