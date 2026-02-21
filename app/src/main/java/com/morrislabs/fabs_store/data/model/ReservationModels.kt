package com.morrislabs.fabs_store.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class ReservationStatus {
    BOOKED_PENDING_PAYMENT,
    BOOKED_PENDING_ACCEPTANCE,
    BOOKED_ACCEPTED,
    IN_PROGRESS,
    PENDING_FINAL_PAYMENT,
    CANCELLED,
    SERVED
}

@Serializable
enum class PaymentStatus {
    PENDING,
    PROCESSING,
    FAILED,
    CANCELLED,
    PARTIAL,
    PAID,
    REFUNDED
}

@Serializable
enum class ReservationCreatedBy {
    STORE,
    CUSTOMER,
    MARKETING_TEAM
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
    val reservationExpertName: String = "",
    val createdBy: ReservationCreatedBy? = null,
    val paymentStatus: PaymentStatus? = null,
    val amountPaid: Double? = null,
    val outstandingBalance: Double? = null,
    val minimumPaymentMet: Boolean? = null,
    val paymentRequestId: String? = null,
    val transactionReference: String? = null
)

@Serializable
data class ReservationDTO(
    val id: String? = null,
    val userId: String,
    val name: String,
    val price: Double,
    val reservationDate: String,
    val startTime: String,
    val endTime: String,
    val expert: String,
    val status: ReservationStatus? = null,
    val createdBy: ReservationCreatedBy? = null,
    val store: String,
    val typeOfService: String,
    val reservationExpert: String
)

@Serializable
data class TimeSlot(
    val startTime: String,
    val endTime: String
)

enum class ReservationFilter(val displayName: String) {
    PENDING_APPROVAL("Pending Approval"),
    UPCOMING("Upcoming"),
    IN_PROGRESS("In Progress"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed"),
    LAPSED_PAID("Lapsed & Paid"),
    LAPSED_NOT_ACCEPTED("Lapsed & Not Accepted")
}

@Serializable
enum class ReservationTransitionAction {
    STORE_APPROVE_BOOKING,
    CUSTOMER_START_SESSION,
    STORE_ACCEPT_SESSION,
    STORE_COMPLETE_SERVICE,
    CUSTOMER_ACCEPT_COMPLETE_SERVICE,
    CANCEL
}

@Serializable
data class ReservationTransitionRequest(
    val action: ReservationTransitionAction
)
