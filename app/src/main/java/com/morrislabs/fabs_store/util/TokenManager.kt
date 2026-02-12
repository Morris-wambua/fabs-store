package com.morrislabs.fabs_store.util

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit

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
        sharedPreferences.edit() { putString(KEY_TOKEN, token) }
    }

    fun getToken(): String? {
        val token = sharedPreferences.getString(KEY_TOKEN, null)
        Log.d(TAG, "Getting token: ${token?.take(10)}...")
        return token
    }

    fun saveRefreshToken(refreshToken: String) {
        Log.d(TAG, "Saving refresh token")
        sharedPreferences.edit() { putString(KEY_REFRESH_TOKEN, refreshToken) }
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
}
