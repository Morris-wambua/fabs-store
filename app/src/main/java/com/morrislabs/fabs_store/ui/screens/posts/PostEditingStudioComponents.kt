package com.morrislabs.fabs_store.ui.screens.posts

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.morrislabs.fabs_store.data.model.PostType
import com.morrislabs.fabs_store.data.model.SoundDTO

@Composable
fun StudioTopBar(
    onBack: () -> Unit,
    onAddSound: () -> Unit,
    onSaveDraft: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = onAddSound, shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F2F2F))) {
            Icon(Icons.Default.MusicNote, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Add sound")
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onSaveDraft) {
            Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.White)
        }
    }
}

@Composable
fun MediaPreviewPane(
    mediaUri: Uri?,
    postType: PostType,
    player: ExoPlayer?,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black)
    ) {
        when {
            postType == PostType.VIDEO && player != null -> {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = player
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            useController = false
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            mediaUri != null -> {
                AsyncImage(model = mediaUri, contentDescription = "Preview", modifier = Modifier.fillMaxSize())
            }

            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No media selected", color = Color.White)
                }
            }
        }

        if (postType == PostType.VIDEO) {
            IconButton(
                onClick = onTogglePlay,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.VideoLibrary,
                    contentDescription = "Toggle playback",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ToolRail(
    modifier: Modifier = Modifier,
    onTimeline: () -> Unit,
    onTrim: () -> Unit,
    onSpeed: () -> Unit,
    onFilters: () -> Unit,
    onText: () -> Unit,
    onEmoji: () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.End) {
        ToolRailButton("Edit", Icons.Default.Tune, onTimeline)
        ToolRailButton("Trim", Icons.Default.AutoAwesome, onTrim)
        ToolRailButton("Speed", Icons.Default.Speed, onSpeed)
        ToolRailButton("Filters", Icons.Default.Filter, onFilters)
        ToolRailButton("Text", Icons.Default.TextFields, onText)
        ToolRailButton("Emoji", Icons.Default.EmojiEmotions, onEmoji)
        ToolRailButton("Captions", Icons.Default.ClosedCaption) {}
    }
}

@Composable
private fun ToolRailButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick, modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)) {
            Icon(icon, contentDescription = label, tint = Color.White)
        }
        Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun TimelineSection(
    previewPositionMs: Float,
    trimRange: ClosedFloatingPointRange<Float>,
    selectedSound: SoundDTO?,
    textCount: Int,
    emojiCount: Int,
    onPositionChanged: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Timeline", color = Color.White, fontWeight = FontWeight.SemiBold)
        Text("Position ${previewPositionMs.toLong()} ms", color = Color(0xFFC2C2C2))
        Slider(
            value = previewPositionMs.coerceIn(trimRange.start, trimRange.endInclusive),
            onValueChange = onPositionChanged,
            valueRange = trimRange.start..trimRange.endInclusive
        )
        TrackRow("Video", Color(0xFF6E6E6E), "${trimRange.start.toLong()}-${trimRange.endInclusive.toLong()} ms")
        TrackRow("Sound", Color(0xFFFF2B55), selectedSound?.title ?: "No sound")
        TrackRow("Text", Color(0xFF3AA8FF), "$textCount overlays")
        TrackRow("Emoji", Color(0xFFF6C14A), "$emojiCount overlays")
    }
}

@Composable
private fun TrackRow(label: String, color: Color, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1F1F1F))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(label, color = Color.White, modifier = Modifier.width(52.dp))
        Text(value, color = Color(0xFFD4D4D4), maxLines = 1)
    }
}

@Composable
fun SoundStrip(
    selectedSound: SoundDTO?,
    onAddSound: () -> Unit,
    onRemoveSound: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF222222))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White)
        Spacer(Modifier.width(8.dp))
        Text(selectedSound?.title ?: "No sound selected", color = Color.White, modifier = Modifier.weight(1f))
        Button(onClick = onAddSound) { Text("Add") }
        Spacer(Modifier.width(6.dp))
        Button(onClick = onRemoveSound) { Text("Remove") }
    }
}
