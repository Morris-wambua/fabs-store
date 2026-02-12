package com.morrislabs.fabs_store.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class Badge {
    GOLD, SILVER, BRONZE, UNRANKED
}

@Serializable
enum class MainCategory {
    BEAUTY, WELLNESS, HAIR_CARE, PERSONAL_CARE, OTHER
}

@Serializable
enum class SubCategory {
    HAIRCUT, COLORING, STYLING, MASSAGE, FACIAL, MANICURE, PEDICURE, OTHER
}

@Serializable
data class LocationDTO(
    val id: String,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
)

data class LocationInput(
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class TypeOfServiceDTO(
    val id: String,
    val name: String,
    val mainCategory: MainCategory,
    val subCategory: SubCategory,
    val price: Int,
    val expertAvailable: Boolean,
    val ratings: Double,
    val duration: Int? = null
)

@Serializable
data class StoreDTO(
    val id: String? = null,
    val name: String,
    val username: String,
    val noOfExperts: Int = 0,
    val ratings: Double = 5.0,
    val isVerified: Boolean = false,
    val badge: Badge = Badge.UNRANKED,
    val discount: Double = 0.0
)

@Serializable
data class CreateStorePayload(
    val name: String,
    val username: String,
    val noOfExperts: Int = 0,
    val ratings: Double = 5.0,
    val badge: Badge = Badge.UNRANKED,
    val discount: Double = 0.0,
    val locationId: String,
    val servicesOfferedIds: Set<String>
)

@Serializable
data class FetchStoreResponse(
    val id: String? = null,
    val name: String,
    val username: String,
    val noOfExperts: Int = 0,
    val ratings: Double = 5.0,
    val isVerified: Boolean = false,
    val badge: Badge = Badge.UNRANKED,
    val discount: Double = 0.0
)
