package com.morrislabs.fabs_store.data.api

import android.util.Log
import com.morrislabs.fabs_store.data.model.StorePostDTO
import com.morrislabs.fabs_store.data.model.StorePostPayload
import com.morrislabs.fabs_store.util.ClientConfig
import com.morrislabs.fabs_store.util.TokenManager
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class StorePostApiService {
    private val clientConfig = ClientConfig()
    private val baseUrl = "http://192.168.100.5:8080/fabs/app"

    suspend fun createStorePost(
        storeId: String,
        payload: StorePostPayload
    ): Result<StorePostDTO> {
        return try {
            val client = clientConfig.createAuthenticatedClient(
                android.app.Application(),
                TokenManager.getInstance(android.app.Application())
            )
            
            val response = client.post("$baseUrl/api/stores/$storeId/posts") {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }
            
            Log.d("StorePostApiService", "Create store post response: ${response.status}")
            val body = response.body<StorePostDTO>()
            Result.success(body)
        } catch (e: Exception) {
            Log.e("StorePostApiService", "Error creating store post: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getStorePosts(
        storeId: String,
        page: Int = 0,
        size: Int = 10
    ): Result<List<StorePostDTO>> {
        return try {
            val client = clientConfig.createAuthenticatedClient(
                android.app.Application(),
                TokenManager.getInstance(android.app.Application())
            )
            
            val response = client.get("$baseUrl/api/stores/$storeId/posts") {
                url {
                    parameters.append("page", page.toString())
                    parameters.append("size", size.toString())
                }
            }
            
            Log.d("StorePostApiService", "Get store posts response: ${response.status}")
            // Assuming backend returns a list directly or we need to parse paged response
            val body = response.body<List<StorePostDTO>>()
            Result.success(body)
        } catch (e: Exception) {
            Log.e("StorePostApiService", "Error fetching store posts: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteStorePost(
        storeId: String,
        postId: String
    ): Result<Unit> {
        return try {
            val client = clientConfig.createAuthenticatedClient(
                android.app.Application(),
                TokenManager.getInstance(android.app.Application())
            )
            
            client.delete("$baseUrl/api/stores/$storeId/posts/$postId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("StorePostApiService", "Error deleting store post: ${e.message}", e)
            Result.failure(e)
        }
    }
}
