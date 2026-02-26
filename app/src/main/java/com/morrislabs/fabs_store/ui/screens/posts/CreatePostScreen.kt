package com.morrislabs.fabs_store.ui.screens.posts

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.morrislabs.fabs_store.data.model.PostType
import com.morrislabs.fabs_store.ui.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    storeId: String,
    postViewModel: PostViewModel,
    onNavigateBack: () -> Unit,
    onPostCreated: () -> Unit,
    onNavigateToLiveStream: () -> Unit = {}
) {
    val createPostState by postViewModel.createPostState.collectAsState()
    val uploadState by postViewModel.uploadState.collectAsState()
    val hashtagSuggestions by postViewModel.hashtagSuggestions.collectAsState()
    val showHashtagSuggestions by postViewModel.showHashtagSuggestions.collectAsState()
    val context = LocalContext.current

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var caption by rememberSaveable { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var mediaUrl by rememberSaveable { mutableStateOf("") }
    var mediaFilename by rememberSaveable { mutableStateOf("") }
    var postType by remember { mutableStateOf(PostType.IMAGE) }

    val mediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedUri = it
            val mimeType = context.contentResolver.getType(it)
            postType = if (mimeType?.startsWith("video/") == true) PostType.VIDEO else PostType.IMAGE
            postViewModel.uploadMedia(it) { url, filename ->
                mediaUrl = url
                mediaFilename = filename
            }
        }
    }

    LaunchedEffect(createPostState) {
        if (createPostState is PostViewModel.CreatePostState.Success) {
            onPostCreated()
        }
    }

    DisposableEffect(Unit) {
        onDispose { postViewModel.resetCreatePostState() }
    }

    val isUploading = uploadState is PostViewModel.UploadState.Uploading
    val isCreating = createPostState is PostViewModel.CreatePostState.Loading
    val canPost = storeId.isNotBlank() && mediaUrl.isNotBlank() && caption.isNotBlank() && !isUploading && !isCreating

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create Post",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            postViewModel.createPost(storeId, caption, mediaUrl, mediaFilename, postType)
                        },
                        enabled = canPost
                    ) {
                        Text(
                            "Post",
                            color = if (canPost) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                ToggleTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    MediaUploadArea(
                        selectedUri = selectedUri,
                        postType = postType,
                        isUploading = isUploading,
                        onSelectMedia = { mediaLauncher.launch("*/*") },
                        onRemoveMedia = {
                            selectedUri = null
                            mediaUrl = ""
                            mediaFilename = ""
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    CaptionSection(
                        caption = caption,
                        onCaptionChange = {
                            caption = it
                            postViewModel.onCaptionInputChanged(it)
                        },
                        suggestions = hashtagSuggestions.map { it.hashtag to it.usageCount },
                        showSuggestions = showHashtagSuggestions,
                        onTagClick = { tag ->
                            caption = applyHashtagSuggestion(caption, tag)
                            postViewModel.clearHashtagSuggestions()
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                } else {
                    GoLiveSection(onGoLive = onNavigateToLiveStream)
                }
            }

            if (selectedTab == 0) {
                ShareButton(
                    enabled = canPost,
                    isLoading = isCreating,
                    onClick = {
                        postViewModel.createPost(storeId, caption, mediaUrl, mediaFilename, postType)
                    }
                )
            }
        }
    }
}

@Composable
private fun ToggleTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clip(RoundedCornerShape(8.dp))
    ) {
        Tab(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            text = { Text("Image/Video") }
        )
        Tab(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            text = { Text("Go Live") }
        )
    }
}

@Composable
private fun MediaUploadArea(
    selectedUri: Uri?,
    postType: PostType,
    isUploading: Boolean,
    onSelectMedia: () -> Unit,
    onRemoveMedia: () -> Unit
) {
    val borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    val cornerRadius = 16.dp
    val context = LocalContext.current

    val videoImageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(VideoFrameDecoder.Factory()) }
            .build()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            .drawBehind {
                val stroke = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
                drawRoundRect(
                    color = borderColor,
                    cornerRadius = CornerRadius(cornerRadius.toPx()),
                    style = stroke
                )
            }
            .clickable(onClick = onSelectMedia),
        contentAlignment = Alignment.Center
    ) {
        if (selectedUri != null) {
            if (postType == PostType.VIDEO) {
                val videoModel = remember(selectedUri) {
                    ImageRequest.Builder(context)
                        .data(selectedUri)
                        .decoderFactory(VideoFrameDecoder.Factory())
                        .build()
                }
                AsyncImage(
                    model = videoModel,
                    imageLoader = videoImageLoader,
                    contentDescription = "Selected video thumbnail",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(cornerRadius)),
                    contentScale = ContentScale.Crop
                )
                Icon(
                    imageVector = Icons.Default.PlayCircleFilled,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White.copy(alpha = 0.85f)
                )
            } else {
                AsyncImage(
                    model = selectedUri,
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(cornerRadius)),
                    contentScale = ContentScale.Crop
                )
            }
            IconButton(
                onClick = onRemoveMedia,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove media",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            if (isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = "Upload media",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Upload Media",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Tap to select photos or videos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun CaptionSection(
    caption: String,
    onCaptionChange: (String) -> Unit,
    suggestions: List<Pair<String, Long>>,
    showSuggestions: Boolean,
    onTagClick: (String) -> Unit
) {
    Text(
        "Caption",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = caption,
        onValueChange = onCaptionChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text("Share what's happening at your store...")
        },
        minLines = 4,
        maxLines = 6,
        shape = RoundedCornerShape(12.dp)
    )
    if (showSuggestions && suggestions.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    RoundedCornerShape(12.dp)
                )
                .padding(6.dp)
        ) {
            HashtagSuggestionsSection(
                tags = suggestions,
                onTagClick = onTagClick
            )
        }
    }
}

@Composable
private fun HashtagSuggestionsSection(
    tags: List<Pair<String, Long>>,
    onTagClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tags.forEach { (tag, usageCount) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onTagClick(tag) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$usageCount uses",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun applyHashtagSuggestion(caption: String, hashtag: String): String {
    if (caption.isBlank()) return "$hashtag "
    var index = caption.length - 1
    while (index >= 0 && !caption[index].isWhitespace()) {
        index--
    }
    val tokenStart = index + 1
    val token = caption.substring(tokenStart)
    if (token.startsWith("#")) {
        return caption.substring(0, tokenStart) + hashtag + " "
    }
    return if (caption.endsWith(" ")) "$caption$hashtag " else "$caption $hashtag "
}

@Composable
private fun GoLiveSection(onGoLive: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.RocketLaunch,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Start a Live Stream",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Broadcast live to your customers and showcase your services in real time",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onGoLive,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Go Live", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ShareButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                Icons.Default.RocketLaunch,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Share to Feed",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
