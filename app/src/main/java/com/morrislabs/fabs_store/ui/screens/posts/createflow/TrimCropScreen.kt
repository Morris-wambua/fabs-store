package com.morrislabs.fabs_store.ui.screens.posts.createflow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.morrislabs.fabs_store.data.model.PostType
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrimCropScreen(
    viewModel: CreatePostFlowViewModel,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val draft by viewModel.draft.collectAsState()
    val context = LocalContext.current
    val isVideo = draft.postType == PostType.VIDEO

    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(draft.trim.startMs) }
    var videoDurationMs by remember { mutableLongStateOf(15_000L) }

    val exoPlayer = remember(draft.mediaUri, isVideo) {
        if (isVideo) {
            draft.mediaUri?.let { uri ->
                ExoPlayer.Builder(context).build().apply {
                    setMediaItem(MediaItem.fromUri(uri))
                    repeatMode = ExoPlayer.REPEAT_MODE_ONE
                    prepare()
                    playWhenReady = false
                }
            }
        } else null
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer?.release() }
    }

    if (isVideo) {
        LaunchedEffect(exoPlayer) {
            exoPlayer?.let { player ->
                while (player.duration <= 0) delay(100)
                videoDurationMs = player.duration
                if (draft.trim.endMs == 15_000L || draft.trim.endMs > player.duration) {
                    viewModel.setTrimRange(draft.trim.startMs, player.duration)
                }
            }
        }

        LaunchedEffect(draft.trim.startMs, draft.trim.endMs, exoPlayer) {
            exoPlayer?.let { player ->
                val pos = player.currentPosition
                if (pos < draft.trim.startMs || pos > draft.trim.endMs) {
                    player.seekTo(draft.trim.startMs)
                    currentPositionMs = draft.trim.startMs
                }
            }
        }

        LaunchedEffect(isPlaying, exoPlayer, draft.trim) {
            exoPlayer?.playWhenReady = isPlaying
            while (isPlaying && exoPlayer != null) {
                currentPositionMs = exoPlayer.currentPosition
                if (currentPositionMs >= draft.trim.endMs) {
                    exoPlayer.seekTo(draft.trim.startMs)
                }
                delay(100)
            }
        }
    }

    val thumbnails = remember(draft.mediaUri, isVideo) {
        if (isVideo) extractThumbnails(draft.mediaUri?.toString(), 6) else emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopAppBar(
            title = {
                Text(
                    if (isVideo) "Edit Video" else "Edit Image",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            },
            actions = {
                Text(
                    "Save",
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onSave() }
                        .padding(horizontal = 16.dp)
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )
        Divider(color = Color(0xFFE2E8F0))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            if (isVideo) {
                VideoPreview(
                    player = exoPlayer,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    durationMs = videoDurationMs,
                    trimStartMs = draft.trim.startMs,
                    trimEndMs = draft.trim.endMs,
                    aspectRatio = draft.crop.aspectRatio,
                    onTogglePlay = {
                        isPlaying = !isPlaying
                        if (isPlaying) exoPlayer?.seekTo(draft.trim.startMs)
                    }
                )

                Spacer(Modifier.height(24.dp))

                TrimDurationSection(
                    trimStartMs = draft.trim.startMs,
                    trimEndMs = draft.trim.endMs,
                    videoDurationMs = videoDurationMs,
                    thumbnails = thumbnails,
                    onTrimChanged = { start, end -> viewModel.setTrimRange(start, end) }
                )
            } else {
                ImagePreview(
                    imageUri = draft.mediaUri,
                    aspectRatio = draft.crop.aspectRatio
                )
            }

            Spacer(Modifier.height(24.dp))

            AspectRatioSection(
                selected = draft.crop.aspectRatio,
                onSelect = { viewModel.setAspectRatio(it) }
            )

            Spacer(Modifier.height(24.dp))
        }

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0x3313EC5B)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Slate900,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (isVideo) "Save Trim & Crop" else "Save Crop",
                color = Slate900,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
