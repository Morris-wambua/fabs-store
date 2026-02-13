package com.morrislabs.fabs_store.data.repository

import android.content.Context
import com.morrislabs.fabs_store.data.api.ReservationApiService
import com.morrislabs.fabs_store.data.model.ReservationWithPaymentDTO
import com.morrislabs.fabs_store.util.TokenManager

class ReservationRepository(context: Context, tokenManager: TokenManager) {
    private val reservationApiService = ReservationApiService(context, tokenManager)

    suspend fun fetchStoreReservations(storeId: String): Result<List<ReservationWithPaymentDTO>> {
        return reservationApiService.fetchStoreReservations(storeId)
    }
}
