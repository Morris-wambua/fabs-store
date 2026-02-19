package com.morrislabs.fabs_store.data.repository

import android.content.Context
import android.net.Uri
import com.morrislabs.fabs_store.data.api.ExpertApiService
import com.morrislabs.fabs_store.data.model.CreateExpertPayload
import com.morrislabs.fabs_store.data.model.ExpertDTO
import com.morrislabs.fabs_store.data.model.ExpertLeaveDTO
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

    suspend fun createExpertForStore(storeId: String, payload: CreateExpertPayload): Result<String> {
        return expertApiService.createExpertForStore(storeId, payload)
    }

    suspend fun updateExpert(expertId: String, payload: CreateExpertPayload): Result<String> {
        return expertApiService.updateExpert(expertId, payload)
    }

    suspend fun deleteExpert(expertId: String): Result<Unit> {
        return expertApiService.deleteExpert(expertId)
    }

    suspend fun getExpertLeaves(expertId: String): Result<List<ExpertLeaveDTO>> {
        return expertApiService.getExpertLeaves(expertId)
    }

    suspend fun setExpertLeaveRange(expertId: String, startDate: String, endDate: String, reason: String?): Result<Unit> {
        return expertApiService.setExpertLeaveRange(expertId, startDate, endDate, reason)
    }

    suspend fun deleteExpertLeaveRange(expertId: String, startDate: String, endDate: String): Result<Unit> {
        return expertApiService.deleteExpertLeaveRange(expertId, startDate, endDate)
    }

    suspend fun uploadExpertPhoto(uri: Uri, userId: String): Result<Pair<String, String>> {
        return expertApiService.uploadExpertPhoto(uri, userId)
    }
}
