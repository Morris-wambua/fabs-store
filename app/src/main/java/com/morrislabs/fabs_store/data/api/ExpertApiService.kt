package com.morrislabs.fabs_store.data.api

import android.content.Context
import android.net.Uri
import android.util.Log
import com.morrislabs.fabs_store.data.model.CreateExpertPayload
import com.morrislabs.fabs_store.data.model.ErrorResponse
import com.morrislabs.fabs_store.data.model.ExpertDTO
import com.morrislabs.fabs_store.data.model.ExpertLeaveDTO
import com.morrislabs.fabs_store.data.model.UploadMediaResponse
import com.morrislabs.fabs_store.util.AppConfig
import com.morrislabs.fabs_store.util.ClientConfig
import com.morrislabs.fabs_store.util.TokenManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
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

    suspend fun updateExpert(expertId: String, payload: CreateExpertPayload): Result<String> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.put("$baseUrl/api/experts/$expertId") {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }
            val responseText = response.bodyAsText()
            Result.success(responseText.trim().replace("\"", ""))
        } catch (e: ClientRequestException) {
            Log.e(TAG, "Failed to update expert: ${e.response.status}", e)
            Result.failure(Exception("Failed to update expert"))
        } catch (e: Exception) {
            Log.e(TAG, "Exception during expert update", e)
            Result.failure(Exception("Failed to update expert: ${e.message}"))
        }
    }

    suspend fun deleteExpert(expertId: String): Result<Unit> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            client.delete("$baseUrl/api/experts/$expertId")
            Result.success(Unit)
        } catch (e: ClientRequestException) {
            Log.e(TAG, "Failed to delete expert: ${e.response.status}", e)
            Result.failure(Exception("Failed to delete expert"))
        } catch (e: Exception) {
            Log.e(TAG, "Exception during expert deletion", e)
            Result.failure(Exception("Failed to delete expert: ${e.message}"))
        }
    }

    suspend fun getExpertLeaves(expertId: String): Result<List<ExpertLeaveDTO>> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.get("$baseUrl/api/experts/$expertId/leaves")
            val responseText = response.bodyAsText()
            val leaves = prettyJson.decodeFromString<List<ExpertLeaveDTO>>(responseText)
            Result.success(leaves)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch expert leaves", e)
            Result.failure(Exception("Failed to fetch leaves: ${e.message}"))
        }
    }

    suspend fun setExpertLeaveRange(expertId: String, startDate: String, endDate: String, reason: String? = null): Result<Unit> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            var url = "$baseUrl/api/experts/$expertId/leaves?startDate=$startDate&endDate=$endDate"
            if (reason != null) url += "&reason=$reason"
            client.post(url)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set leave range", e)
            Result.failure(Exception("Failed to set leave: ${e.message}"))
        }
    }

    suspend fun deleteExpertLeaveRange(expertId: String, startDate: String, endDate: String): Result<Unit> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            client.delete("$baseUrl/api/experts/$expertId/leaves?startDate=$startDate&endDate=$endDate")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete leave range", e)
            Result.failure(Exception("Failed to delete leave: ${e.message}"))
        }
    }

    suspend fun uploadExpertPhoto(uri: Uri, userId: String): Result<Pair<String, String>> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("Cannot read image file"))
            val bytes = inputStream.readBytes()
            inputStream.close()

            val token = tokenManager.getToken()
            val uploadClient = HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true; isLenient = true })
                }
                install(HttpTimeout) {
                    requestTimeoutMillis = 60000
                    connectTimeoutMillis = 30000
                    socketTimeoutMillis = 60000
                }
            }

            try {
                val response = uploadClient.submitFormWithBinaryData(
                    url = "$baseUrl/api/media/upload",
                    formData = formData {
                        append("file", bytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"expert_photo.jpg\"")
                        })
                        append("userId", userId)
                    }
                ) {
                    token?.let { bearerAuth(it) }
                }
                val responseText = response.bodyAsText()
                Log.d(TAG, "Upload expert photo response: $responseText")
                val json = Json { ignoreUnknownKeys = true }
                val uploadResponse = json.decodeFromString<UploadMediaResponse>(responseText)
                Result.success(Pair(uploadResponse.url, uploadResponse.fileName))
            } finally {
                uploadClient.close()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Upload expert photo failed", e)
            Result.failure(Exception("Failed to upload photo: ${e.message}"))
        }
    }
}
