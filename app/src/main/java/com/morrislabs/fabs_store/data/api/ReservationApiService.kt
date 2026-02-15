package com.morrislabs.fabs_store.data.api

import android.content.Context
import android.util.Log
import com.morrislabs.fabs_store.data.model.ReservationWithPaymentDTO
import com.morrislabs.fabs_store.util.AppConfig
import com.morrislabs.fabs_store.util.ClientConfig
import com.morrislabs.fabs_store.util.TokenManager
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

private const val TAG = "ReservationApiService"

class ReservationApiService(private val context: Context, private val tokenManager: TokenManager) {

    private val baseUrl = AppConfig.Api.BASE_URL
    private val clientConfig = ClientConfig()

    suspend fun fetchStoreReservations(
        storeId: String,
        filterStatus: String = "ALL",
        pageNumber: Int = 0,
        pageSize: Int = 20
    ): Result<List<ReservationWithPaymentDTO>> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)

            val response = client.get("$baseUrl/api/reservations/store/$storeId") {
                parameter("status", filterStatus)
                parameter("page", pageNumber)
                parameter("size", pageSize)
            }

            Log.d(TAG, "Fetching reservations for store: $storeId (filter: $filterStatus, page: $pageNumber, size: $pageSize)")

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
}
