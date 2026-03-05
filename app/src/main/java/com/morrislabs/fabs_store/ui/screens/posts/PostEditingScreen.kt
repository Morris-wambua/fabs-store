package com.morrislabs.fabs_store.ui.screens.posts

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer
import com.morrislabs.fabs_store.data.model.PostType
import com.morrislabs.fabs_store.data.model.SoundDTO
import kotlinx.coroutines.delay

private const val MAX_PREVIEW_DURATION_MS = 60_000f

enum class EditorTab {
    TIMELINE, TRIM, SPEED, FILTERS, TEXT, EMOJI
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostEditingScreen(
    mediaUri: Uri?,
    postType: PostType,
    selectedSound: SoundDTO?,
    initialState: PostEditingState,
    onBack: () -> Unit,
    onPickMedia: () -> Unit,
    onAddSound: () -> Unit,
    onRemoveSound: () -> Unit,
    onApply: (PostEditingState) -> Unit,
    onSaveDraft: (PostEditingState) -> Unit
) {
    var trimRange by remember {
        mutableStateOf(initialState.videoTrimStartMs.toFloat()..initialState.videoTrimEndMs.toFloat())
    }
    var selectedFilter by remember { mutableStateOf(initialState.filterName) }
    var videoSpeed by remember { mutableStateOf(initialState.videoSpeed) }
    var previewPositionMs by remember { mutableStateOf(initialState.previewPositionMs.toFloat()) }
    var isPlaying by remember { mutableStateOf(false) }
    var activeSheet by remember { mutableStateOf<EditorTab?>(null) }

    val textOverlays = remember { mutableStateListOf<String>().apply { addAll(initialState.textOverlays) } }
    val emojiOverlays = remember { mutableStateListOf<String>().apply { addAll(initialState.emojiOverlays) } }
    var overlayInput by remember { mutableStateOf("") }

    val filterOptions = listOf("None", "Portrait", "Film", "Soft", "Cool", "Mono", "Vivid")
    val speedOptions = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)
    val emojiOptions = listOf(":fire:", ":sparkles:", ":heart:", ":ok:", ":star:", ":camera:")

    val context = androidx.compose.ui.platform.LocalContext.current
    val exoPlayer = remember(mediaUri, postType) {
        if (postType == PostType.VIDEO && mediaUri != null) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(mediaUri))
                repeatMode = ExoPlayer.REPEAT_MODE_ONE
                prepare()
                playWhenReady = false
            }
        } else null
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer?.release()
        }
    }

    LaunchedEffect(initialState) {
        trimRange = initialState.videoTrimStartMs.toFloat()..initialState.videoTrimEndMs.toFloat()
        selectedFilter = initialState.filterName
        videoSpeed = initialState.videoSpeed
        previewPositionMs = initialState.previewPositionMs.toFloat()
        textOverlays.clear()
        textOverlays.addAll(initialState.textOverlays)
        emojiOverlays.clear()
        emojiOverlays.addAll(initialState.emojiOverlays)
    }

    LaunchedEffect(videoSpeed, exoPlayer) {
        exoPlayer?.setPlaybackParameters(PlaybackParameters(videoSpeed))
    }

    LaunchedEffect(isPlaying, exoPlayer) {
        exoPlayer?.playWhenReady = isPlaying
    }

    LaunchedEffect(isPlaying, exoPlayer, trimRange) {
        while (isPlaying && exoPlayer != null) {
            previewPositionMs = exoPlayer.currentPosition.toFloat()
            if (previewPositionMs > trimRange.endInclusive) {
                exoPlayer.seekTo(trimRange.start.toLong())
            }
            delay(120)
        }
    }

    val stateSnapshot = PostEditingState(
        videoTrimStartMs = trimRange.start.toLong(),
        videoTrimEndMs = trimRange.endInclusive.toLong(),
        videoSpeed = videoSpeed,
        previewPositionMs = previewPositionMs.toLong(),
        filterName = selectedFilter,
        textOverlays = textOverlays.toList(),
        emojiOverlays = emojiOverlays.toList()
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070707))
    ) {
        MediaPreviewPane(
            mediaUri = mediaUri,
            postType = postType,
            player = exoPlayer,
            isPlaying = isPlaying,
            onTogglePlay = { isPlaying = !isPlaying }
        )

        StudioTopBar(
            onBack = onBack,
            onAddSound = onAddSound,
            onSaveDraft = { onSaveDraft(stateSnapshot) }
        )

        ToolRail(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp),
            activeTab = activeSheet,
            onTabSelected = { activeSheet = it },
            drawerContent = { tab ->
                when (tab) {
                    EditorTab.TIMELINE -> {
                        TimelineSection(
                            previewPositionMs = previewPositionMs,
                            trimRange = trimRange,
                            selectedSound = selectedSound,
                            textCount = textOverlays.size,
                            emojiCount = emojiOverlays.size,
                            onPositionChanged = {
                                previewPositionMs = it
                                exoPlayer?.seekTo(it.toLong())
                            }
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = onPickMedia) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Replace")
                            }
                            Button(onClick = onAddSound) {
                                Icon(Icons.Default.MusicNote, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Audio")
                            }
                        }
                        SoundStrip(
                            selectedSound = selectedSound,
                            onAddSound = onAddSound,
                            onRemoveSound = onRemoveSound
                        )
                    }

                    EditorTab.TRIM -> {
                        Text("Trim", color = Color.White)
                        RangeSlider(
                            value = trimRange,
                            onValueChange = { trimRange = it },
                            valueRange = 0f..MAX_PREVIEW_DURATION_MS
                        )
                        Text(
                            "${trimRange.start.toLong()} ms  -  ${trimRange.endInclusive.toLong()} ms",
                            color = Color(0xFFC9C9C9)
                        )
                    }

                    EditorTab.SPEED -> {
                        Text("Speed", color = Color.White)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            speedOptions.forEach { option ->
                                FilterChip(
                                    selected = option == videoSpeed,
                                    onClick = {
                                        videoSpeed = option
                                        exoPlayer?.setPlaybackParameters(PlaybackParameters(option))
                                    },
                                    label = { Text("${option}x") }
                                )
                            }
                        }
                    }

                    EditorTab.FILTERS -> {
                        Text("Filters", color = Color.White)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filterOptions.forEach { filter ->
                                FilterChip(
                                    selected = selectedFilter == filter,
                                    onClick = { selectedFilter = filter },
                                    label = { Text(filter) }
                                )
                            }
                        }
                    }

                    EditorTab.TEXT -> {
                        Text("Text overlays", color = Color.White)
                        OutlinedTextField(
                            value = overlayInput,
                            onValueChange = { overlayInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Type caption text") }
                        )
                        Button(onClick = {
                            if (overlayInput.isNotBlank()) {
                                textOverlays.add(overlayInput.trim())
                                overlayInput = ""
                            }
                        }) {
                            Text("Add Text")
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            textOverlays.forEach { value ->
                                AssistChip(onClick = { textOverlays.remove(value) }, label = { Text(value) })
                            }
                        }
                    }

                    EditorTab.EMOJI -> {
                        Text("Emoji overlays", color = Color.White)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            emojiOptions.forEach { symbol ->
                                AssistChip(onClick = { emojiOverlays.add(symbol) }, label = { Text(symbol) })
                            }
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            emojiOverlays.forEach { item ->
                                AssistChip(onClick = { emojiOverlays.remove(item) }, label = { Text(item) })
                            }
                        }
                    }
                }
            }
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp)
        ) {
            Button(
                onClick = { onApply(stateSnapshot) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2B55)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Preview")
            }
            Button(
                onClick = { onApply(stateSnapshot) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Next")
            }
        }
    }
}