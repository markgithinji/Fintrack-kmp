package com.fintrack.shared.feature.account.data.remote

import com.fintrack.shared.feature.core.ApiConfig
import com.fintrack.shared.feature.core.ApiResponse
import com.fintrack.shared.feature.account.data.model.AccountDto
import com.fintrack.shared.feature.auth.data.local.TokenProvider
import com.fintrack.shared.feature.auth.data.local.TokenProviderImpl
import com.fintrack.shared.feature.auth.data.repository.TokenRepository
import com.fintrack.shared.feature.auth.data.local.createTokenDataStore
import com.fintrack.shared.feature.core.ApiClient
import io.ktor.client.HttpClient

import io.ktor.client.call.body
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class AccountsApi(
    private val client: HttpClient,
    private val baseUrl: String
) {

    suspend fun getAccounts(): List<AccountDto> {
        val response: ApiResponse<List<AccountDto>> = client.get("$baseUrl/accounts").body()
        return response.result
    }

    suspend fun addAccount(account: AccountDto): AccountDto {
        val response: ApiResponse<AccountDto> = client.post("$baseUrl/accounts") {
            contentType(ContentType.Application.Json)
            setBody(account)
        }.body()
        return response.result
    }

    suspend fun updateAccount(id: Int, account: AccountDto): AccountDto {
        val response: ApiResponse<AccountDto> = client.put("$baseUrl/accounts/$id") {
            contentType(ContentType.Application.Json)
            setBody(account)
        }.body()
        return response.result
    }

    suspend fun deleteAccount(id: Int) {
        client.delete("$baseUrl/accounts/$id")
    }

    suspend fun getAccountById(id: Int): AccountDto {
        val response: ApiResponse<AccountDto> = client.get("$baseUrl/accounts/$id").body()
        return response.result
    }
}