package com.morrislabs.fabs_store.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExpertDTO(
    val id: String,
    val name: String,
    val noOfAttendedCustomers: Int,
    val specialization: List<SubCategory>,
    val ratings: Double,
    val contacts: String,
    val storeId: String,
    val imageUrl: String? = null,
    val bio: String? = null,
    val availability: List<String>? = null,
    @SerialName("available")
    val isAvailable: Boolean = false,
    val yearsOfExperience: Int? = null
)

@Serializable
data class CreateExpertPayload(
    val name: String,
    val bio: String? = null,
    val noOfAttendedCustomers: Int = 0,
    val specialization: List<SubCategory>,
    val ratings: Double = 5.0,
    val contacts: String,
    val storeId: String,
    val imageUrl: String? = null,
    val availability: List<String>? = null,
    @SerialName("available")
    val isAvailable: Boolean = true,
    val yearsOfExperience: Int? = null
)

@Serializable
data class ExpertLeaveDTO(
    val id: String? = null,
    val expertId: String,
    val date: String,
    @SerialName("available")
    val isAvailable: Boolean = false,
    val reason: String? = null
)

fun SubCategory.toDisplayName(): String {
    return this.name
        .replace("_", " ")
        .split("(?<=[a-z])(?=[A-Z])".toRegex())
        .joinToString(" ")
        .split(" ")
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
}

fun MainCategory.toDisplayName(): String {
    return this.name
        .replace("_", " ")
        .split("(?<=[a-z])(?=[A-Z])".toRegex())
        .joinToString(" ")
        .split(" ")
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
}
