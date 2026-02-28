package com.morrislabs.fabs_store.data.api

import android.content.Context
import android.net.Uri
import android.util.Log
import com.morrislabs.fabs_store.data.model.CreateServicePayload
import com.morrislabs.fabs_store.data.model.TypeOfServiceDTO
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
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val TAG = "ServicesApiService"

class ServicesApiService(private val context: Context, private val tokenManager: TokenManager) {

    private val baseUrl = AppConfig.Api.BASE_URL
    private val clientConfig = ClientConfig()
    private val json = Json { ignoreUnknownKeys = true }
    private val directUploadApi = DirectMediaUploadApiService(context, tokenManager)

    suspend fun fetchServicesByStore(
        storeId: String,
        page: Int = 0,
        size: Int = 100,
        query: String? = null
    ): Result<List<TypeOfServiceDTO>> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.get("$baseUrl/api/typeOfServices/by-store/$storeId") {
                parameter("page", page)
                parameter("size", size)
                if (!query.isNullOrBlank()) {
                    parameter("query", query.trim())
                }
            }
            val responseText = response.bodyAsText()
            Log.d(TAG, "Fetch services by store response: $responseText")

            val paginatedResponse = json.decodeFromString<PaginatedServiceResponse>(responseText)
            Log.d(TAG, "Services fetched for store $storeId: ${paginatedResponse.content.size} items")
            Result.success(paginatedResponse.content)
        } catch (e: ClientRequestException) {
            val errorBody = try { e.response.bodyAsText() } catch (ex: Exception) { "Unable to parse error body" }
            Log.e(TAG, "Failed to fetch services - Status: ${e.response.status.value}, Body: $errorBody")
            Result.failure(Exception("Failed to fetch services"))
        } catch (e: Exception) {
            Log.e(TAG, "Fetch services failed: ${e.message}", e)
            Result.failure(Exception("Failed to fetch services"))
        }
    }

    suspend fun createService(storeId: String, payload: CreateServicePayload): Result<String> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.post("$baseUrl/api/typeOfServices/$storeId") {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }
            val responseText = response.bodyAsText()
            Log.d(TAG, "Create service response: $responseText")
            val serviceId = Json.decodeFromString<String>(responseText)
            Result.success(serviceId)
        } catch (e: ClientRequestException) {
            val errorBody = try { e.response.bodyAsText() } catch (ex: Exception) { "Unable to parse error body" }
            Log.e(TAG, "Create service failed - Status: ${e.response.status.value}, Body: $errorBody")
            Result.failure(Exception("Failed to create service"))
        } catch (e: Exception) {
            Log.e(TAG, "Create service failed: ${e.message}", e)
            Result.failure(Exception("Failed to create service"))
        }
    }

    suspend fun updateService(storeId: String, serviceId: String, payload: CreateServicePayload): Result<String> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.put("$baseUrl/api/typeOfServices/$storeId/$serviceId") {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }
            val responseText = response.bodyAsText()
            Log.d(TAG, "Update service response: $responseText")
            val updatedId = Json.decodeFromString<String>(responseText)
            Result.success(updatedId)
        } catch (e: ClientRequestException) {
            val errorBody = try { e.response.bodyAsText() } catch (ex: Exception) { "Unable to parse error body" }
            Log.e(TAG, "Update service failed - Status: ${e.response.status.value}, Body: $errorBody")
            Result.failure(Exception("Failed to update service"))
        } catch (e: Exception) {
            Log.e(TAG, "Update service failed: ${e.message}", e)
            Result.failure(Exception("Failed to update service"))
        }
    }

    suspend fun deleteService(serviceId: String): Result<Unit> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            client.delete("$baseUrl/api/typeOfServices/$serviceId")
            Log.d(TAG, "Service deleted: $serviceId")
            Result.success(Unit)
        } catch (e: ClientRequestException) {
            val errorBody = try { e.response.bodyAsText() } catch (ex: Exception) { "Unable to parse error body" }
            Log.e(TAG, "Delete service failed - Status: ${e.response.status.value}, Body: $errorBody")
            Result.failure(Exception("Failed to delete service"))
        } catch (e: Exception) {
            Log.e(TAG, "Delete service failed: ${e.message}", e)
            Result.failure(Exception("Failed to delete service"))
        }
    }

    suspend fun uploadServiceImage(uri: Uri, userId: String): Result<Pair<String, String>> {
        return directUploadApi.upload(
            uri = uri,
            userId = userId,
            fallbackContentType = "image/jpeg",
            fallbackName = "service_image.jpg"
        ).onFailure { error ->
            Log.e(TAG, "Upload service image failed", error)
        }
    }
}
