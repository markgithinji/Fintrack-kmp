package com.fintrack.shared.feature.transaction.data

import com.fintrack.shared.feature.core.data.domain.ApiResponse
import com.fintrack.shared.feature.core.data.domain.PaginatedTransactionDto
import com.fintrack.shared.feature.transaction.data.model.CreateTransactionRequest
import com.fintrack.shared.feature.transaction.data.model.TransactionDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class TransactionApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun getTransactions(
        limit: Int = 20,
        sortBy: String = "date",
        order: String = "DESC",
        afterDateTime: String? = null,
        afterId: String? = null,
        accountId: String? = null,
        isIncome: Boolean? = null
    ): PaginatedTransactionDto {
        val response: ApiResponse<PaginatedTransactionDto> = client.get("$baseUrl/transactions") {
            parameter("limit", limit)
            parameter("sortBy", sortBy)
            parameter("order", order)
            afterDateTime?.let { parameter("afterDateTime", it) }
            afterId?.let { parameter("afterId", it) }
            accountId?.let { parameter("accountId", it) }
            isIncome?.let { parameter("isIncome", it) }
        }.body()
        return response.result
    }

    suspend fun addTransaction(request: CreateTransactionRequest): TransactionDto {
        val response: ApiResponse<TransactionDto> = client.post("$baseUrl/transactions") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        return response.result
    }

    suspend fun addTransactions(requests: List<CreateTransactionRequest>): Unit {
        client.post("$baseUrl/transactions/batch") {
            contentType(ContentType.Application.Json)
            setBody(requests)
        }
    }

    suspend fun importMpesaTransactions(requests: List<CreateTransactionRequest>): Unit {
        client.post("$baseUrl/transactions/mpesa") {
            contentType(ContentType.Application.Json)
            setBody(requests)
        }
    }

    suspend fun getTransaction(id: String): TransactionDto {
        val response: ApiResponse<TransactionDto> = client.get("$baseUrl/transactions/$id").body()
        return response.result
    }

    suspend fun updateTransaction(id: String, request: CreateTransactionRequest): TransactionDto {
        val response: ApiResponse<TransactionDto> = client.put("$baseUrl/transactions/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        return response.result
    }

    suspend fun deleteTransaction(id: String) {
        client.delete("$baseUrl/transactions/$id")
    }

    suspend fun deleteAllTransactions() {
        client.delete("$baseUrl/transactions/clear")
    }
}