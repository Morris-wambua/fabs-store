package com.morrislabs.fabs_store.ui.screens.posts.createflow

import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.morrislabs.fabs_store.data.model.SoundDTO
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundPickerScreen(
    viewModel: CreatePostFlowViewModel,
    sounds: List<SoundDTO>,
    isLoading: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onLoadTrending: () -> Unit,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val draft by viewModel.draft.collectAsState()
    val selectedSoundId = draft.soundSelection?.sound?.id
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var showDetailOverlay by remember { mutableStateOf(false) }
    var previewingSound by remember { mutableStateOf<SoundDTO?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    LaunchedEffect(Unit) { onLoadTrending() }

    LaunchedEffect(isPlaying, exoPlayer) {
        while (isPlaying) {
            currentPositionMs = exoPlayer.currentPosition
            if (!exoPlayer.isPlaying) isPlaying = false
            delay(200)
        }
    }

    fun previewSound(sound: SoundDTO) {
        exoPlayer.stop()
        if (!sound.audioUrl.isNullOrBlank()) {
            exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(sound.audioUrl)))
            exoPlayer.prepare()
            exoPlayer.play()
            isPlaying = true
            currentPositionMs = 0L
        }
        previewingSound = sound
        showDetailOverlay = true
    }

    fun selectSound(sound: SoundDTO) {
        viewModel.setSound(sound)
    }

    val trendingSounds = sounds.take(5)
    val upbeatSounds = sounds.drop(5).take(6)
    val relaxingSounds = sounds.drop(11).take(5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopAppBar(
            title = {
                Text(
                    "Select Sound",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    exoPlayer.stop()
                    onBack()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            },
            actions = { Spacer(Modifier.size(48.dp)) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = {
                Text(
                    "Search sounds, artists...",
                    color = Slate500
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = Slate500
                )
            },
            shape = RoundedCornerShape(50),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Slate100,
                focusedContainerColor = Slate100,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = PrimaryGreen
            )
        )

        Spacer(Modifier.height(8.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Slate900,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = if (selectedTab == 0) Slate900 else Slate500
                    )
                }
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "Discover",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) Slate900 else Slate500
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (selectedTab == 1) Slate900 else Slate500
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Favorites",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) Slate900 else Slate500
                        )
                    }
                }
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else if (sounds.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No sounds found", color = Slate500)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedTab == 0) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        SectionHeader(
                            title = "Trending",
                            showSeeAll = true,
                            onSeeAll = {}
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    items(trendingSounds, key = { "trending_${it.id}" }) { sound ->
                        SoundRow(
                            sound = sound,
                            isSelected = sound.id == selectedSoundId,
                            onSelect = {
                                selectSound(sound)
                                previewSound(sound)
                            },
                            onBookmark = {},
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    if (upbeatSounds.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            CategoryHorizontalRow(
                                title = "Upbeat",
                                sounds = upbeatSounds,
                                onSelect = { sound ->
                                    selectSound(sound)
                                    previewSound(sound)
                                }
                            )
                        }
                    }

                    if (relaxingSounds.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            SectionHeader(title = "Relaxing")
                            Spacer(Modifier.height(8.dp))
                        }

                        items(relaxingSounds, key = { "relaxing_${it.id}" }) { sound ->
                            SoundRow(
                                sound = sound,
                                isSelected = sound.id == selectedSoundId,
                                onSelect = {
                                    selectSound(sound)
                                    previewSound(sound)
                                },
                                onBookmark = {},
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No favorites yet", color = Slate500)
                        }
                    }
                }
            }
        }

        val hasSelection = selectedSoundId != null
        Button(
            onClick = {
                exoPlayer.stop()
                onDone()
            },
            enabled = hasSelection,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(56.dp)
                .shadow(
                    8.dp,
                    RoundedCornerShape(50),
                    spotColor = Color(0x3313EC5B)
                ),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen,
                disabledContainerColor = PrimaryGreen.copy(alpha = 0.4f)
            )
        ) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = Slate900,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Use this sound",
                color = Slate900,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (showDetailOverlay && previewingSound != null) {
        SoundDetailOverlay(
            sound = previewingSound!!,
            isPlaying = isPlaying,
            currentPositionMs = currentPositionMs,
            onTogglePlay = {
                if (isPlaying) {
                    exoPlayer.pause()
                    isPlaying = false
                } else {
                    exoPlayer.play()
                    isPlaying = true
                }
            },
            onSeek = { fraction ->
                val seekMs = (fraction * (previewingSound?.duration ?: 0L)).toLong()
                exoPlayer.seekTo(seekMs)
                currentPositionMs = seekMs
            },
            onClose = {
                exoPlayer.pause()
                isPlaying = false
                showDetailOverlay = false
            },
            onDone = {
                exoPlayer.stop()
                isPlaying = false
                showDetailOverlay = false
                onDone()
            }
        )
    }
}
