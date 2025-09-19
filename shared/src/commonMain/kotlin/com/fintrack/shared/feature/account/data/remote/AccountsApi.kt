package com.fintrack.shared.feature.account.data.remote

import com.fintrack.shared.feature.core.ApiConfig
import com.fintrack.shared.feature.core.ApiResponse
import com.fintrack.shared.feature.auth.data.SessionManager
import com.fintrack.shared.feature.account.data.model.AccountDto
import io.ktor.client.HttpClient

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
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

class AccountsApi(private val baseUrl: String = ApiConfig.BASE_URL) {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; explicitNulls = false })
        }

        defaultRequest {
            SessionManager.token?.let {
                header("Authorization", "Bearer $it")
            }
        }
    }

    // Get all accounts for the current user
    suspend fun getAccounts(): List<AccountDto> {
        val response: ApiResponse<List<AccountDto>> = client.get("$baseUrl/accounts").body()
        return response.result
    }

    // Add a new account
    suspend fun addAccount(account: AccountDto): AccountDto {
        val response: ApiResponse<AccountDto> = client.post("$baseUrl/accounts") {
            contentType(ContentType.Application.Json)
            setBody(account)
        }.body()
        return response.result
    }

    // Update an existing account
    suspend fun updateAccount(id: Int, account: AccountDto): AccountDto {
        val response: ApiResponse<AccountDto> = client.put("$baseUrl/accounts/$id") {
            contentType(ContentType.Application.Json)
            setBody(account)
        }.body()
        return response.result
    }

    // Delete an account
    suspend fun deleteAccount(id: Int) {
        client.delete("$baseUrl/accounts/$id")
    }

    // Get account by ID
    suspend fun getAccountById(id: Int): AccountDto {
        val response: ApiResponse<AccountDto> = client.get("$baseUrl/accounts/$id").body()
        return response.result
    }
}