package com.morrislabs.fabs_store.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ReviewDTO(
    val id: String,
    val userId: String,
    val userName: String,
    val displayName: String? = null,
    val storeId: String,
    val rating: Float,
    val comment: String,
    val date: Long = System.currentTimeMillis(),
    val userImageUrl: String? = null
)

@Serializable
data class ReviewSummaryDTO(
    val averageRating: Float,
    val totalReviews: Int,
    val ratingDistribution: Map<Int, Int>
)

@Serializable
data class PagedReviewResponse(
    val content: List<ReviewDTO> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val last: Boolean = true
)
