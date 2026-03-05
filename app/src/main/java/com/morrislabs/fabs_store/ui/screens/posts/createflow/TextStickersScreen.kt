package com.morrislabs.fabs_store.ui.screens.posts.createflow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

private val EditorBackground = Color(0xFF102216)
private val EditorSurface = Color(0xFF1A2E1F)

@Composable
fun TextStickersScreen(
    viewModel: CreatePostFlowViewModel,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onSaveDraft: () -> Unit,
    onSavePost: () -> Unit
) {
    val draft by viewModel.draft.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(TextStickerTab.STYLE) }
    var textInput by remember { mutableStateOf("") }
    var selectedTextStyle by remember { mutableStateOf(TextStyleType.MODERN) }
    var selectedTextColor by remember { mutableStateOf(0xFFFFFFFF) }

    val player = remember(draft.mediaUri) {
        draft.mediaUri?.let { uri ->
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(uri))
                repeatMode = Player.REPEAT_MODE_ONE
                prepare()
                playWhenReady = true
            }
        }
    }

    DisposableEffect(player) {
        onDispose { player?.release() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EditorBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TextStickersTopBar(
                onBack = onBack,
                onDone = onDone
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                VideoEditorPreview(
                    player = player,
                    overlays = draft.overlays,
                    onOverlayMoved = { viewModel.updateOverlay(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(460.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EditorSurface)
                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp)
            ) {
                TextStickerTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    modifier = Modifier.padding(top = 14.dp)
                )

                when (selectedTab) {
                    TextStickerTab.STYLE -> {
                        StyleEditorPanel(
                            selectedStyle = selectedTextStyle,
                            selectedColor = selectedTextColor,
                            textInput = textInput,
                            onTextInputChange = { textInput = it },
                            onStyleSelected = { selectedTextStyle = it },
                            onColorSelected = { selectedTextColor = it },
                            onAddText = {
                                if (textInput.isNotBlank()) {
                                    viewModel.addOverlay(
                                        OverlayItem.TextOverlay(
                                            id = "text_${System.currentTimeMillis()}",
                                            text = textInput.trim(),
                                            style = selectedTextStyle,
                                            color = selectedTextColor
                                        )
                                    )
                                    textInput = ""
                                }
                            }
                        )
                    }

                    TextStickerTab.STICKERS -> {
                        StickerGridPanel(
                            onStickerSelected = { sticker ->
                                viewModel.addOverlay(
                                    OverlayItem.StickerOverlay(
                                        id = "sticker_${System.currentTimeMillis()}",
                                        stickerType = sticker
                                    )
                                )
                            }
                        )
                    }
                }

                ActionBar(
                    onSaveDraft = onSaveDraft,
                    onSavePost = onSavePost,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 12.dp)
                        .navigationBarsPadding()
                )
            }
        }

        if (draft.mediaUri == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No media selected",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
