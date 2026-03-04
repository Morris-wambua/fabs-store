package com.morrislabs.fabs_store.data.api

import android.content.Context
import android.util.Log
import com.morrislabs.fabs_store.data.model.SoundDTO
import com.morrislabs.fabs_store.util.AppConfig
import com.morrislabs.fabs_store.util.ClientConfig
import com.morrislabs.fabs_store.util.TokenManager
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val TAG = "SoundApiService"

@Serializable
data class PagedSoundResponse(
    val content: List<SoundDTO> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val last: Boolean = true
)

class SoundApiService(private val context: Context, private val tokenManager: TokenManager) {

    private val baseUrl = AppConfig.Api.BASE_URL
    private val clientConfig = ClientConfig()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getTrendingSounds(page: Int = 0, size: Int = 20): Result<PagedSoundResponse> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.get("$baseUrl/api/sounds/trending") {
                parameter("page", page)
                parameter("size", size)
            }
            val responseText = response.bodyAsText()
            Log.d(TAG, "Trending sounds response: $responseText")
            val pagedResponse = json.decodeFromString<PagedSoundResponse>(responseText)
            Result.success(pagedResponse)
        } catch (e: Exception) {
            Log.e(TAG, "Fetch trending sounds failed: ${e.message}", e)
            Result.failure(Exception("Failed to fetch trending sounds"))
        }
    }

    suspend fun searchSounds(query: String, page: Int = 0, size: Int = 20): Result<PagedSoundResponse> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.get("$baseUrl/api/sounds/search") {
                parameter("query", query)
                parameter("page", page)
                parameter("size", size)
            }
            val responseText = response.bodyAsText()
            Log.d(TAG, "Search sounds response: $responseText")
            val pagedResponse = json.decodeFromString<PagedSoundResponse>(responseText)
            Result.success(pagedResponse)
        } catch (e: Exception) {
            Log.e(TAG, "Search sounds failed: ${e.message}", e)
            Result.failure(Exception("Failed to search sounds"))
        }
    }

    suspend fun getSoundById(soundId: String): Result<SoundDTO> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.get("$baseUrl/api/sounds/$soundId")
            val responseText = response.bodyAsText()
            val sound = json.decodeFromString<SoundDTO>(responseText)
            Result.success(sound)
        } catch (e: Exception) {
            Log.e(TAG, "Fetch sound failed: ${e.message}", e)
            Result.failure(Exception("Failed to fetch sound"))
        }
    }
}
