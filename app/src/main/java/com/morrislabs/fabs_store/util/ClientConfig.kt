package com.morrislabs.fabs_store.util

import android.content.Context
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.statement.HttpReceivePipeline
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// Callback interface for authentication state changes
fun interface AuthenticationStateListener {
    fun onSessionExpired()
}

class ClientConfig {
    companion object {
        var authStateListener: AuthenticationStateListener? = null
    }

    fun createUnAuthenticatedClient(): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30000
                connectTimeoutMillis = 30000
                socketTimeoutMillis = 30000
            }

            HttpResponseValidator {
                validateResponse { response ->
                    if (!response.status.isSuccess()) {
                        throw io.ktor.client.plugins.ClientRequestException(response, "HTTP Error: ${response.status}")
                    }
                }
            }
        }
    }

    fun createAuthenticatedClient(context: Context, tokenManager: TokenManager): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30000
                connectTimeoutMillis = 30000
                socketTimeoutMillis = 30000
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        tokenManager.getToken()?.let {
                            BearerTokens(it, "")
                        }
                    }
                    refreshTokens {
                        // Only attempt refresh if we have a valid refresh token
                        val refreshToken = tokenManager.getRefreshToken()
                        
                        if (refreshToken == null) {
                            Log.w("ClientConfig", "Received 401 but no refresh token available - user needs to re-login")
                            authStateListener?.onSessionExpired()
                            return@refreshTokens null
                        }

                        Log.d("ClientConfig", "Attempting automatic token refresh due to 401 response")
                        
                        try {
                            val authService = com.morrislabs.fabs_store.data.api.AuthApiService()
                            val result = authService.refreshToken(refreshToken)
                            
                            result.fold(
                                onSuccess = { refreshTokenDTO ->
                                    Log.d("ClientConfig", "Token refreshed successfully - access token renewed")
                                    refreshTokenDTO.accessToken?.let {
                                        tokenManager.saveToken(it)
                                    }
                                    refreshTokenDTO.refreshToken?.let {
                                        tokenManager.saveRefreshToken(it)
                                    }
                                    refreshTokenDTO.accessToken?.let { newToken ->
                                        BearerTokens(newToken, "")
                                    }
                                },
                                onFailure = { error ->
                                    Log.e("ClientConfig", "Token refresh failed - refresh token may be invalid: ${error.message}")
                                    // Clear all tokens since refresh failed
                                    tokenManager.clearToken()
                                    authStateListener?.onSessionExpired()
                                    null
                                }
                            )
                        } catch (e: Exception) {
                            Log.e("ClientConfig", "Token refresh exception: ${e.message}")
                            tokenManager.clearToken()
                            authStateListener?.onSessionExpired()
                            null
                        }
                    }
                }
            }

            HttpResponseValidator {
                validateResponse { response ->
                    if (response.status == HttpStatusCode.Unauthorized) {
                        // 401 - Auth plugin will attempt refresh
                        Log.e("ClientConfig", "HTTP 401 Unauthorized - Auth plugin will attempt token refresh")
                        throw io.ktor.client.plugins.ClientRequestException(response, "Unauthorized")
                    } else if (response.status == HttpStatusCode.Forbidden) {
                        // 403 Forbidden - May be due to expired token or insufficient permissions
                        // Check if we have a refresh token to attempt recovery
                        val refreshToken = tokenManager.getRefreshToken()
                        if (refreshToken == null) {
                            Log.e("ClientConfig", "HTTP 403 Forbidden and no refresh token available - user must logout")
                            tokenManager.clearToken()
                            authStateListener?.onSessionExpired()
                            throw io.ktor.client.plugins.ClientRequestException(response, "Authentication failed. Your session may have expired.")
                        } else {
                            // Have refresh token - let Auth plugin attempt refresh just like 401
                            // The Auth plugin checks for 401 in the response handler, so we need to manually trigger refresh here
                            Log.d("ClientConfig", "HTTP 403 received - attempting token refresh")
                            try {
                                val authService = com.morrislabs.fabs_store.data.api.AuthApiService()
                                val result = authService.refreshToken(refreshToken)
                                
                                result.fold(
                                    onSuccess = { refreshTokenDTO ->
                                        Log.d("ClientConfig", "Token refreshed successfully - request will be retried")
                                        refreshTokenDTO.accessToken?.let { tokenManager.saveToken(it) }
                                        refreshTokenDTO.refreshToken?.let { tokenManager.saveRefreshToken(it) }
                                    },
                                    onFailure = { error ->
                                        Log.e("ClientConfig", "Token refresh failed on 403 - refresh token may be invalid: ${error.message}")
                                        tokenManager.clearToken()
                                        authStateListener?.onSessionExpired()
                                    }
                                )
                            } catch (e: Exception) {
                                Log.e("ClientConfig", "Token refresh exception on 403: ${e.message}")
                                tokenManager.clearToken()
                                authStateListener?.onSessionExpired()
                            }
                            // Rethrow so request fails and user can retry
                            throw io.ktor.client.plugins.ClientRequestException(response, "Forbidden - token refresh attempted")
                        }
                    } else if (!response.status.isSuccess()) {
                        throw io.ktor.client.plugins.ClientRequestException(response, "HTTP Error: ${response.status}")
                    }
                }
            }

            install(ResponseObserver) {
                onResponse { response ->
                    if (response.status == HttpStatusCode.Unauthorized) {
                        Log.w("ClientConfig", "Received 401 Unauthorized - Auth plugin will attempt refresh if refresh token available")
                    }
                }
            }
        }
    }
}
