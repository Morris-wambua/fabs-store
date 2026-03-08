package com.morrislabs.fabs_store.util

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit
import org.json.JSONObject

class TokenManager private constructor(context: Context) {
    private val appContext = context.applicationContext

    private val masterKey = MasterKey.Builder(appContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        appContext,
        "store_auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_FIRST_NAME = "first_name"
        private const val KEY_LAST_NAME = "last_name"
        private const val KEY_TOKEN_EXPIRY_MS = "token_expiry_ms"
        private const val KEY_REFRESH_TOKEN_EXPIRY_MS = "refresh_token_expiry_ms"
        private const val TAG = "TokenManager"

        @Volatile
        private var INSTANCE: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun saveToken(token: String) {
        Log.d(TAG, "Saving token")
        val expiry = extractExpiryMillis(token)
        sharedPreferences.edit() {
            putString(KEY_TOKEN, token)
            if (expiry != null) putLong(KEY_TOKEN_EXPIRY_MS, expiry) else remove(KEY_TOKEN_EXPIRY_MS)
        }
    }

    fun getToken(): String? {
        val token = sharedPreferences.getString(KEY_TOKEN, null)
        Log.d(TAG, "Getting token: ${token?.take(10)}...")
        return token
    }

    fun saveRefreshToken(refreshToken: String) {
        Log.d(TAG, "Saving refresh token")
        val expiry = extractExpiryMillis(refreshToken)
        sharedPreferences.edit() {
            putString(KEY_REFRESH_TOKEN, refreshToken)
            if (expiry != null) putLong(KEY_REFRESH_TOKEN_EXPIRY_MS, expiry) else remove(KEY_REFRESH_TOKEN_EXPIRY_MS)
        }
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    fun saveUserInfo(userId: String, firstName: String, lastName: String) {
        Log.d(TAG, "Saving user info - ID: $userId, Name: $firstName $lastName")
        sharedPreferences.edit {
            putString(KEY_USER_ID, userId)
            putString(KEY_FIRST_NAME, firstName)
            putString(KEY_LAST_NAME, lastName)
        }
    }

    fun getUserId(): String? {
        val userId = sharedPreferences.getString(KEY_USER_ID, null)
        Log.d(TAG, "Getting user ID: $userId")
        return userId
    }

    fun getFirstName(): String? {
        return sharedPreferences.getString(KEY_FIRST_NAME, null)
    }

    fun getLastName(): String? {
        return sharedPreferences.getString(KEY_LAST_NAME, null)
    }

    fun clearToken() {
        Log.d(TAG, "Clearing all token and user data")
        sharedPreferences.edit() {
            remove(KEY_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_FIRST_NAME)
            remove(KEY_LAST_NAME)
            remove(KEY_TOKEN_EXPIRY_MS)
            remove(KEY_REFRESH_TOKEN_EXPIRY_MS)
        }
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    fun verifyUserId(expectedId: String): Boolean {
        val currentId = getUserId()
        val matches = currentId == expectedId
        if (!matches) {
            Log.w(TAG, "User ID mismatch! Expected: $expectedId, Found: $currentId")
        }
        return matches
    }

    fun getTokenExpiryMillis(): Long? {
        if (!sharedPreferences.contains(KEY_TOKEN_EXPIRY_MS)) return null
        return sharedPreferences.getLong(KEY_TOKEN_EXPIRY_MS, 0L)
    }

    fun getRefreshTokenExpiryMillis(): Long? {
        if (!sharedPreferences.contains(KEY_REFRESH_TOKEN_EXPIRY_MS)) return null
        return sharedPreferences.getLong(KEY_REFRESH_TOKEN_EXPIRY_MS, 0L)
    }

    fun isAccessTokenExpired(nowMs: Long = System.currentTimeMillis()): Boolean {
        val expiry = getTokenExpiryMillis() ?: return false
        return nowMs >= expiry
    }

    fun isAccessTokenExpiringWithin(windowMs: Long, nowMs: Long = System.currentTimeMillis()): Boolean {
        val expiry = getTokenExpiryMillis() ?: return false
        return expiry - nowMs <= windowMs
    }

    fun isRefreshTokenExpired(nowMs: Long = System.currentTimeMillis()): Boolean {
        val expiry = getRefreshTokenExpiryMillis() ?: return false
        return nowMs >= expiry
    }

    fun isRefreshTokenExpiringWithin(windowMs: Long, nowMs: Long = System.currentTimeMillis()): Boolean {
        val expiry = getRefreshTokenExpiryMillis() ?: return false
        return expiry - nowMs <= windowMs
    }

    private fun extractExpiryMillis(jwt: String): Long? {
        return try {
            val parts = jwt.split(".")
            if (parts.size < 2) return null
            val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            val payload = String(payloadBytes, Charsets.UTF_8)
            val json = JSONObject(payload)
            if (!json.has("exp")) return null
            json.getLong("exp") * 1000L
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse JWT expiry", e)
            null
        }
    }
}
