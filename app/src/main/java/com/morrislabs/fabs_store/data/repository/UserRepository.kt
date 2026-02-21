package com.morrislabs.fabs_store.data.repository

import android.content.Context
import com.morrislabs.fabs_store.data.api.UserApiService
import com.morrislabs.fabs_store.data.model.UserLookupResponseDTO
import com.morrislabs.fabs_store.util.TokenManager

class UserRepository(context: Context, tokenManager: TokenManager) {
    private val userApiService = UserApiService(context, tokenManager)

    suspend fun lookupUserByEmail(email: String): Result<UserLookupResponseDTO> {
        return userApiService.lookupUserByEmail(email)
    }
}
