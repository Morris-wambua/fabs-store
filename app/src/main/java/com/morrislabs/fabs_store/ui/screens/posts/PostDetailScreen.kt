package com.morrislabs.fabs_store.ui.screens.posts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.morrislabs.fabs_store.data.model.PostDTO
import com.morrislabs.fabs_store.data.model.PostType
import com.morrislabs.fabs_store.ui.viewmodel.PostViewModel
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel
import com.morrislabs.fabs_store.util.AppConfig
import com.morrislabs.fabs_store.util.formatTimeAgo
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    postViewModel: PostViewModel,
    onNavigateBack: () -> Unit
) {
    val postsState by postViewModel.postsState.collectAsState()
    val postDetailState by postViewModel.postDetailState.collectAsState()
    val commentsState by postViewModel.commentsState.collectAsState()
    val hasMoreComments by postViewModel.hasMoreComments.collectAsState()

    var commentsForPostId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(postId) {
        postViewModel.fetchPostDetail(postId)
        postViewModel.fetchComments(postId)
    }

    val queuedPosts = remember(postsState, postDetailState, postId) {
        when (val state = postsState) {
            is StoreViewModel.LoadingState.Success -> buildQueuedPosts(state.data, postId)
            else -> {
                val detail = (postDetailState as? StoreViewModel.LoadingState.Success)?.data
                if (detail != null) listOf(detail) else emptyList()
            }
        }
    }

    commentsForPostId?.let { selectedPostId ->
        ModalBottomSheet(
            onDismissRequest = { commentsForPostId = null },
            containerColor = MaterialTheme.colorScheme.background
        ) {
            val currentComments = (commentsState as? StoreViewModel.LoadingState.Success)?.data ?: emptyList()
            PostCommentsSection(
                comments = currentComments,
                onAddComment = { content -> postViewModel.addComment(selectedPostId, content) },
                onReply = { commentId, content -> postViewModel.addReply(selectedPostId, commentId, content) },
                onDeleteComment = { commentId -> postViewModel.deleteComment(selectedPostId, commentId) },
                onToggleCommentLike = { commentId -> postViewModel.toggleCommentLike(selectedPostId, commentId) },
                onEditComment = { commentId, content -> postViewModel.editComment(selectedPostId, commentId, content) },
                onLoadMore = { postViewModel.loadMoreComments(selectedPostId) },
                hasMore = hasMoreComments
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Post Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        if (queuedPosts.isEmpty()) {
            when (val state = postDetailState) {
                is StoreViewModel.LoadingState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            return@Scaffold
        }

        key(queuedPosts.firstOrNull()?.id ?: postId) {
            val pagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { queuedPosts.size }
            )
            var currentPagePostId by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(pagerState.currentPage, queuedPosts) {
                val currentPost = queuedPosts.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
                val currentPostIdValue = currentPost.id ?: return@LaunchedEffect
                if (currentPagePostId == currentPostIdValue) return@LaunchedEffect
                currentPagePostId = currentPostIdValue
                postViewModel.fetchPostDetail(currentPostIdValue)
                postViewModel.fetchComments(currentPostIdValue)
            }

            VerticalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) { page ->
                val post = queuedPosts[page]
                PostQueuePage(
                    post = post,
                    isVisiblePage = page == pagerState.currentPage,
                    onLikeClick = { post.id?.let(postViewModel::toggleLike) },
                    onCommentClick = {
                        post.id?.let {
                            commentsForPostId = it
                            postViewModel.fetchComments(it)
                        }
                    },
                    onRefreshPost = { post.id?.let(postViewModel::fetchPostDetail) },
                    onSaveClick = { post.id?.let(postViewModel::toggleSave) },
                    onShareClick = { post.id?.let(postViewModel::sharePost) }
                )
            }
        }
    }
}

private fun buildQueuedPosts(posts: List<PostDTO>, selectedPostId: String): List<PostDTO> {
    if (posts.isEmpty()) return emptyList()
    val selectedIndex = posts.indexOfFirst { it.id == selectedPostId }
    if (selectedIndex <= 0) return posts
    return posts.drop(selectedIndex) + posts.take(selectedIndex)
}

@Composable
private fun PostQueuePage(
    post: PostDTO,
    isVisiblePage: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onRefreshPost: () -> Unit,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit
) {
    var showHeart by remember(post.id) { mutableStateOf(false) }
    var isPausedByUser by remember(post.id) { mutableStateOf(false) }

    LaunchedEffect(showHeart) {
        if (showHeart) {
            delay(650L)
            showHeart = false
        }
    }
    LaunchedEffect(isVisiblePage) {
        if (!isVisiblePage) {
            isPausedByUser = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PostMediaSection(
            post = post,
            shouldAutoPlay = isVisiblePage && !isPausedByUser,
            onMediaForbidden = onRefreshPost,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(post.id, post.likedByCurrentUser) {
                    detectTapGestures(
                        onTap = {
                            val videoPlaybackUrl = post.presignedMediaUrl ?: post.mediaUrl
                            if (post.type == PostType.VIDEO && !videoPlaybackUrl.isNullOrBlank()) {
                                isPausedByUser = !isPausedByUser
                            }
                        },
                        onDoubleTap = {
                            if (!post.likedByCurrentUser) {
                                onLikeClick()
                            }
                            showHeart = true
                        }
                    )
                }
        )

        AnimatedVisibility(
            visible = showHeart,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Liked",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(108.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color.Transparent,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                        )
                    )
                )
        ) {
            InteractionRail(
                post = post,
                onLikeClick = onLikeClick,
                onCommentClick = onCommentClick,
                onSaveClick = onSaveClick,
                onShareClick = onShareClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 14.dp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, end = 84.dp, bottom = 16.dp)
            ) {
                post.caption?.takeIf { it.isNotBlank() }?.let { caption ->
                    val tokens = caption.split(Regex("\\s+"))
                    val hashtags = tokens.filter { it.startsWith("#") }
                    val description = tokens.filterNot { it.startsWith("#") }.joinToString(" ").trim()

                    if (description.isNotBlank()) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (hashtags.isNotEmpty()) {
                        Text(
                            text = hashtags.joinToString(" "),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (description.isBlank() && hashtags.isEmpty()) {
                        Text(
                            text = caption,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Text(
                    text = formatTimeAgo(post.dateCreated),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PostMediaSection(
    post: PostDTO,
    shouldAutoPlay: Boolean,
    onMediaForbidden: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val videoPlaybackUrl = post.presignedMediaUrl ?: post.mediaUrl

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxSize()
            .clip(RoundedCornerShape(0.dp))
    ) {
        if (post.type == PostType.VIDEO && !videoPlaybackUrl.isNullOrBlank()) {
            AutoPlayVideoSurface(
                mediaUrl = videoPlaybackUrl,
                shouldAutoPlay = shouldAutoPlay,
                onMediaForbidden = onMediaForbidden,
                modifier = Modifier.fillMaxSize()
            )
            Icon(
                imageVector = Icons.Default.PlayCircleOutline,
                contentDescription = "Video",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.Center)
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(post.mediaUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = post.caption ?: "Post media",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun AutoPlayVideoSurface(
    mediaUrl: String,
    shouldAutoPlay: Boolean,
    onMediaForbidden: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val shouldAutoPlayState by rememberUpdatedState(shouldAutoPlay)
    var hasRequestedRefresh by remember(mediaUrl) { mutableStateOf(false) }
    var hasFirstFrame by remember(mediaUrl) { mutableStateOf(false) }
    val player = remember(mediaUrl) {
        val mediaSourceFactory = DefaultMediaSourceFactory(
            DefaultHttpDataSource.Factory()
                .setUserAgent("fabs-store-media3")
                .setDefaultRequestProperties(mapOf("Referer" to AppConfig.Media.BUNNY_REFERER))
        )
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
            setMediaItem(MediaItem.fromUri(mediaUrl))
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
        }
    }

    LaunchedEffect(shouldAutoPlay, player) {
        if (shouldAutoPlay) {
            player.playWhenReady = true
            player.play()
        } else {
            player.pause()
            player.playWhenReady = false
        }
    }

    DisposableEffect(lifecycleOwner, player) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (shouldAutoPlayState) {
                        player.playWhenReady = true
                        player.play()
                    }
                }
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    player.pause()
                    player.playWhenReady = false
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() {
                hasFirstFrame = true
            }

            override fun onPlayerError(error: PlaybackException) {
                if (!hasRequestedRefresh && isHttp403(error)) {
                    hasRequestedRefresh = true
                    onMediaForbidden()
                }
            }
        }
        player.addListener(listener)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            player.removeListener(listener)
            player.release()
        }
    }

    Box(modifier = modifier) {
        if (!hasFirstFrame) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }

        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    this.player = player
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.player = player
            }
        )
    }
}

private fun isHttp403(error: Throwable?): Boolean {
    var current: Throwable? = error
    while (current != null) {
        if (current is HttpDataSource.InvalidResponseCodeException && current.responseCode == 403) {
            return true
        }
        current = current.cause
    }
    return false
}

@Composable
private fun InteractionRail(
    post: PostDTO,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconTint = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RailItem(
            icon = if (post.likedByCurrentUser) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            count = post.likeCount.toString(),
            iconTint = if (post.likedByCurrentUser) MaterialTheme.colorScheme.error else iconTint,
            onClick = onLikeClick
        )
        RailItem(
            icon = Icons.Default.ChatBubbleOutline,
            count = post.totalComments.toString(),
            iconTint = iconTint,
            onClick = onCommentClick
        )
        RailItem(
            icon = if (post.savedByCurrentUser) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
            count = post.saveCount.toString(),
            iconTint = if (post.savedByCurrentUser) MaterialTheme.colorScheme.primary else iconTint,
            onClick = onSaveClick
        )
        RailItem(
            icon = Icons.Default.Share,
            count = post.shareCount.toString(),
            iconTint = iconTint,
            onClick = onShareClick
        )
        RailItem(
            icon = Icons.Default.Visibility,
            count = post.viewCount.toString(),
            iconTint = iconTint,
            onClick = {}
        )
    }
}

@Composable
private fun RailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: String,
    iconTint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.88f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = count,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
