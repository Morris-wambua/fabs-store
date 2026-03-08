package com.morrislabs.fabs_store.util

import android.content.Context
import android.util.Log
import com.morrislabs.fabs_store.data.api.AuthApiService

class SessionManager(
    context: Context,
    private val onSessionExpired: () -> Unit
) {
    companion object {
        private const val TAG = "SessionManager"
        private const val ACCESS_REFRESH_WINDOW_MS = 15_000L
        private const val REFRESH_LOGOUT_WINDOW_MS = 30_000L
    }

    private val tokenManager = TokenManager.getInstance(context)
    private val authApiService = AuthApiService()
    private var sessionExpiredNotified = false

    fun nextCheckDelayMs(nowMs: Long = System.currentTimeMillis()): Long {
        val accessExpiry = tokenManager.getTokenExpiryMillis()
        val refreshExpiry = tokenManager.getRefreshTokenExpiryMillis()

        var nextDelay = 60_000L

        if (accessExpiry != null) {
            val untilAccessRefreshWindow = (accessExpiry - ACCESS_REFRESH_WINDOW_MS) - nowMs
            if (untilAccessRefreshWindow <= 0L) return 5_000L
            nextDelay = minOf(nextDelay, untilAccessRefreshWindow)
        }

        if (refreshExpiry != null) {
            val untilRefreshCutoffWindow = (refreshExpiry - REFRESH_LOGOUT_WINDOW_MS) - nowMs
            if (untilRefreshCutoffWindow <= 0L) return 5_000L
            nextDelay = minOf(nextDelay, untilRefreshCutoffWindow)
        }

        return nextDelay.coerceIn(10_000L, 60_000L)
    }

    suspend fun enforceSession(): Boolean {
        val hasAccessToken = !tokenManager.getToken().isNullOrBlank()
        val refreshToken = tokenManager.getRefreshToken()

        if (refreshToken.isNullOrBlank()) {
            if (!hasAccessToken || tokenManager.isAccessTokenExpired()) {
                notifySessionExpiredOnce("Session expired: no usable refresh token")
                return false
            }
            sessionExpiredNotified = false
            return true
        }

        if (tokenManager.isRefreshTokenExpired()) {
            notifySessionExpiredOnce("Session expired: refresh token expired")
            return false
        }

        val shouldRefresh = !hasAccessToken ||
            tokenManager.isAccessTokenExpired() ||
            tokenManager.isAccessTokenExpiringWithin(ACCESS_REFRESH_WINDOW_MS)

        if (!shouldRefresh) {
            sessionExpiredNotified = false
            return true
        }

        if (tokenManager.isRefreshTokenExpiringWithin(REFRESH_LOGOUT_WINDOW_MS)) {
            notifySessionExpiredOnce("Session expired: refresh token near expiry during refresh attempt")
            return false
        }

        val refreshed = authApiService.refreshToken(refreshToken).fold(
            onSuccess = { response ->
                response.accessToken?.let { tokenManager.saveToken(it) }
                response.refreshToken?.let { tokenManager.saveRefreshToken(it) }
                !response.accessToken.isNullOrBlank()
            },
            onFailure = {
                false
            }
        )

        if (!refreshed) {
            notifySessionExpiredOnce("Session refresh failed")
            return false
        }

        sessionExpiredNotified = false
        return true
    }

    private fun notifySessionExpiredOnce(reason: String) {
        if (sessionExpiredNotified) return
        sessionExpiredNotified = true
        Log.w(TAG, reason)
        onSessionExpired()
    }
}
