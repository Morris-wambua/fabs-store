package com.morrislabs.fabs_store.localization

import android.content.Context
import android.util.Log
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency
import java.util.Locale

object ExchangeRateManager {
    private const val PREFS_NAME = "fx_rates_cache"
    private const val KEY_RATES_JSON = "rates_json"
    private const val KEY_LAST_UPDATED = "last_updated"
    private const val STALE_MS = 6 * 60 * 60 * 1000L

    private val mutex = Mutex()
    private val apiService = ExchangeRateApiService()

    @Volatile
    private var loaded = false

    @Volatile
    private var lastUpdated: Long = 0L

    @Volatile
    private var rates: Map<String, Double> = mapOf("USD" to 1.0)

    fun initialize(context: Context) {
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedJson = prefs.getString(KEY_RATES_JSON, null)
            lastUpdated = prefs.getLong(KEY_LAST_UPDATED, 0L)
            if (!savedJson.isNullOrBlank()) {
                rates = parseRatesJson(savedJson).ifEmpty { mapOf("USD" to 1.0) }
            }
            loaded = true
        }
    }

    suspend fun refreshIfStale(context: Context, force: Boolean = false) {
        initialize(context)
        val now = System.currentTimeMillis()
        if (!force && rates.isNotEmpty() && now - lastUpdated < STALE_MS) return

        mutex.withLock {
            val recheckNow = System.currentTimeMillis()
            if (!force && rates.isNotEmpty() && recheckNow - lastUpdated < STALE_MS) return@withLock
            apiService.fetchUsdRates()
                .onSuccess { response ->
                    val fetchedRates = response.rates
                    if (fetchedRates.isNotEmpty()) {
                        val sanitized = fetchedRates.toMutableMap()
                        sanitized["USD"] = 1.0
                        rates = sanitized
                        lastUpdated = recheckNow
                        persist(context, sanitized, lastUpdated)
                    }
                }
                .onFailure { error ->
                    Log.w("ExchangeRateManager", "Using cached FX rates due to refresh failure: ${error.message}")
                }
        }
    }

    fun convertUsdToLocale(amount: Number, locale: Locale): BigDecimal {
        val targetCurrency = runCatching { Currency.getInstance(locale) }.getOrElse { Currency.getInstance("USD") }
        val currencyCode = targetCurrency.currencyCode
        val rate = rates[currencyCode] ?: 1.0
        val usdAmount = amount.toString().toBigDecimalOrNull() ?: BigDecimal.ZERO
        val converted = usdAmount.multiply(BigDecimal.valueOf(rate))
        val digits = maxOf(targetCurrency.defaultFractionDigits, 0)
        return converted.setScale(digits, RoundingMode.HALF_UP)
    }

    fun convertCurrency(amount: Number, sourceCurrencyCode: String, targetCurrencyCode: String): BigDecimal {
        val source = sourceCurrencyCode.uppercase(Locale.US)
        val target = targetCurrencyCode.uppercase(Locale.US)
        val rawAmount = amount.toString().toBigDecimalOrNull() ?: BigDecimal.ZERO
        if (source == target) {
            val targetDigits = runCatching { Currency.getInstance(target).defaultFractionDigits }.getOrDefault(2)
            return rawAmount.setScale(maxOf(targetDigits, 0), RoundingMode.HALF_UP)
        }

        val sourceRate = rates[source] ?: 1.0
        val targetRate = rates[target] ?: 1.0
        val usdAmount = if (source == "USD" || sourceRate <= 0.0) {
            rawAmount
        } else {
            rawAmount.divide(BigDecimal.valueOf(sourceRate), 8, RoundingMode.HALF_UP)
        }
        val converted = if (target == "USD" || targetRate <= 0.0) {
            usdAmount
        } else {
            usdAmount.multiply(BigDecimal.valueOf(targetRate))
        }
        val targetDigits = runCatching { Currency.getInstance(target).defaultFractionDigits }.getOrDefault(2)
        return converted.setScale(maxOf(targetDigits, 0), RoundingMode.HALF_UP)
    }

    fun convertLocalToUsd(amount: Number, locale: Locale): BigDecimal {
        val localCurrency = runCatching { Currency.getInstance(locale) }.getOrElse { Currency.getInstance("USD") }
        val currencyCode = localCurrency.currencyCode
        if (currencyCode.equals("USD", ignoreCase = true)) {
            return amount.toString().toBigDecimalOrNull()
                ?.setScale(2, RoundingMode.HALF_UP)
                ?: BigDecimal.ZERO
        }
        val rate = rates[currencyCode] ?: 1.0
        if (rate <= 0.0) {
            return amount.toString().toBigDecimalOrNull()
                ?.setScale(2, RoundingMode.HALF_UP)
                ?: BigDecimal.ZERO
        }
        val localAmount = amount.toString().toBigDecimalOrNull() ?: BigDecimal.ZERO
        return localAmount.divide(BigDecimal.valueOf(rate), 2, RoundingMode.HALF_UP)
    }

    @VisibleForTesting
    fun setRatesForTesting(testRates: Map<String, Double>) {
        val mutable = testRates.toMutableMap()
        mutable["USD"] = 1.0
        rates = mutable
        loaded = true
    }

    @VisibleForTesting
    fun clearRatesForTesting() {
        rates = mapOf("USD" to 1.0)
        lastUpdated = 0L
        loaded = false
    }

    private fun persist(context: Context, data: Map<String, Double>, updatedAt: Long) {
        val json = JSONObject()
        data.forEach { (code, rate) -> json.put(code, rate) }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_RATES_JSON, json.toString())
            .putLong(KEY_LAST_UPDATED, updatedAt)
            .apply()
    }

    private fun parseRatesJson(json: String): Map<String, Double> {
        return try {
            val obj = JSONObject(json)
            buildMap {
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    put(key, obj.optDouble(key, 1.0))
                }
            }
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
