package com.morrislabs.fabs_store.data.repository

import android.content.Context
import com.morrislabs.fabs_store.data.api.ReservationApiService
import com.morrislabs.fabs_store.data.model.ReservationDTO
import com.morrislabs.fabs_store.data.model.ReservationWithPaymentDTO
import com.morrislabs.fabs_store.util.TokenManager

class ReservationRepository(context: Context, tokenManager: TokenManager) {
    private val reservationApiService = ReservationApiService(context, tokenManager)

    suspend fun fetchStoreReservations(
        storeId: String,
        filterStatus: String = "ALL",
        query: String? = null,
        pageNumber: Int = 0,
        pageSize: Int = 20
    ): Result<List<ReservationWithPaymentDTO>> {
        return reservationApiService.fetchStoreReservations(storeId, filterStatus, query, pageNumber, pageSize)
    }

    suspend fun createReservation(reservation: ReservationDTO): Result<String> {
        return reservationApiService.createReservation(reservation)
    }
}
