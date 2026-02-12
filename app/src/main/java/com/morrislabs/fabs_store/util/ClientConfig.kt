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

class ClientConfig {

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
                }
            }

            HttpResponseValidator {
                validateResponse { response ->
                    if (!response.status.isSuccess()) {
                        throw io.ktor.client.plugins.ClientRequestException(response, "HTTP Error: ${response.status}")
                    }
                }
            }

            install(ResponseObserver) {
                onResponse { response ->
                    if (response.status == HttpStatusCode.Unauthorized) {
                        Log.w("ClientConfig", "Received 401 Unauthorized response")
                    }
                }
            }
        }
    }
}
