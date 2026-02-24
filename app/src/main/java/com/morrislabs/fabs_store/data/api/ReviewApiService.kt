package com.morrislabs.fabs_store.data.api

import android.content.Context
import android.util.Log
import com.morrislabs.fabs_store.data.model.PagedReviewResponse
import com.morrislabs.fabs_store.data.model.ReplyRequest
import com.morrislabs.fabs_store.data.model.ReviewDTO
import com.morrislabs.fabs_store.util.AppConfig
import com.morrislabs.fabs_store.util.ClientConfig
import com.morrislabs.fabs_store.util.TokenManager
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

private const val TAG = "ReviewApiService"

class ReviewApiService(private val context: Context, private val tokenManager: TokenManager) {

    private val baseUrl = AppConfig.Api.BASE_URL
    private val clientConfig = ClientConfig()

    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    suspend fun getStoreReviews(
        storeId: String,
        page: Int = 0,
        size: Int = 10
    ): Result<PagedReviewResponse> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)

            val response = client.get("$baseUrl/api/reviews/store/$storeId") {
                parameter("page", page)
                parameter("size", size)
            }

            val responseText = response.bodyAsText()
            Log.d(TAG, "Fetch reviews response status: ${response.status}")

            if (response.status == HttpStatusCode.OK) {
                val pagedResponse = json.decodeFromString<PagedReviewResponse>(responseText)
                Log.d(TAG, "Fetched ${pagedResponse.content.size} reviews for store $storeId")
                Result.success(pagedResponse)
            } else {
                Log.e(TAG, "Failed to fetch reviews: ${response.status}")
                Result.failure(Exception("Failed to fetch reviews: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching reviews for store $storeId", e)
            Result.failure(Exception("Failed to fetch reviews: ${e.message}"))
        }
    }

    suspend fun replyToReview(reviewId: String, reply: String): Result<ReviewDTO> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)

            val response = client.post("$baseUrl/api/reviews/$reviewId/reply") {
                contentType(ContentType.Application.Json)
                setBody(ReplyRequest(reply = reply))
            }

            val responseText = response.bodyAsText()
            Log.d(TAG, "Reply to review response status: ${response.status}")

            if (response.status == HttpStatusCode.OK) {
                val reviewDTO = json.decodeFromString<ReviewDTO>(responseText)
                Log.d(TAG, "Successfully replied to review $reviewId")
                Result.success(reviewDTO)
            } else {
                Log.e(TAG, "Failed to reply to review: ${response.status}")
                Result.failure(Exception("Failed to reply: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception replying to review $reviewId", e)
            Result.failure(Exception("Failed to reply: ${e.message}"))
        }
    }

    suspend fun getUnrepliedReviews(
        storeId: String,
        page: Int = 0,
        size: Int = 10
    ): Result<PagedReviewResponse> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)

            val response = client.get("$baseUrl/api/reviews/store/$storeId/unreplied") {
                parameter("page", page)
                parameter("size", size)
            }

            val responseText = response.bodyAsText()
            Log.d(TAG, "Fetch unreplied reviews response status: ${response.status}")

            if (response.status == HttpStatusCode.OK) {
                val pagedResponse = json.decodeFromString<PagedReviewResponse>(responseText)
                Log.d(TAG, "Fetched ${pagedResponse.content.size} unreplied reviews")
                Result.success(pagedResponse)
            } else {
                Log.e(TAG, "Failed to fetch unreplied reviews: ${response.status}")
                Result.failure(Exception("Failed to fetch unreplied reviews: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching unreplied reviews", e)
            Result.failure(Exception("Failed to fetch unreplied reviews: ${e.message}"))
        }
    }
}
