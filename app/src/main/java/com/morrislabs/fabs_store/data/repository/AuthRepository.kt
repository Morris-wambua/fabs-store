package com.morrislabs.fabs_store.data.repository

import com.morrislabs.fabs_store.data.api.AuthApiService
import com.morrislabs.fabs_store.data.model.LoginDTO

class AuthRepository(
    private val apiService: AuthApiService = AuthApiService()
) {

    suspend fun login(login: String, password: String): Result<LoginDTO> {
        return apiService.login(login, password)
    }

    suspend fun register(
        firstName: String,
        lastName: String,
        login: String,
        email: String,
        password: String
    ): Result<LoginDTO> {
        return apiService.register(firstName, lastName, login, email, password)
    }

    suspend fun logout(): Result<Unit> {
        return apiService.logout()
    }

    suspend fun refreshToken(refreshToken: String): Result<String> {
        return apiService.refreshToken(refreshToken).mapCatching { it.accessToken ?: "" }
    }
}
