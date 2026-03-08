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
                        if (tokenManager.isRefreshTokenExpired() || tokenManager.isRefreshTokenExpiringWithin(30_000L)) {
                            Log.w("ClientConfig", "Refresh token expired or close to expiry window - forcing logout")
                            tokenManager.clearToken()
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
                    }
                    // Only throw for 401; other errors should be handled by the caller
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
