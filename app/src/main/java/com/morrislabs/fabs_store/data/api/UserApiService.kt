package com.morrislabs.fabs_store.data.api

import android.content.Context
import android.util.Log
import com.morrislabs.fabs_store.data.model.UserLookupResponseDTO
import com.morrislabs.fabs_store.util.AppConfig
import com.morrislabs.fabs_store.util.ClientConfig
import com.morrislabs.fabs_store.util.TokenManager
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json

private const val TAG = "UserApiService"

class UserApiService(private val context: Context, private val tokenManager: TokenManager) {

    private val baseUrl = AppConfig.Api.BASE_URL
    private val clientConfig = ClientConfig()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun lookupUserByEmail(email: String): Result<UserLookupResponseDTO> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.get("$baseUrl/api/users/lookup-by-email") {
                parameter("email", email.trim())
            }
            val responseText = response.bodyAsText()

            when (response.status) {
                HttpStatusCode.OK -> {
                    if (responseText.isBlank()) {
                        Result.failure(Exception("Lookup returned empty response"))
                    } else {
                        val payload = json.decodeFromString<UserLookupResponseDTO>(responseText)
                        Result.success(payload)
                    }
                }
                HttpStatusCode.NotFound -> {
                    Result.failure(Exception("User not found"))
                }
                else -> {
                    Log.e(TAG, "Lookup failed: ${response.status.value}, $responseText")
                    Result.failure(Exception("Lookup failed: ${response.status.value}"))
                }
            }
        } catch (e: ClientRequestException) {
            if (e.response.status.value == 404) {
                Result.failure(Exception("User not found"))
            } else {
                val errorBody = try { e.response.bodyAsText() } catch (_: Exception) { "" }
                Log.e(TAG, "Lookup failed: ${e.response.status.value}, $errorBody", e)
                Result.failure(Exception("Lookup failed: ${e.response.status.value}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lookup failed: ${e.message}", e)
            Result.failure(Exception("Lookup failed: ${e.message}"))
        }
    }
}
