package com.morrislabs.fabs_store.data.repository

import android.content.Context
import com.morrislabs.fabs_store.data.api.ExpertApiService
import com.morrislabs.fabs_store.data.model.ExpertDTO
import com.morrislabs.fabs_store.util.TokenManager

class ExpertRepository(private val context: Context, private val tokenManager: TokenManager) {
    private val expertApiService = ExpertApiService(context, tokenManager)

    suspend fun getAllExperts(): Result<List<ExpertDTO>> {
        return expertApiService.getAllExperts()
    }

    suspend fun getExpertById(expertId: String): Result<ExpertDTO> {
        return expertApiService.getExpertById(expertId)
    }

    suspend fun getExpertsByStoreId(storeId: String): Result<List<ExpertDTO>> {
        return expertApiService.getExpertsByStoreId(storeId)
    }
}
