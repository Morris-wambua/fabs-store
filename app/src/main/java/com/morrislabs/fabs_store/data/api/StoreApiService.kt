package com.morrislabs.fabs_store.data.api

import android.content.Context
import android.util.Log
import com.morrislabs.fabs_store.data.model.CreateStorePayload
import com.morrislabs.fabs_store.data.model.FetchStoreResponse
import com.morrislabs.fabs_store.util.AppConfig
import com.morrislabs.fabs_store.util.ClientConfig
import com.morrislabs.fabs_store.util.TokenManager
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

private const val TAG = "StoreApiService"

class StoreApiService(private val context: Context, private val tokenManager: TokenManager) {

    private val baseUrl = AppConfig.Api.BASE_URL
    private val clientConfig = ClientConfig()

    suspend fun fetchUserStore(userId: String): Result<FetchStoreResponse> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            
            val response = client.get("$baseUrl/api/stores/by-user/$userId")

            Log.d(TAG, "Fetching store for user: $userId")

            val responseText = response.bodyAsText()
            Log.d(TAG, "Fetch store response: $responseText")

            try {
                val json = Json { ignoreUnknownKeys = true }
                val store = json.decodeFromString<FetchStoreResponse>(responseText)
                Log.d(TAG, "Store found: ${store.name}")
                Result.success(store)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse store response", e)
                Result.failure(Exception("No store found"))
            }
        } catch (e: ClientRequestException) {
            val errorBody = try { e.response.bodyAsText() } catch (ex: Exception) { "Unable to parse error body" }
            if (e.response.status.value == 404) {
                Log.d(TAG, "Store not found (404)")
                Result.failure(Exception("No store found"))
            } else if (e.response.status.value == 401) {
                Log.e(TAG, "Unauthorized access (401): $errorBody")
                Result.failure(Exception("Unauthorized"))
            } else {
                Log.e(TAG, "Failed to fetch store - Status: ${e.response.status.value}, Body: $errorBody")
                Result.failure(Exception("Failed to fetch store: ${e.response.status.value}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fetch store failed with exception: ${e.message}", e)
            // Check if it's a 404 in the exception message
            if (e.message?.contains("404") == true) {
                Result.failure(Exception("No store found"))
            } else {
                Result.failure(Exception("No store found"))
            }
        }
    }

    suspend fun createStore(payload: CreateStorePayload): Result<String> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)

            val json = Json { prettyPrint = true }
            val requestBody = json.encodeToString(CreateStorePayload.serializer(), payload)
            Log.d(TAG, "Create store request body: $requestBody")

            val response = client.post("$baseUrl/api/stores") {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }

            val responseText = response.bodyAsText()
            try {
                val storeId = Json.decodeFromString<String>(responseText)
                Result.success(storeId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse store creation response", e)
                Result.failure(Exception("Store created but failed to parse response"))
            }
        } catch (e: ClientRequestException) {
            val errorBody = e.response.bodyAsText()
            Log.e(TAG, "Create store failed with status ${e.response.status}: $errorBody")
            Result.failure(Exception("Failed to create store"))
        } catch (e: Exception) {
            Log.e(TAG, "Create store failed with exception", e)
            Result.failure(Exception("An unexpected error occurred: ${e.message}"))
        }
    }
}
