package com.fintrack.shared.feature.transaction.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class TransactionApi(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            })
        }
    }

    suspend fun getTransactions(): List<TransactionDto> {
        val response: ApiResponse<List<TransactionDto>> =
            client.get("$baseUrl/transactions").body()
        return response.data
    }

    suspend fun addTransaction(transaction: TransactionDto): TransactionDto {
        val response: ApiResponse<TransactionDto> = client.post("$baseUrl/transactions") {
            contentType(ContentType.Application.Json)
            setBody(transaction)
        }.body()
        return response.data
    }

    suspend fun getSummary(): SummaryDto {
        val response: ApiResponse<SummaryDto> =
            client.get("$baseUrl/transactions/summary")
                .body()
        return response.data
    }
}
