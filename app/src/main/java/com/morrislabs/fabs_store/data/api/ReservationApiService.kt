package com.morrislabs.fabs_store.data.api

import android.content.Context
import android.util.Log
import com.morrislabs.fabs_store.data.model.ReservationDTO
import com.morrislabs.fabs_store.data.model.ReservationTransitionAction
import com.morrislabs.fabs_store.data.model.ReservationTransitionRequest
import com.morrislabs.fabs_store.data.model.ReservationWithPaymentDTO
import com.morrislabs.fabs_store.util.AppConfig
import com.morrislabs.fabs_store.util.ClientConfig
import com.morrislabs.fabs_store.util.TokenManager
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

private const val TAG = "ReservationApiService"

class ReservationApiService(private val context: Context, private val tokenManager: TokenManager) {

    private val baseUrl = AppConfig.Api.BASE_URL
    private val clientConfig = ClientConfig()

    suspend fun fetchStoreReservations(
        storeId: String,
        filterStatus: String = "ALL",
        query: String? = null,
        pageNumber: Int = 0,
        pageSize: Int = 20
    ): Result<List<ReservationWithPaymentDTO>> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)

            val response = client.get("$baseUrl/api/reservations/store/$storeId") {
                parameter("status", filterStatus)
                if (!query.isNullOrBlank()) {
                    parameter("query", query.trim())
                }
                parameter("page", pageNumber)
                parameter("size", pageSize)
            }

            Log.d(TAG, "Fetching reservations for store: $storeId (filter: $filterStatus, query: ${query ?: ""}, page: $pageNumber, size: $pageSize)")

            val responseText = response.bodyAsText()
            Log.d(TAG, "Fetch reservations response: $responseText")

            try {
                val json = Json { ignoreUnknownKeys = true }
                val reservations = json.decodeFromString<List<ReservationWithPaymentDTO>>(responseText)
                Log.d(TAG, "Reservations found: ${reservations.size}")
                Result.success(reservations)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse reservations response", e)
                Result.failure(Exception("Failed to parse reservations"))
            }
        } catch (e: ClientRequestException) {
            val errorBody = try { e.response.bodyAsText() } catch (ex: Exception) { "Unable to parse error body" }
            if (e.response.status.value == 404) {
                Log.d(TAG, "Reservations not found (404)")
                Result.success(emptyList())
            } else if (e.response.status.value == 401) {
                Log.e(TAG, "Unauthorized access (401): $errorBody")
                Result.failure(Exception("Unauthorized"))
            } else {
                Log.e(TAG, "Failed to fetch reservations - Status: ${e.response.status.value}, Body: $errorBody")
                Result.failure(Exception("Failed to fetch reservations: ${e.response.status.value}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fetch reservations failed with exception: ${e.message}", e)
            Result.failure(Exception("Failed to fetch reservations: ${e.message}"))
        }
    }

    suspend fun createReservation(reservation: ReservationDTO): Result<String> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.post("$baseUrl/api/reservations") {
                contentType(ContentType.Application.Json)
                setBody(reservation)
            }

            val responseText = response.bodyAsText()
            if (response.status == HttpStatusCode.Created) {
                val createdId = responseText.trim().replace("\"", "")
                Result.success(createdId)
            } else {
                Result.failure(Exception("Failed to create reservation: ${response.status.value}"))
            }
        } catch (e: ClientRequestException) {
            val errorBody = try { e.response.bodyAsText() } catch (ex: Exception) { "Unable to parse error body" }
            Log.e(TAG, "Failed to create reservation - Status: ${e.response.status.value}, Body: $errorBody")
            Result.failure(Exception("Failed to create reservation: ${e.response.status.value}"))
        } catch (e: Exception) {
            Log.e(TAG, "Create reservation failed with exception: ${e.message}", e)
            Result.failure(Exception("Failed to create reservation: ${e.message}"))
        }
    }

    suspend fun transitionReservation(reservationId: String, action: ReservationTransitionAction): Result<String> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.post("$baseUrl/api/reservations/$reservationId/transition") {
                contentType(ContentType.Application.Json)
                setBody(ReservationTransitionRequest(action))
            }
            val responseText = response.bodyAsText()
            if (response.status == HttpStatusCode.OK) {
                Result.success(responseText.trim().replace("\"", ""))
            } else {
                Result.failure(Exception("Failed to transition reservation: ${response.status.value}"))
            }
        } catch (e: ClientRequestException) {
            val errorBody = try { e.response.bodyAsText() } catch (_: Exception) { "Unable to parse error body" }
            Log.e(TAG, "Failed to transition reservation - Status: ${e.response.status.value}, Body: $errorBody")
            Result.failure(Exception("Failed to transition reservation: ${e.response.status.value}"))
        } catch (e: Exception) {
            Log.e(TAG, "Transition reservation failed with exception: ${e.message}", e)
            Result.failure(Exception("Failed to transition reservation: ${e.message}"))
        }
    }
}
