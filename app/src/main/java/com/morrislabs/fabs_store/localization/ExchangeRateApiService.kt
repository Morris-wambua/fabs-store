package com.morrislabs.fabs_store.localization

import android.util.Log
import com.morrislabs.fabs_store.util.ClientConfig
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

@Serializable
data class ExchangeRateResponse(
    val result: String? = null,
    val base_code: String? = null,
    val rates: Map<String, Double> = emptyMap(),
    val time_last_update_unix: Long? = null
)

class ExchangeRateApiService {
    private val client = ClientConfig().createUnAuthenticatedClient()

    suspend fun fetchUsdRates(): Result<ExchangeRateResponse> {
        return try {
            val response: ExchangeRateResponse =
                client.get("https://open.er-api.com/v6/latest/USD").body()
            Result.success(response)
        } catch (e: Exception) {
            Log.e("ExchangeRateApiService", "Failed to fetch FX rates", e)
            Result.failure(e)
        }
    }
}
