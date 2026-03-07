package com.morrislabs.fabs_store.ui.screens.posts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morrislabs.fabs_store.ui.screens.posts.createflow.CreatePostFlowRoutes
import com.morrislabs.fabs_store.ui.screens.posts.createflow.CreatePostFlowViewModel
import com.morrislabs.fabs_store.ui.screens.posts.createflow.CaptionsTagsScreen
import com.morrislabs.fabs_store.ui.screens.posts.createflow.FiltersScreen
import com.morrislabs.fabs_store.ui.screens.posts.createflow.OverlayItem
import com.morrislabs.fabs_store.ui.screens.posts.createflow.PostPreviewScreen
import com.morrislabs.fabs_store.ui.screens.posts.createflow.RecordVideoScreen
import com.morrislabs.fabs_store.ui.screens.posts.createflow.SoundPickerScreen
import com.morrislabs.fabs_store.ui.screens.posts.createflow.TagStoreDetailsScreen
import com.morrislabs.fabs_store.ui.screens.posts.createflow.TagStoreServicesScreen
import com.morrislabs.fabs_store.ui.screens.posts.createflow.TextStickersScreen
import com.morrislabs.fabs_store.ui.screens.posts.createflow.TrimCropScreen
import com.morrislabs.fabs_store.ui.viewmodel.PostViewModel
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel

@Composable
fun CreatePostScreen(
    storeId: String,
    postViewModel: PostViewModel,
    onNavigateBack: () -> Unit,
    onPostCreated: () -> Unit,
    onNavigateToLiveStream: () -> Unit
) {
    val flowViewModel: CreatePostFlowViewModel = viewModel()
    val draft by flowViewModel.draft.collectAsState()
    val createPostState by postViewModel.createPostState.collectAsState()
    val uploadState by postViewModel.uploadState.collectAsState()
    val soundsState by postViewModel.soundsState.collectAsState()
    val soundSearchQuery by postViewModel.soundSearchQuery.collectAsState()
    val hashtagSuggestions by postViewModel.hashtagSuggestions.collectAsState()
    val showHashtagSuggestions by postViewModel.showHashtagSuggestions.collectAsState()

    var currentRoute by rememberSaveable { mutableStateOf(CreatePostFlowRoutes.RECORD) }
    var returnRouteAfterSounds by rememberSaveable { mutableStateOf(CreatePostFlowRoutes.RECORD) }
    var localError by remember { mutableStateOf<String?>(null) }

    val sounds = (soundsState as? StoreViewModel.LoadingState.Success)?.data ?: emptyList()
    val isLoadingSounds = soundsState is StoreViewModel.LoadingState.Loading
    val isPublishing = uploadState is PostViewModel.UploadState.Uploading || createPostState is PostViewModel.CreatePostState.Loading

    LaunchedEffect(createPostState) {
        when (createPostState) {
            is PostViewModel.CreatePostState.Success -> {
                flowViewModel.resetDraft()
                onPostCreated()
            }

            is PostViewModel.CreatePostState.Error -> {
                localError = (createPostState as PostViewModel.CreatePostState.Error).message
            }

            else -> Unit
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            postViewModel.resetCreatePostState()
            flowViewModel.resetDraft()
        }
    }

    fun goToSounds(returnTo: String) {
        returnRouteAfterSounds = returnTo
        postViewModel.loadTrendingSounds()
        currentRoute = CreatePostFlowRoutes.SOUNDS
    }

    fun publishDraft() {
        val mediaUri = draft.mediaUri ?: run {
            localError = "Select or record a video before publishing."
            return
        }
        localError = null

        draft.soundSelection?.let {
            postViewModel.selectSound(it.sound, it.trimStartMs, if (it.trimEndMs > 0L) it.trimEndMs else it.sound.duration)
        } ?: postViewModel.clearSelectedSound()

        postViewModel.uploadMedia(mediaUri) { mediaUrl, filename ->
            postViewModel.createPost(
                storeId = storeId,
                caption = draft.caption.ifBlank { "Fresh looks this week #FlashSale #StyleInspo #ShoppingDay" },
                mediaUrl = mediaUrl,
                filename = filename,
                type = draft.postType,
                videoTrimStartMs = draft.trim.startMs,
                videoTrimEndMs = draft.trim.endMs,
                videoSpeed = draft.recordingSpeed,
                filterName = draft.filter.name.takeUnless { it.equals("Original", ignoreCase = true) },
                textOverlays = draft.overlays.filterIsInstance<OverlayItem.TextOverlay>().map { it.text },
                emojiOverlays = draft.overlays.filterIsInstance<OverlayItem.StickerOverlay>().map { it.stickerType.name }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentRoute) {
            CreatePostFlowRoutes.RECORD -> {
                RecordVideoScreen(
                    viewModel = flowViewModel,
                    onClose = onNavigateBack,
                    onNavigateToTrimCrop = { currentRoute = CreatePostFlowRoutes.TRIM_CROP },
                    onNavigateToSounds = { goToSounds(CreatePostFlowRoutes.RECORD) }
                )
            }

            CreatePostFlowRoutes.TRIM_CROP -> {
                TrimCropScreen(
                    viewModel = flowViewModel,
                    onBack = { currentRoute = CreatePostFlowRoutes.RECORD },
                    onSave = { currentRoute = CreatePostFlowRoutes.FILTERS }
                )
            }

            CreatePostFlowRoutes.FILTERS -> {
                FiltersScreen(
                    viewModel = flowViewModel,
                    onBack = { currentRoute = CreatePostFlowRoutes.TRIM_CROP },
                    onNext = { goToSounds(CreatePostFlowRoutes.TEXT_STICKERS) }
                )
            }

            CreatePostFlowRoutes.SOUNDS -> {
                SoundPickerScreen(
                    viewModel = flowViewModel,
                    sounds = sounds,
                    isLoading = isLoadingSounds,
                    searchQuery = soundSearchQuery,
                    onSearchQueryChange = postViewModel::onSoundSearchQueryChanged,
                    onLoadTrending = postViewModel::loadTrendingSounds,
                    onBack = { currentRoute = returnRouteAfterSounds },
                    onDone = { currentRoute = returnRouteAfterSounds }
                )
            }

            CreatePostFlowRoutes.TEXT_STICKERS -> {
                TextStickersScreen(
                    viewModel = flowViewModel,
                    onBack = { currentRoute = CreatePostFlowRoutes.FILTERS },
                    onDone = { currentRoute = CreatePostFlowRoutes.TAG_STORE_DETAILS },
                    onSaveDraft = { },
                    onSavePost = { currentRoute = CreatePostFlowRoutes.TAG_STORE_DETAILS }
                )
            }

            CreatePostFlowRoutes.TAG_STORE_DETAILS -> {
                TagStoreDetailsScreen(
                    viewModel = flowViewModel,
                    onBack = { currentRoute = CreatePostFlowRoutes.TEXT_STICKERS },
                    onNext = { currentRoute = CreatePostFlowRoutes.TAG_STORE }
                )
            }

            CreatePostFlowRoutes.TAG_STORE -> {
                TagStoreServicesScreen(
                    viewModel = flowViewModel,
                    onBack = { currentRoute = CreatePostFlowRoutes.TAG_STORE_DETAILS },
                    onDone = { currentRoute = CreatePostFlowRoutes.CAPTIONS_TAGS }
                )
            }

            CreatePostFlowRoutes.CAPTIONS_TAGS -> {
                CaptionsTagsScreen(
                    draft = draft,
                    suggestions = hashtagSuggestions,
                    showSuggestions = showHashtagSuggestions,
                    onBack = { currentRoute = CreatePostFlowRoutes.TAG_STORE },
                    onSaveDraft = { },
                    onCaptionChange = {
                        flowViewModel.setCaption(it)
                        postViewModel.onCaptionInputChanged(it)
                    },
                    onSuggestionClick = { suggestion ->
                        val updated = replaceActiveHashtagToken(draft.caption, suggestion)
                        flowViewModel.setCaption(updated)
                        postViewModel.onCaptionInputChanged(updated)
                    },
                    onPublish = { currentRoute = CreatePostFlowRoutes.PREVIEW }
                )
            }

            CreatePostFlowRoutes.PREVIEW -> {
                PostPreviewScreen(
                    viewModel = flowViewModel,
                    storeName = "fabs_store",
                    onEdit = { currentRoute = CreatePostFlowRoutes.CAPTIONS_TAGS },
                    onPublish = { if (!isPublishing) publishDraft() }
                )
            }
        }

        if (isPublishing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF13EC5B))
            }
        }

        localError?.let { message ->
            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)) {
                Text(text = message, color = Color.Red)
            }
        }
    }
}

private fun replaceActiveHashtagToken(caption: String, hashtag: String): String {
    val clean = hashtag.removePrefix("#")
    val hash = "#$clean"
    if (caption.isBlank()) return "$hash "

    var index = caption.length - 1
    while (index >= 0 && !caption[index].isWhitespace()) {
        index--
    }
    val tokenStart = index + 1
    val token = caption.substring(tokenStart)
    if (!token.startsWith("#")) return "$caption $hash "
    return caption.substring(0, tokenStart) + "$hash "
}
