package com.morrislabs.fabs_store.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.morrislabs.fabs_store.data.api.StorePostApiService
import com.morrislabs.fabs_store.data.model.CommentDTO
import com.morrislabs.fabs_store.data.model.HashtagSuggestionDTO
import com.morrislabs.fabs_store.data.model.MediaS3Data
import com.morrislabs.fabs_store.data.model.PostDTO
import com.morrislabs.fabs_store.data.model.PostPayload
import com.morrislabs.fabs_store.data.model.PostType
import com.morrislabs.fabs_store.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PostViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "PostViewModel"
    }

    private val context = application.applicationContext
    private val tokenManager = TokenManager.getInstance(context)
    private val postApiService = StorePostApiService(context, tokenManager)

    private val _postsState = MutableStateFlow<StoreViewModel.LoadingState<List<PostDTO>>>(StoreViewModel.LoadingState.Idle)
    val postsState: StateFlow<StoreViewModel.LoadingState<List<PostDTO>>> = _postsState.asStateFlow()

    private val _postDetailState = MutableStateFlow<StoreViewModel.LoadingState<PostDTO>>(StoreViewModel.LoadingState.Idle)
    val postDetailState: StateFlow<StoreViewModel.LoadingState<PostDTO>> = _postDetailState.asStateFlow()

    private val _commentsState = MutableStateFlow<StoreViewModel.LoadingState<List<CommentDTO>>>(StoreViewModel.LoadingState.Idle)
    val commentsState: StateFlow<StoreViewModel.LoadingState<List<CommentDTO>>> = _commentsState.asStateFlow()

    private val _createPostState = MutableStateFlow<CreatePostState>(CreatePostState.Idle)
    val createPostState: StateFlow<CreatePostState> = _createPostState.asStateFlow()

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _hasMoreComments = MutableStateFlow(false)
    val hasMoreComments: StateFlow<Boolean> = _hasMoreComments.asStateFlow()

    private val _hashtagSuggestions = MutableStateFlow<List<HashtagSuggestionDTO>>(emptyList())
    val hashtagSuggestions: StateFlow<List<HashtagSuggestionDTO>> = _hashtagSuggestions.asStateFlow()

    private val _showHashtagSuggestions = MutableStateFlow(false)
    val showHashtagSuggestions: StateFlow<Boolean> = _showHashtagSuggestions.asStateFlow()

    private var hasMoreCommentsInternal = false
    private var currentCommentsPage = 0
    private val viewedPostIds = mutableSetOf<String>()
    private var hashtagSearchJob: Job? = null

    private fun logPostMediaDiagnostics(source: String, posts: List<PostDTO>) {
        posts.forEachIndexed { index, post ->
            Log.d(
                TAG,
                "[$source][$index] postId=${post.id}, type=${post.type}, mediaUrl=${post.mediaUrl}, presignedMediaUrl=${post.presignedMediaUrl}, thumbnailUrl=${post.thumbnailUrl}, previewAnimationUrl=${post.previewAnimationUrl}"
            )
        }
    }

    fun fetchStorePosts(storeId: String, page: Int = 0, size: Int = 20) {
        if (page == 0) _postsState.value = StoreViewModel.LoadingState.Loading

        viewModelScope.launch {
            val userId = tokenManager.getUserId()
            postApiService.getStorePosts(storeId, userId, page, size)
                .onSuccess { pagedResponse ->
                    Log.d(TAG, "Posts fetched: ${pagedResponse.content.size} items")
                    logPostMediaDiagnostics("store_posts_page_${pagedResponse.page}", pagedResponse.content)
                    _postsState.value = StoreViewModel.LoadingState.Success(pagedResponse.content)
                    _isRefreshing.value = false
                }
                .onFailure { error ->
                    Log.e(TAG, "Fetch posts failed: ${error.message}", error)
                    _postsState.value = StoreViewModel.LoadingState.Error(error.message ?: "Failed to fetch posts")
                    _isRefreshing.value = false
                }
        }
    }

    fun refreshPosts(storeId: String) {
        if (_isRefreshing.value) return
        _isRefreshing.value = true
        fetchStorePosts(storeId)
    }

    fun fetchPostDetail(postId: String) {
        _postDetailState.value = StoreViewModel.LoadingState.Loading

        viewModelScope.launch {
            val userId = tokenManager.getUserId()
            postApiService.getPostById(postId, userId)
                .onSuccess { post ->
                    logPostMediaDiagnostics("post_detail", listOf(post))
                    updatePostInState(post)
                    maybeTrackView(postId)
                }
                .onFailure { error ->
                    _postDetailState.value = StoreViewModel.LoadingState.Error(error.message ?: "Failed to fetch post")
                }
        }
    }

    fun fetchComments(postId: String, page: Int = 0) {
        if (page == 0) {
            _commentsState.value = StoreViewModel.LoadingState.Loading
            currentCommentsPage = 0
            hasMoreCommentsInternal = false
            _hasMoreComments.value = false
        }

        viewModelScope.launch {
            val userId = tokenManager.getUserId()
            postApiService.getPostComments(postId, userId, page)
                .onSuccess { pagedResponse ->
                    val currentComments = if (page > 0) {
                        (_commentsState.value as? StoreViewModel.LoadingState.Success)?.data.orEmpty()
                    } else emptyList()
                    _commentsState.value = StoreViewModel.LoadingState.Success(currentComments + pagedResponse.content)
                    hasMoreCommentsInternal = !pagedResponse.last
                    _hasMoreComments.value = hasMoreCommentsInternal
                    currentCommentsPage = pagedResponse.page
                }
                .onFailure { error ->
                    _commentsState.value = StoreViewModel.LoadingState.Error(error.message ?: "Failed to fetch comments")
                }
        }
    }

    fun loadMoreComments(postId: String) {
        if (!hasMoreCommentsInternal) return
        fetchComments(postId, currentCommentsPage + 1)
    }

    fun addComment(postId: String, content: String) {
        viewModelScope.launch {
            val userId = tokenManager.getUserId() ?: return@launch
            postApiService.addComment(postId, content, userId)
                .onSuccess {
                    fetchComments(postId)
                    fetchPostDetail(postId)
                }
                .onFailure { error ->
                    Log.e(TAG, "Add comment failed: ${error.message}", error)
                }
        }
    }

    fun addReply(postId: String, commentId: String, content: String) {
        viewModelScope.launch {
            val userId = tokenManager.getUserId() ?: return@launch
            postApiService.addCommentReply(postId, commentId, content, userId)
                .onSuccess {
                    fetchComments(postId)
                }
                .onFailure { error ->
                    Log.e(TAG, "Add reply failed: ${error.message}", error)
                }
        }
    }

    fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            val userId = tokenManager.getUserId() ?: return@launch
            postApiService.deleteComment(postId, commentId, userId)
                .onSuccess {
                    fetchComments(postId)
                    fetchPostDetail(postId)
                }
                .onFailure { error ->
                    Log.e(TAG, "Delete comment failed: ${error.message}", error)
                }
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            val userId = tokenManager.getUserId() ?: return@launch
            postApiService.toggleLike(postId, userId)
                .onSuccess { updatedPost ->
                    updatePostInState(updatedPost)
                }
                .onFailure { error ->
                    Log.e(TAG, "Toggle like failed: ${error.message}", error)
                }
        }
    }

    fun toggleSave(postId: String) {
        viewModelScope.launch {
            val userId = tokenManager.getUserId() ?: return@launch
            postApiService.toggleSave(postId, userId)
                .onSuccess { updatedPost ->
                    updatePostInState(updatedPost)
                }
                .onFailure { error ->
                    Log.e(TAG, "Toggle save failed: ${error.message}", error)
                }
        }
    }

    fun sharePost(postId: String) {
        viewModelScope.launch {
            postApiService.sharePost(postId)
                .onSuccess { updatedPost ->
                    updatePostInState(updatedPost)
                }
                .onFailure { error ->
                    Log.e(TAG, "Share post failed: ${error.message}", error)
                }
        }
    }

    fun toggleCommentLike(postId: String, commentId: String) {
        viewModelScope.launch {
            val userId = tokenManager.getUserId() ?: return@launch
            postApiService.toggleCommentLike(postId, commentId, userId)
                .onSuccess { updatedPost ->
                    updatePostInState(updatedPost)
                    fetchComments(postId)
                }
                .onFailure { error ->
                    Log.e(TAG, "Toggle comment like failed: ${error.message}", error)
                }
        }
    }

    fun editComment(postId: String, commentId: String, content: String) {
        viewModelScope.launch {
            val userId = tokenManager.getUserId() ?: return@launch
            postApiService.editComment(postId, commentId, content, userId)
                .onSuccess { updatedPost ->
                    updatePostInState(updatedPost)
                    fetchComments(postId)
                }
                .onFailure { error ->
                    Log.e(TAG, "Edit comment failed: ${error.message}", error)
                }
        }
    }

    fun uploadMedia(uri: Uri, onSuccess: (url: String, filename: String) -> Unit) {
        _uploadState.value = UploadState.Uploading

        viewModelScope.launch {
            val userId = tokenManager.getUserId()
            if (userId.isNullOrBlank()) {
                _uploadState.value = UploadState.Error("User not authenticated")
                return@launch
            }

            postApiService.uploadPostMedia(uri, userId)
                .onSuccess { (url, filename) ->
                    _uploadState.value = UploadState.Success(url, filename)
                    onSuccess(url, filename)
                }
                .onFailure { error ->
                    _uploadState.value = UploadState.Error(error.message ?: "Failed to upload media")
                }
        }
    }

    fun createPost(storeId: String, caption: String, mediaUrl: String, filename: String, type: PostType) {
        _createPostState.value = CreatePostState.Loading

        viewModelScope.launch {
            val payload = PostPayload(
                caption = caption,
                type = type,
                mediaS3Data = MediaS3Data(
                    mediaUrl = mediaUrl,
                    filename = filename
                ),
                autoPlay = type == PostType.VIDEO
            )

            postApiService.createStorePost(storeId, payload)
                .onSuccess { post ->
                    Log.d(TAG, "Post created successfully: ${post.id}")
                    _createPostState.value = CreatePostState.Success(post)
                    fetchStorePosts(storeId)
                }
                .onFailure { error ->
                    Log.e(TAG, "Create post failed: ${error.message}", error)
                    _createPostState.value = CreatePostState.Error(error.message ?: "Failed to create post")
                }
        }
    }

    fun resetCreatePostState() {
        _createPostState.value = CreatePostState.Idle
        _uploadState.value = UploadState.Idle
        clearHashtagSuggestions()
    }

    fun onCaptionInputChanged(caption: String) {
        val activeToken = extractActiveHashtagToken(caption)
        if (activeToken == null) {
            clearHashtagSuggestions()
            return
        }

        hashtagSearchJob?.cancel()
        hashtagSearchJob = viewModelScope.launch {
            delay(180)
            postApiService.getHashtagSuggestions(activeToken.removePrefix("#"), 12)
                .onSuccess { tags ->
                    _hashtagSuggestions.value = tags
                    _showHashtagSuggestions.value = tags.isNotEmpty()
                }
                .onFailure {
                    clearHashtagSuggestions()
                }
        }
    }

    fun clearHashtagSuggestions() {
        hashtagSearchJob?.cancel()
        _hashtagSuggestions.value = emptyList()
        _showHashtagSuggestions.value = false
    }

    private fun updatePostInState(updatedPost: PostDTO) {
        _postDetailState.value = StoreViewModel.LoadingState.Success(updatedPost)
        val currentPosts = (_postsState.value as? StoreViewModel.LoadingState.Success)?.data
        if (currentPosts != null) {
            _postsState.value = StoreViewModel.LoadingState.Success(
                currentPosts.map { if (it.id == updatedPost.id) updatedPost else it }
            )
        }
    }

    private fun maybeTrackView(postId: String) {
        if (viewedPostIds.contains(postId)) return
        viewedPostIds.add(postId)
        viewModelScope.launch {
            val userId = tokenManager.getUserId()
            postApiService.incrementView(postId, userId)
                .onSuccess { updatedPost ->
                    updatePostInState(updatedPost)
                }
                .onFailure { error ->
                    Log.e(TAG, "Increment view failed: ${error.message}", error)
                }
        }
    }

    private fun extractActiveHashtagToken(caption: String): String? {
        if (caption.isBlank()) return null
        var index = caption.length - 1
        while (index >= 0 && !caption[index].isWhitespace()) {
            index--
        }
        val token = caption.substring(index + 1)
        if (!token.startsWith("#")) return null
        if (token == "#") return token
        val body = token.removePrefix("#")
        return if (body.all { it.isLetterOrDigit() || it == '_' }) token else null
    }

    sealed class CreatePostState {
        data object Idle : CreatePostState()
        data object Loading : CreatePostState()
        data class Success(val post: PostDTO) : CreatePostState()
        data class Error(val message: String) : CreatePostState()
    }

    sealed class UploadState {
        data object Idle : UploadState()
        data object Uploading : UploadState()
        data class Success(val url: String, val filename: String) : UploadState()
        data class Error(val message: String) : UploadState()
    }
}
