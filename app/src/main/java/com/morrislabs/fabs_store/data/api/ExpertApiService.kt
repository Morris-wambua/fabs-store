package com.morrislabs.fabs_store.data.api

import android.content.Context
import android.util.Log
import com.morrislabs.fabs_store.data.model.CreateExpertPayload
import com.morrislabs.fabs_store.data.model.ErrorResponse
import com.morrislabs.fabs_store.data.model.ExpertDTO
import com.morrislabs.fabs_store.util.AppConfig
import com.morrislabs.fabs_store.util.ClientConfig
import com.morrislabs.fabs_store.util.TokenManager
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json

private const val TAG = "ExpertApiService"

class ExpertApiService(private val context: Context, private val tokenManager: TokenManager) {

    private val baseUrl = AppConfig.Api.BASE_URL
    private val clientConfig = ClientConfig()

    private val prettyJson = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    suspend fun getAllExperts(): Result<List<ExpertDTO>> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.get("$baseUrl/api/experts")
            val responseText = response.bodyAsText()

            try {
                val experts = prettyJson.decodeFromString<List<ExpertDTO>>(responseText)
                Log.d(TAG, "Successfully fetched ${experts.size} experts")
                Result.success(experts)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse experts response", e)
                Result.failure(Exception("Failed to parse experts"))
            }
        } catch (e: ClientRequestException) {
            Log.e(TAG, "Failed to fetch experts: ${e.response.status}", e)
            Result.failure(Exception("Failed to fetch experts: ${e.response.status.value}"))
        } catch (e: Exception) {
            Log.e(TAG, "Exception during experts fetch", e)
            Result.failure(Exception("Failed to fetch experts: ${e.message}"))
        }
    }

    suspend fun getExpertById(expertId: String): Result<ExpertDTO> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.get("$baseUrl/api/experts/$expertId")
            val responseText = response.bodyAsText()

            try {
                val expert = prettyJson.decodeFromString<ExpertDTO>(responseText)
                Log.d(TAG, "Successfully fetched expert: ${expert.name}")
                Result.success(expert)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse expert response", e)
                Result.failure(Exception("Failed to parse expert"))
            }
        } catch (e: ClientRequestException) {
            Log.e(TAG, "Failed to fetch expert: ${e.response.status}", e)
            Result.failure(Exception("Failed to fetch expert: ${e.response.status.value}"))
        } catch (e: Exception) {
            Log.e(TAG, "Exception during expert fetch", e)
            Result.failure(Exception("Failed to fetch expert: ${e.message}"))
        }
    }

    suspend fun getExpertsByStoreId(storeId: String): Result<List<ExpertDTO>> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.get("$baseUrl/api/experts/store/$storeId")
            val responseText = response.bodyAsText()

            try {
                val experts = prettyJson.decodeFromString<List<ExpertDTO>>(responseText)
                Log.d(TAG, "Successfully fetched ${experts.size} experts for store")
                Result.success(experts)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse store experts response", e)
                Result.failure(Exception("Failed to parse store experts"))
            }
        } catch (e: ClientRequestException) {
            Log.e(TAG, "Failed to fetch store experts: ${e.response.status}", e)
            Result.failure(Exception("Failed to fetch store experts: ${e.response.status.value}"))
        } catch (e: Exception) {
            Log.e(TAG, "Exception during store experts fetch", e)
            Result.failure(Exception("Failed to fetch store experts: ${e.message}"))
        }
    }

    suspend fun createExpertForStore(storeId: String, payload: CreateExpertPayload): Result<String> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            Log.d(TAG, "Creating expert for store: $storeId")

            val response = client.post("$baseUrl/api/experts/store/$storeId") {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }

            val responseText = response.bodyAsText()
            try {
                val expertId = prettyJson.decodeFromString<String>(responseText)
                Log.d(TAG, "Successfully created expert: $expertId")
                Result.success(expertId)
            } catch (e: Exception) {
                Log.d(TAG, "Expert created, raw response: $responseText")
                Result.success(responseText.trim().replace("\"", ""))
            }
        } catch (e: ClientRequestException) {
            val errorBody = try { e.response.bodyAsText() } catch (ex: Exception) { "" }
            Log.e(TAG, "Failed to create expert: ${e.response.status} - $errorBody", e)
            Result.failure(Exception("Failed to create expert: ${e.response.status.value}"))
        } catch (e: Exception) {
            Log.e(TAG, "Exception during expert creation", e)
            Result.failure(Exception("Failed to create expert: ${e.message}"))
        }
    }
}
