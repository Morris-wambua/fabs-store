package com.morrislabs.fabs_store.data.api

import android.content.Context
import android.net.Uri
import android.util.Log
import com.morrislabs.fabs_store.data.model.CommentDTO
import com.morrislabs.fabs_store.data.model.HashtagSuggestionDTO
import com.morrislabs.fabs_store.data.model.PagedCommentResponse
import com.morrislabs.fabs_store.data.model.PagedPostResponse
import com.morrislabs.fabs_store.data.model.PostDTO
import com.morrislabs.fabs_store.data.model.PostPayload
import com.morrislabs.fabs_store.data.model.UploadMediaResponse
import com.morrislabs.fabs_store.util.AppConfig
import com.morrislabs.fabs_store.util.ClientConfig
import com.morrislabs.fabs_store.util.TokenManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val TAG = "StorePostApiService"

class StorePostApiService(private val context: Context, private val tokenManager: TokenManager) {

    private val baseUrl = AppConfig.Api.BASE_URL
    private val clientConfig = ClientConfig()
    private val json = Json { ignoreUnknownKeys = true }
    private val directUploadApi = DirectMediaUploadApiService(context, tokenManager)

    suspend fun createStorePost(storeId: String, payload: PostPayload): Result<PostDTO> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.post("$baseUrl/api/posts/store/$storeId") {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }
            val responseText = response.bodyAsText()
            Log.d(TAG, "Create store post response: $responseText")
            val post = json.decodeFromString<PostDTO>(responseText)
            Result.success(post)
        } catch (e: ClientRequestException) {
            val errorBody = try { e.response.bodyAsText() } catch (ex: Exception) { "Unable to parse error body" }
            Log.e(TAG, "Create post failed - Status: ${e.response.status.value}, Body: $errorBody")
            Result.failure(Exception("Failed to create post"))
        } catch (e: Exception) {
            Log.e(TAG, "Create post failed: ${e.message}", e)
            Result.failure(Exception("Failed to create post"))
        }
    }

    suspend fun getStorePosts(
        storeId: String,
        userId: String? = null,
        page: Int = 0,
        size: Int = 10
    ): Result<PagedPostResponse> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.get("$baseUrl/api/posts/store/$storeId") {
                parameter("page", page)
                parameter("size", size)
                userId?.let { parameter("userId", it) }
            }
            val responseText = response.bodyAsText()
            Log.d(TAG, "Get store posts response: $responseText")
            val pagedResponse = json.decodeFromString<PagedPostResponse>(responseText)
            Result.success(pagedResponse)
        } catch (e: ClientRequestException) {
            val errorBody = try { e.response.bodyAsText() } catch (ex: Exception) { "Unable to parse error body" }
            Log.e(TAG, "Fetch posts failed - Status: ${e.response.status.value}, Body: $errorBody")
            Result.failure(Exception("Failed to fetch posts"))
        } catch (e: Exception) {
            Log.e(TAG, "Fetch posts failed: ${e.message}", e)
            Result.failure(Exception("Failed to fetch posts"))
        }
    }

    suspend fun getPostById(
        postId: String,
        currentUserId: String? = null,
        page: Int = 0,
        size: Int = 10
    ): Result<PostDTO> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.get("$baseUrl/api/posts/$postId") {
                currentUserId?.let { parameter("currentUserId", it) }
                parameter("page", page)
                parameter("size", size)
            }
            val responseText = response.bodyAsText()
            Log.d(TAG, "Get post by ID response: $responseText")
            val post = json.decodeFromString<PostDTO>(responseText)
            Result.success(post)
        } catch (e: Exception) {
            Log.e(TAG, "Fetch post failed: ${e.message}", e)
            Result.failure(Exception("Failed to fetch post"))
        }
    }

    suspend fun getPostComments(
        postId: String,
        currentUserId: String? = null,
        page: Int = 0,
        size: Int = 10
    ): Result<PagedCommentResponse> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.get("$baseUrl/api/posts/$postId/comments") {
                parameter("page", page)
                parameter("size", size)
                currentUserId?.let { parameter("currentUserId", it) }
            }
            val responseText = response.bodyAsText()
            Log.d(TAG, "Get post comments response: $responseText")
            val pagedResponse = json.decodeFromString<PagedCommentResponse>(responseText)
            Result.success(pagedResponse)
        } catch (e: Exception) {
            Log.e(TAG, "Fetch comments failed: ${e.message}", e)
            Result.failure(Exception("Failed to fetch comments"))
        }
    }

    suspend fun getCommentReplies(
        postId: String,
        commentId: String,
        currentUserId: String? = null,
        page: Int = 0,
        size: Int = 10
    ): Result<PagedCommentResponse> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.get("$baseUrl/api/posts/$postId/comments/$commentId/replies") {
                parameter("page", page)
                parameter("size", size)
                currentUserId?.let { parameter("currentUserId", it) }
            }
            val responseText = response.bodyAsText()
            val pagedResponse = json.decodeFromString<PagedCommentResponse>(responseText)
            Result.success(pagedResponse)
        } catch (e: Exception) {
            Log.e(TAG, "Fetch replies failed: ${e.message}", e)
            Result.failure(Exception("Failed to fetch replies"))
        }
    }

    suspend fun addComment(postId: String, content: String, userId: String): Result<PostDTO> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val commentPayload = CommentDTO(content = content)
            val response = client.post("$baseUrl/api/posts/$postId/comments") {
                contentType(ContentType.Application.Json)
                parameter("userId", userId)
                setBody(commentPayload)
            }
            val responseText = response.bodyAsText()
            Log.d(TAG, "Add comment response: $responseText")
            val post = json.decodeFromString<PostDTO>(responseText)
            Result.success(post)
        } catch (e: Exception) {
            Log.e(TAG, "Add comment failed: ${e.message}", e)
            Result.failure(Exception("Failed to add comment"))
        }
    }

    suspend fun addCommentReply(
        postId: String,
        commentId: String,
        content: String,
        userId: String
    ): Result<PostDTO> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val replyPayload = CommentDTO(content = content)
            val response = client.post("$baseUrl/api/posts/$postId/comments/$commentId/replies") {
                contentType(ContentType.Application.Json)
                parameter("userId", userId)
                setBody(replyPayload)
            }
            val responseText = response.bodyAsText()
            Log.d(TAG, "Add reply response: $responseText")
            val post = json.decodeFromString<PostDTO>(responseText)
            Result.success(post)
        } catch (e: Exception) {
            Log.e(TAG, "Add reply failed: ${e.message}", e)
            Result.failure(Exception("Failed to add reply"))
        }
    }

    suspend fun deleteComment(postId: String, commentId: String, userId: String): Result<PostDTO> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.delete("$baseUrl/api/posts/$postId/comments/$commentId") {
                parameter("userId", userId)
            }
            val responseText = response.bodyAsText()
            val post = json.decodeFromString<PostDTO>(responseText)
            Result.success(post)
        } catch (e: Exception) {
            Log.e(TAG, "Delete comment failed: ${e.message}", e)
            Result.failure(Exception("Failed to delete comment"))
        }
    }

    suspend fun toggleLike(postId: String, userId: String): Result<PostDTO> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.post("$baseUrl/api/posts/$postId/like") {
                parameter("userId", userId)
            }
            val responseText = response.bodyAsText()
            val post = json.decodeFromString<PostDTO>(responseText)
            Result.success(post)
        } catch (e: Exception) {
            Log.e(TAG, "Toggle like failed: ${e.message}", e)
            Result.failure(Exception("Failed to toggle like"))
        }
    }

    suspend fun toggleSave(postId: String, userId: String): Result<PostDTO> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.post("$baseUrl/api/posts/$postId/save") {
                parameter("userId", userId)
            }
            val responseText = response.bodyAsText()
            val post = json.decodeFromString<PostDTO>(responseText)
            Result.success(post)
        } catch (e: Exception) {
            Log.e(TAG, "Toggle save failed: ${e.message}", e)
            Result.failure(Exception("Failed to toggle save"))
        }
    }

    suspend fun sharePost(postId: String): Result<PostDTO> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.post("$baseUrl/api/posts/$postId/share")
            val responseText = response.bodyAsText()
            val post = json.decodeFromString<PostDTO>(responseText)
            Result.success(post)
        } catch (e: Exception) {
            Log.e(TAG, "Share post failed: ${e.message}", e)
            Result.failure(Exception("Failed to share post"))
        }
    }

    suspend fun incrementView(postId: String, userId: String? = null): Result<PostDTO> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.post("$baseUrl/api/posts/$postId/view") {
                userId?.let { parameter("userId", it) }
            }
            val responseText = response.bodyAsText()
            val post = json.decodeFromString<PostDTO>(responseText)
            Result.success(post)
        } catch (e: Exception) {
            Log.e(TAG, "Increment view failed: ${e.message}", e)
            Result.failure(Exception("Failed to increment view"))
        }
    }

    suspend fun toggleCommentLike(postId: String, commentId: String, userId: String): Result<PostDTO> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.post("$baseUrl/api/posts/$postId/comments/$commentId/like") {
                parameter("userId", userId)
            }
            val responseText = response.bodyAsText()
            val post = json.decodeFromString<PostDTO>(responseText)
            Result.success(post)
        } catch (e: Exception) {
            Log.e(TAG, "Toggle comment like failed: ${e.message}", e)
            Result.failure(Exception("Failed to toggle comment like"))
        }
    }

    suspend fun getHashtagSuggestions(prefix: String? = null, size: Int = 10): Result<List<HashtagSuggestionDTO>> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val response = client.get("$baseUrl/api/posts/hashtags") {
                parameter("size", size)
                prefix?.let { parameter("prefix", it) }
            }
            val responseText = response.bodyAsText()
            val hashtags = json.decodeFromString<List<HashtagSuggestionDTO>>(responseText)
            Result.success(hashtags)
        } catch (e: Exception) {
            Log.e(TAG, "Fetch hashtag suggestions failed: ${e.message}", e)
            Result.failure(Exception("Failed to fetch hashtag suggestions"))
        }
    }

    suspend fun editComment(postId: String, commentId: String, content: String, userId: String): Result<PostDTO> {
        return try {
            val client = clientConfig.createAuthenticatedClient(context, tokenManager)
            val payload = CommentDTO(content = content)
            val response = client.put("$baseUrl/api/posts/$postId/comments/$commentId") {
                contentType(ContentType.Application.Json)
                parameter("userId", userId)
                setBody(payload)
            }
            val responseText = response.bodyAsText()
            val post = json.decodeFromString<PostDTO>(responseText)
            Result.success(post)
        } catch (e: Exception) {
            Log.e(TAG, "Edit comment failed: ${e.message}", e)
            Result.failure(Exception("Failed to edit comment"))
        }
    }

    suspend fun uploadPostMedia(uri: Uri, userId: String): Result<Pair<String, String>> {
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val extension = if (mimeType.startsWith("video")) "mp4" else "jpg"
        return directUploadApi.upload(
            uri = uri,
            userId = userId,
            fallbackContentType = mimeType,
            fallbackName = "post_media.$extension"
        ).onFailure { error ->
            Log.e(TAG, "Upload media failed", error)
        }
    }
}
