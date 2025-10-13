package com.fintrack.shared.feature.account.data.remote

import com.fintrack.shared.feature.account.data.model.AccountDto
import com.fintrack.shared.feature.account.data.model.CreateAccountRequest
import com.fintrack.shared.feature.account.data.model.UpdateAccountRequest
import com.fintrack.shared.feature.core.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AccountsApi(
    private val client: HttpClient,
    private val baseUrl: String
) {

    suspend fun getAccounts(): List<AccountDto> {
        val response: ApiResponse<List<AccountDto>> = client.get("$baseUrl/accounts").body()
        return response.result
    }

    suspend fun addAccount(request: CreateAccountRequest): AccountDto {
        val response: ApiResponse<AccountDto> = client.post("$baseUrl/accounts") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        return response.result
    }

    suspend fun updateAccount(id: String, request: UpdateAccountRequest): AccountDto {
        val response: ApiResponse<AccountDto> = client.put("$baseUrl/accounts/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        return response.result
    }

    suspend fun deleteAccount(id: String) {
        client.delete("$baseUrl/accounts/$id")
    }

    suspend fun getAccountById(id: String): AccountDto {
        val response: ApiResponse<AccountDto> = client.get("$baseUrl/accounts/$id").body()
        return response.result
    }
}