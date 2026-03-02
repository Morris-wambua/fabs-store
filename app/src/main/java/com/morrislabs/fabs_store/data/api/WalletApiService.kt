package com.morrislabs.fabs_store.data.api

import android.content.Context
import android.util.Log
import com.morrislabs.fabs_store.data.model.PagedWalletTransactionResponse
import com.morrislabs.fabs_store.data.model.WalletDTO
import com.morrislabs.fabs_store.data.model.WithdrawRequest
import com.morrislabs.fabs_store.util.AppConfig
import com.morrislabs.fabs_store.util.ClientConfig
import com.morrislabs.fabs_store.util.TokenManager
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

private const val TAG = "WalletApiService"

class WalletApiService(private val context: Context, private val tokenManager: TokenManager) {

    private val baseUrl = AppConfig.Api.BASE_URL
    private val clientConfig = ClientConfig()

    suspend fun fetchStoreWallet(storeId: String): Result<WalletDTO> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)

            val response = client.get("$baseUrl/api/wallets/store/$storeId")

            Log.d(TAG, "Fetching wallet for store: $storeId")

            val responseText = response.bodyAsText()
            Log.d(TAG, "Fetch wallet response: $responseText")

            try {
                val json = Json { ignoreUnknownKeys = true }
                val wallet = json.decodeFromString<WalletDTO>(responseText)
                Result.success(wallet)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse wallet response", e)
                Result.failure(Exception("Failed to parse wallet"))
            }
        } catch (e: ClientRequestException) {
            val errorBody = try { e.response.bodyAsText() } catch (ex: Exception) { "Unable to parse error body" }
            if (e.response.status.value == 404) {
                Log.d(TAG, "Wallet not found (404)")
                Result.failure(Exception("Wallet not found"))
            } else if (e.response.status.value == 401) {
                Log.e(TAG, "Unauthorized access (401): $errorBody")
                Result.failure(Exception("Unauthorized"))
            } else {
                Log.e(TAG, "Failed to fetch wallet - Status: ${e.response.status.value}, Body: $errorBody")
                Result.failure(Exception("Failed to fetch wallet: ${e.response.status.value}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fetch wallet failed with exception: ${e.message}", e)
            Result.failure(Exception("Failed to fetch wallet: ${e.message}"))
        }
    }

    suspend fun fetchStoreTransactions(
        storeId: String,
        page: Int = 0,
        size: Int = 20
    ): Result<PagedWalletTransactionResponse> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)

            val response = client.get("$baseUrl/api/wallets/store/$storeId/transactions") {
                parameter("page", page)
                parameter("size", size)
            }

            Log.d(TAG, "Fetching transactions for store: $storeId (page: $page, size: $size)")

            val responseText = response.bodyAsText()
            Log.d(TAG, "Fetch transactions response: $responseText")

            try {
                val json = Json { ignoreUnknownKeys = true }
                val pagedResponse = json.decodeFromString<PagedWalletTransactionResponse>(responseText)
                Log.d(TAG, "Transactions found: ${pagedResponse.content.size}")
                Result.success(pagedResponse)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse transactions response", e)
                Result.failure(Exception("Failed to parse transactions"))
            }
        } catch (e: ClientRequestException) {
            val errorBody = try { e.response.bodyAsText() } catch (ex: Exception) { "Unable to parse error body" }
            if (e.response.status.value == 404) {
                Log.d(TAG, "Transactions not found (404)")
                Result.success(PagedWalletTransactionResponse())
            } else if (e.response.status.value == 401) {
                Log.e(TAG, "Unauthorized access (401): $errorBody")
                Result.failure(Exception("Unauthorized"))
            } else {
                Log.e(TAG, "Failed to fetch transactions - Status: ${e.response.status.value}, Body: $errorBody")
                Result.failure(Exception("Failed to fetch transactions: ${e.response.status.value}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fetch transactions failed with exception: ${e.message}", e)
            Result.failure(Exception("Failed to fetch transactions: ${e.message}"))
        }
    }

    suspend fun initiateWithdrawal(storeId: String, request: WithdrawRequest): Result<WalletDTO> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.post("$baseUrl/api/wallets/store/$storeId/withdraw") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            val responseText = response.bodyAsText()
            Log.d(TAG, "Withdraw response: $responseText")

            try {
                val json = Json { ignoreUnknownKeys = true }
                val wallet = json.decodeFromString<WalletDTO>(responseText)
                Result.success(wallet)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse withdrawal response", e)
                Result.failure(Exception("Failed to parse withdrawal response"))
            }
        } catch (e: ClientRequestException) {
            val errorBody = try { e.response.bodyAsText() } catch (ex: Exception) { "Unable to parse error body" }
            if (e.response.status.value == 401) {
                Log.e(TAG, "Unauthorized access (401): $errorBody")
                Result.failure(Exception("Unauthorized"))
            } else {
                Log.e(TAG, "Failed to initiate withdrawal - Status: ${e.response.status.value}, Body: $errorBody")
                Result.failure(Exception("Failed to initiate withdrawal: ${e.response.status.value}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Initiate withdrawal failed with exception: ${e.message}", e)
            Result.failure(Exception("Failed to initiate withdrawal: ${e.message}"))
        }
    }
}
