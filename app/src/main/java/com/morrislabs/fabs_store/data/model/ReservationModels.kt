package com.morrislabs.fabs_store.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class ReservationStatus {
    BOOKED_PENDING_ACCEPTANCE,
    BOOKED_ACCEPTED,
    IN_PROGRESS,
    CANCELLED,
    SERVED
}

@Serializable
data class ReservationWithPaymentDTO(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val reservationDate: String = "",  // LocalDate as string
    val startTime: String = "",        // LocalTime as string
    val endTime: String = "",          // LocalTime as string
    val expert: String = "",
    val status: ReservationStatus = ReservationStatus.BOOKED_PENDING_ACCEPTANCE,
    val store: String = "",
    val storeName: String = "",
    val typeOfService: String = "",
    val typeOfServiceName: String = "",
    val reservationExpert: String = "",
    val reservationExpertName: String = ""
)

enum class ReservationFilter(val displayName: String) {
    PENDING_APPROVAL("Pending Approval"),
    UPCOMING("Upcoming"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed"),
    LAPSED_PAID("Lapsed & Paid"),
    LAPSED_NOT_ACCEPTED("Lapsed & Not Accepted")
}
