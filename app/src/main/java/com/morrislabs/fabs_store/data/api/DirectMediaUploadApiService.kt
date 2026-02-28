package com.morrislabs.fabs_store.data.api

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.morrislabs.fabs_store.data.model.UploadCompleteRequest
import com.morrislabs.fabs_store.data.model.UploadIntentRequest
import com.morrislabs.fabs_store.data.model.UploadIntentResponse
import com.morrislabs.fabs_store.data.model.UploadMediaResponse
import com.morrislabs.fabs_store.util.AppConfig
import com.morrislabs.fabs_store.util.ClientConfig
import com.morrislabs.fabs_store.util.TokenManager
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class DirectMediaUploadApiService(
    private val context: Context,
    private val tokenManager: TokenManager
) {
    private val baseUrl = AppConfig.Api.BASE_URL
    private val clientConfig = ClientConfig()

    suspend fun upload(uri: Uri, userId: String, fallbackContentType: String, fallbackName: String): Result<Pair<String, String>> {
        return try {
            val authenticatedClient = clientConfig.createAuthenticatedClient(context, tokenManager)

            val contentType = context.contentResolver.getType(uri) ?: fallbackContentType
            val fileName = resolveDisplayName(uri) ?: fallbackName
            val mediaFile = prepareUploadFile(uri, fileName)
            val sizeBytes = mediaFile.length()
            val baseUploadPath = if (contentType.startsWith("video/")) "/api/videos" else "/api/media"

            val intentResponse = authenticatedClient.post("$baseUrl$baseUploadPath/upload-intent") {
                contentType(ContentType.Application.Json)
                setBody(
                    UploadIntentRequest(
                        fileName = fileName,
                        sizeBytes = sizeBytes,
                        contentType = contentType,
                        userId = userId
                    )
                )
            }
            if (!intentResponse.status.isSuccess()) {
                return Result.failure(Exception("Failed to create upload intent: ${intentResponse.status}"))
            }
            val intent = intentResponse.body<UploadIntentResponse>()

            val uploadSuccess = uploadUriToPresignedUrl(
                uploadUrl = intent.uploadUrl,
                file = mediaFile,
                contentType = contentType
            )
            if (mediaFile.exists()) {
                mediaFile.delete()
            }
            if (!uploadSuccess) {
                return Result.failure(Exception("Direct upload failed"))
            }

            val completeResponse = authenticatedClient.post("$baseUrl$baseUploadPath/complete") {
                contentType(ContentType.Application.Json)
                setBody(
                    UploadCompleteRequest(
                        uploadId = intent.uploadId,
                        objectKey = intent.objectKey,
                        fileName = fileName,
                        contentType = contentType,
                        userId = userId
                    )
                )
            }
            if (!completeResponse.status.isSuccess()) {
                return Result.failure(
                    Exception("Failed to finalize upload: ${completeResponse.status} - ${completeResponse.bodyAsText()}")
                )
            }
            val result = completeResponse.body<UploadMediaResponse>()
            Result.success(result.url to result.fileName)
        } catch (ex: Exception) {
            Result.failure(ex)
        }
    }

    private suspend fun uploadUriToPresignedUrl(
        uploadUrl: String,
        file: File,
        contentType: String
    ): Boolean = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(uploadUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                doOutput = true
                instanceFollowRedirects = false
                connectTimeout = 30_000
                readTimeout = 300_000
                setRequestProperty("Content-Type", contentType)
                setFixedLengthStreamingMode(file.length())
            }

            file.inputStream().use { input ->
                connection.outputStream.use { output ->
                    input.copyTo(output, DEFAULT_BUFFER_SIZE)
                }
            }

            val statusCode = connection.responseCode
            statusCode in 200..299
        } finally {
            connection?.disconnect()
        }
    }

    private fun resolveDisplayName(uri: Uri): String? {
        return context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIdx >= 0 && cursor.moveToFirst()) cursor.getString(nameIdx) else null
        }
    }

    private fun resolveSizeBytes(uri: Uri): Long {
        val lengthFromDescriptor = context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length }
        if (lengthFromDescriptor != null && lengthFromDescriptor > 0) {
            return lengthFromDescriptor
        }
        val sizeFromCursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIdx >= 0 && cursor.moveToFirst()) cursor.getLong(sizeIdx) else null
        }
        return if (sizeFromCursor != null && sizeFromCursor > 0) sizeFromCursor else 0L
    }

    private fun prepareUploadFile(uri: Uri, displayName: String): File {
        val ext = displayName.substringAfterLast('.', "")
        val suffix = if (ext.isNotBlank()) ".${ext}" else ".bin"
        val tempFile = File.createTempFile("direct_upload_", suffix, context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output, DEFAULT_BUFFER_SIZE)
            }
        } ?: throw IllegalStateException("Cannot read media file")
        return tempFile
    }
}
