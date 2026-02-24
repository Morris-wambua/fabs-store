package com.morrislabs.fabs_store.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.morrislabs.fabs_store.data.api.ReviewApiService
import com.morrislabs.fabs_store.data.model.ReviewDTO
import com.morrislabs.fabs_store.data.model.ReviewSummaryDTO
import com.morrislabs.fabs_store.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "ReviewViewModel"
    }

    private val context = application.applicationContext
    private val tokenManager = TokenManager.getInstance(context)
    private val reviewApiService = ReviewApiService(context, tokenManager)

    private val _reviewsState = MutableStateFlow<ReviewsState>(ReviewsState.Idle)
    val reviewsState: StateFlow<ReviewsState> = _reviewsState.asStateFlow()

    private val _replyState = MutableStateFlow<ReplyState>(ReplyState.Idle)
    val replyState: StateFlow<ReplyState> = _replyState.asStateFlow()

    fun fetchStoreReviews(storeId: String) {
        if (storeId.isBlank()) return

        _reviewsState.value = ReviewsState.Loading

        viewModelScope.launch {
            Log.d(TAG, "Fetching reviews for store: $storeId")

            reviewApiService.getStoreReviews(storeId, page = 0, size = 10)
                .onSuccess { pagedResponse ->
                    val reviews = pagedResponse.content
                    Log.d(TAG, "Reviews fetched: ${reviews.size} items")
                    val summary = calculateSummary(reviews)
                    _reviewsState.value = ReviewsState.Success(reviews, summary)
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: "Failed to fetch reviews"
                    Log.e(TAG, "Fetch reviews failed: $errorMessage", error)
                    _reviewsState.value = ReviewsState.Error(errorMessage)
                }
        }
    }

    private fun calculateSummary(reviews: List<ReviewDTO>): ReviewSummaryDTO {
        if (reviews.isEmpty()) {
            return ReviewSummaryDTO(0f, 0, emptyMap())
        }

        val totalReviews = reviews.size
        val averageRating = reviews.sumOf { it.rating.toDouble() }.toFloat() / totalReviews

        val distribution = mutableMapOf<Int, Int>()
        for (i in 1..5) {
            distribution[i] = 0
        }
        reviews.forEach { review ->
            val rating = review.rating.toInt()
            distribution[rating] = (distribution[rating] ?: 0) + 1
        }

        return ReviewSummaryDTO(averageRating, totalReviews, distribution)
    }

    fun replyToReview(reviewId: String, reply: String, storeId: String) {
        _replyState.value = ReplyState.Loading

        viewModelScope.launch {
            Log.d(TAG, "Replying to review: $reviewId")

            reviewApiService.replyToReview(reviewId, reply)
                .onSuccess { updatedReview ->
                    Log.d(TAG, "Reply sent successfully")
                    _replyState.value = ReplyState.Success
                    fetchStoreReviews(storeId)
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: "Failed to send reply"
                    Log.e(TAG, "Reply failed: $errorMessage", error)
                    _replyState.value = ReplyState.Error(errorMessage)
                }
        }
    }

    fun resetReplyState() {
        _replyState.value = ReplyState.Idle
    }

    sealed class ReviewsState {
        data object Idle : ReviewsState()
        data object Loading : ReviewsState()
        data class Success(val reviews: List<ReviewDTO>, val summary: ReviewSummaryDTO) : ReviewsState()
        data class Error(val message: String) : ReviewsState()
    }

    sealed class ReplyState {
        data object Idle : ReplyState()
        data object Loading : ReplyState()
        data object Success : ReplyState()
        data class Error(val message: String) : ReplyState()
    }
}
