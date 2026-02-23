package com.morrislabs.fabs_store.data.api

import android.util.Log
import com.morrislabs.fabs_store.data.model.CredentialsDTO
import com.morrislabs.fabs_store.data.model.ErrorResponse
import com.morrislabs.fabs_store.data.model.GoogleAuthRequest
import com.morrislabs.fabs_store.data.model.LoginDTO
import com.morrislabs.fabs_store.data.model.RefreshTokenDTO
import com.morrislabs.fabs_store.data.model.RegisterDTO
import com.morrislabs.fabs_store.data.model.UserRole
import com.morrislabs.fabs_store.util.AppConfig
import com.morrislabs.fabs_store.util.ClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

private const val TAG = "AuthApiService"

class AuthApiService {

    private val baseUrl = AppConfig.Api.BASE_URL

    private val clientConfig = ClientConfig()
    private val client = clientConfig.createUnAuthenticatedClient()

    suspend fun login(login: String, password: String): Result<LoginDTO> {
        return try {
            val credentialsDTO = CredentialsDTO(
                login = login,
                password = password
            )

            Log.d(TAG, "Attempting login for: $login")

            val response = client.post("$baseUrl/api/login") {
                contentType(ContentType.Application.Json)
                setBody(credentialsDTO)
            }

            val responseText = response.bodyAsText()
            Log.d(TAG, "Login response: $responseText")

            // First, check if response contains an error status
            if (responseText.contains("\"status\":400") || responseText.contains("\"code\":\"RESPONSE_STATUS\"")) {
                Log.e(TAG, "Login response contains error status")
                val errorMessage = parseErrorResponse(responseText)
                Result.failure(Exception(errorMessage))
            } else {
                // Try to parse as successful LoginDTO
                try {
                    val loginDTO = Json.decodeFromString<LoginDTO>(responseText)
                    
                    // Validate that we got actual user data (not just nulls)
                    if (loginDTO.token.isNullOrEmpty() || loginDTO.id.isNullOrEmpty()) {
                        Log.e(TAG, "Login response missing required fields (token or id)")
                        Result.failure(Exception("Invalid login response: missing token or user ID"))
                    } else {
                        Result.success(loginDTO)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse login response", e)
                    Result.failure(Exception("Failed to parse server response: ${e.message}"))
                }
            }

        } catch (e: ClientRequestException) {
            val errorBody = e.response.bodyAsText()
            Log.e(TAG, "Login failed with status ${e.response.status}: $errorBody")

            // Try to parse structured error response first
            val errorMessage = try {
                parseErrorResponse(errorBody)
            } catch (parseError: Exception) {
                Log.w(TAG, "Failed to parse error response, using generic message")
                // Fallback for HTTP 401/400 specifically
                when (e.response.status.value) {
                    400, 401, 403 -> "Invalid email or password"
                    else -> "An error occurred. Please try again."
                }
            }
            
            Result.failure(Exception(errorMessage))

        } catch (e: Exception) {
            Log.e(TAG, "Login failed with exception", e)
            val errorMessage = e.message ?: "An unexpected error occurred"
            Log.e(TAG, "Full exception: $e")
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun register(
        firstName: String,
        lastName: String,
        login: String,
        email: String,
        password: String,
    ): Result<LoginDTO> {
        return try {
            val registerDTO = RegisterDTO(
                firstName = firstName,
                lastName = lastName,
                login = login,
                email = email,
                password = password,
                role = UserRole.STORE_OWNER
            )

            val json = Json { prettyPrint = true }
            val requestBody = json.encodeToString(RegisterDTO.serializer(), registerDTO)
            Log.d(TAG, "Register request body: $requestBody")

            try {
                val response = client.post("$baseUrl/api/signup") {
                    contentType(ContentType.Application.Json)
                    setBody(registerDTO)
                }
                Result.success(response.body())
            } catch (e: ClientRequestException) {
                val errorBody = e.response.bodyAsText()
                Log.e(TAG, "Signup failed: $errorBody")
                
                if (errorBody.contains("Login already exists", ignoreCase = true)) {
                    Log.d(TAG, "User exists, attempting to transition to store owner")
                    return registerStoreOwner(registerDTO)
                } else {
                    Result.failure(Exception(parseErrorResponse(errorBody)))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Register failed with exception", e)
            Result.failure(e)
        }
    }

    private suspend fun registerStoreOwner(registerDTO: RegisterDTO): Result<LoginDTO> {
        return try {
            Log.d(TAG, "Calling register-store-owner endpoint")
            
            val response = client.post("$baseUrl/api/register-store-owner") {
                contentType(ContentType.Application.Json)
                setBody(registerDTO)
            }

            val responseText = response.bodyAsText()
            Log.d(TAG, "Store owner registration response: $responseText")

            try {
                val loginDTO = Json.decodeFromString<LoginDTO>(responseText)
                Log.d(TAG, "Login successful: $loginDTO")
                Result.success(loginDTO)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse store owner response", e)
                Result.failure(Exception("Failed to parse response"))
            }
        } catch (e: ClientRequestException) {
            val errorBody = e.response.bodyAsText()
            Log.e(TAG, "Store owner registration failed with status ${e.response.status}: $errorBody")
            Result.failure(Exception(parseErrorResponse(errorBody)))
        } catch (e: Exception) {
            Log.e(TAG, "Store owner registration failed with exception", e)
            Result.failure(Exception(e.message ?: "Unknown error"))
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            client.post("$baseUrl/api/logout") {
                contentType(ContentType.Application.Json)
            }
            Log.d(TAG, "Logout successful")
            Result.success(Unit)
        } catch (e: ClientRequestException) {
            Log.e(TAG, "Logout failed with status ${e.response.status}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Logout failed with exception", e)
            Result.failure(e)
        }
    }

    suspend fun refreshToken(refreshToken: String): Result<RefreshTokenDTO> {
        return try {
            val refreshRequest = RefreshTokenDTO(refreshToken = refreshToken)

            val response = client.post("$baseUrl/api/refresh") {
                contentType(ContentType.Application.Json)
                setBody(refreshRequest)
            }

            val responseText = response.bodyAsText()
            val tokenResponse = Json.decodeFromString<RefreshTokenDTO>(responseText)
            Log.d(TAG, "Token refreshed successfully")
            Result.success(tokenResponse)
        } catch (e: ClientRequestException) {
            Log.e(TAG, "Token refresh failed with status ${e.response.status}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Token refresh failed with exception", e)
            Result.failure(e)
        }
    }

    suspend fun googleAuth(idToken: String, role: UserRole = UserRole.STORE_OWNER): Result<LoginDTO> {
        return try {
            val request = GoogleAuthRequest(idToken = idToken, role = role)

            Log.d(TAG, "Attempting Google auth")

            val response = client.post("$baseUrl/api/google/auth") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            val responseText = response.bodyAsText()
            Log.d(TAG, "Google auth response received")

            val json = Json { ignoreUnknownKeys = true }
            val loginDTO = json.decodeFromString<LoginDTO>(responseText)

            if (loginDTO.token.isNullOrEmpty() || loginDTO.id.isNullOrEmpty()) {
                Result.failure(Exception("Invalid Google auth response: missing token or user ID"))
            } else {
                Result.success(loginDTO)
            }
        } catch (e: ClientRequestException) {
            val errorBody = e.response.bodyAsText()
            Log.e(TAG, "Google auth failed with status ${e.response.status}: $errorBody")
            val errorMessage = parseErrorResponse(errorBody)
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Log.e(TAG, "Google auth failed with exception", e)
            Result.failure(Exception(e.message ?: "Google authentication failed"))
        }
    }

    private fun parseErrorResponse(errorBody: String): String {
        return try {
            // First, try to parse as ErrorResponse object
            val errorResponse = Json.decodeFromString<ErrorResponse>(errorBody)
            
            // Map backend error messages to user-friendly messages
            when {
                errorResponse.message.contains("Invalid login credentials", ignoreCase = true) ||
                errorResponse.message.contains("Invalid email or password", ignoreCase = true) ||
                errorResponse.message.contains("User not found", ignoreCase = true) ||
                errorResponse.message.contains("Unauthorized", ignoreCase = true) -> {
                    "Invalid email or password"
                }
                errorResponse.message.contains("account is locked", ignoreCase = true) -> {
                    "Your account has been locked. Please contact support."
                }
                errorResponse.message.contains("email not verified", ignoreCase = true) -> {
                    "Please verify your email before logging in."
                }
                else -> errorResponse.message
            }
        } catch (e: Exception) {
            // If parsing as ErrorResponse fails, try to extract message from error body
            Log.w("parseErrorResponse", "Failed to parse as ErrorResponse, attempting to extract message")
            
            // Try to find message in JSON string directly
            val messagePattern = """"message"\s*:\s*"([^"]*)"""".toRegex()
            val match = messagePattern.find(errorBody)
            if (match != null) {
                val extractedMessage = match.groupValues[1]
                Log.d("parseErrorResponse", "Extracted message: $extractedMessage")
                return when {
                    extractedMessage.contains("Invalid login credentials", ignoreCase = true) ||
                    extractedMessage.contains("Invalid email or password", ignoreCase = true) -> {
                        "Invalid email or password"
                    }
                    else -> extractedMessage
                }
            }
            
            "An error occurred during login"
        }
    }
}
