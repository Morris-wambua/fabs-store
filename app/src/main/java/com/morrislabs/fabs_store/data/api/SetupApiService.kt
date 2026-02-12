package com.morrislabs.fabs_store.data.api

import android.content.Context
import android.util.Log
import com.morrislabs.fabs_store.data.model.LocationDTO
import com.morrislabs.fabs_store.data.model.TypeOfServiceDTO
import com.morrislabs.fabs_store.util.AppConfig
import com.morrislabs.fabs_store.util.ClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

private const val TAG = "SetupApiService"

class SetupApiService(context: Context) {
    private val baseUrl = AppConfig.Api.BASE_URL
    private val clientConfig = ClientConfig()
    private val client = clientConfig.createUnAuthenticatedClient()

    suspend fun fetchAllLocations(): Result<List<LocationDTO>> {
        return try {
            val response = client.get("$baseUrl/api/locations")
            val responseText = response.bodyAsText()

            Log.d(TAG, "Fetch locations response: $responseText")

            try {
                val locations = Json.decodeFromString<List<LocationDTO>>(responseText)
                Log.d(TAG, "Locations fetched: ${locations.size} items")
                Result.success(locations)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse locations response", e)
                Result.failure(Exception("Failed to parse locations"))
            }
        } catch (e: ClientRequestException) {
            val errorBody = try { e.response.bodyAsText() } catch (ex: Exception) { "Unable to parse error body" }
            Log.e(TAG, "Failed to fetch locations - Status: ${e.response.status.value}, Body: $errorBody")
            Result.failure(Exception("Failed to fetch locations"))
        } catch (e: Exception) {
            Log.e(TAG, "Fetch locations failed with exception: ${e.message}", e)
            Result.failure(Exception("Failed to fetch locations"))
        }
    }

    suspend fun fetchAllServices(): Result<List<TypeOfServiceDTO>> {
        return try {
            val response = client.get("$baseUrl/api/typeOfServices?page=0&size=100")
            val responseText = response.bodyAsText()

            Log.d(TAG, "Fetch services response: $responseText")

            try {
                // Response is paginated, so we need to extract content
                val json = Json { ignoreUnknownKeys = true }
                val paginatedResponse = json.decodeFromString<PaginatedServiceResponse>(responseText)
                Log.d(TAG, "Services fetched: ${paginatedResponse.content.size} items")
                Result.success(paginatedResponse.content)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse services response", e)
                Result.failure(Exception("Failed to parse services"))
            }
        } catch (e: ClientRequestException) {
            val errorBody = try { e.response.bodyAsText() } catch (ex: Exception) { "Unable to parse error body" }
            Log.e(TAG, "Failed to fetch services - Status: ${e.response.status.value}, Body: $errorBody")
            Result.failure(Exception("Failed to fetch services"))
        } catch (e: Exception) {
            Log.e(TAG, "Fetch services failed with exception: ${e.message}", e)
            Result.failure(Exception("Failed to fetch services"))
        }
    }
}

// Data class to handle paginated response
@kotlinx.serialization.Serializable
data class PaginatedServiceResponse(
    val content: List<TypeOfServiceDTO>,
    val totalElements: Int = 0,
    val totalPages: Int = 0,
    val size: Int = 0,
    val number: Int = 0,
    val empty: Boolean = true
)
