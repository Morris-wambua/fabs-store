package com.morrislabs.fabs_store.ui.screens.posts

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.morrislabs.fabs_store.data.model.SoundDTO
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundTrimmerBottomSheet(
    sound: SoundDTO,
    maxDurationMs: Long = 60_000L,
    onDismiss: () -> Unit,
    onConfirm: (startMs: Long, endMs: Long) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val totalDuration = if (sound.duration > 0) sound.duration else 60_000L
    val clipLength = minOf(maxDurationMs, totalDuration)

    var offsetMs by remember { mutableLongStateOf(0L) }
    var isPlaying by remember { mutableStateOf(false) }
    val player = remember { createTrimmerPlayer(context, sound.audioUrl) }

    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }

    LaunchedEffect(offsetMs, isPlaying) {
        if (isPlaying) {
            player.seekTo(offsetMs)
            player.play()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            player.stop()
            onDismiss()
        },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    player.stop()
                    onDismiss()
                }) {
                    Text("Cancel")
                }
                Text(
                    "Trim Sound",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = {
                    player.stop()
                    onConfirm(offsetMs, offsetMs + clipLength)
                }) {
                    Text("Done", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            SoundInfoRow(sound)

            Spacer(modifier = Modifier.height(16.dp))

            WaveformTrimBar(
                totalDurationMs = totalDuration,
                clipLengthMs = clipLength,
                offsetMs = offsetMs,
                onOffsetChange = { newOffset ->
                    offsetMs = newOffset.coerceIn(0L, (totalDuration - clipLength).coerceAtLeast(0L))
                    if (isPlaying) {
                        player.seekTo(offsetMs)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    formatMs(offsetMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formatMs(offsetMs + clipLength),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                FilledTonalIconButton(
                    onClick = {
                        isPlaying = !isPlaying
                        if (isPlaying) {
                            player.seekTo(offsetMs)
                            player.play()
                        } else {
                            player.pause()
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Preview"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SoundInfoRow(sound: SoundDTO) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            Icons.Default.MusicNote,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                sound.title ?: "Unknown",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            if (!sound.artistName.isNullOrBlank()) {
                Text(
                    sound.artistName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WaveformTrimBar(
    totalDurationMs: Long,
    clipLengthMs: Long,
    offsetMs: Long,
    onOffsetChange: (Long) -> Unit
) {
    val barWidthDp = 320.dp
    val density = LocalDensity.current
    val barWidthPx = with(density) { barWidthDp.toPx() }
    val clipRatio = if (totalDurationMs > 0) clipLengthMs.toFloat() / totalDurationMs else 1f
    val highlightWidthPx = barWidthPx * clipRatio
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(offsetMs, totalDurationMs) {
        dragOffsetPx = if (totalDurationMs > 0) offsetMs.toFloat() / totalDurationMs * barWidthPx else 0f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.width(barWidthDp).height(56.dp)) {
            Canvas(
                modifier = Modifier
                    .width(barWidthDp)
                    .height(56.dp)
            ) {
                val barCount = 60
                val gap = size.width / barCount
                for (i in 0 until barCount) {
                    val h = (12f + (i * 7 + 13) % 30).dp.toPx()
                    val x = i * gap + gap / 2
                    drawRoundRect(
                        color = Color.Gray.copy(alpha = 0.3f),
                        topLeft = Offset(x - 2f, (size.height - h) / 2),
                        size = Size(4f, h),
                        cornerRadius = CornerRadius(2f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .offset { IntOffset(dragOffsetPx.roundToInt(), 0) }
                    .width(with(density) { highlightWidthPx.toDp() })
                    .height(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .pointerInput(totalDurationMs, clipLengthMs) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            val newPx = (dragOffsetPx + dragAmount)
                                .coerceIn(0f, barWidthPx - highlightWidthPx)
                            dragOffsetPx = newPx
                            val newMs = (newPx / barWidthPx * totalDurationMs).toLong()
                            onOffsetChange(newMs)
                        }
                    }
            ) {
                HighlightedWaveform(clipRatio)

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(3.dp)
                        .height(56.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(3.dp)
                        .height(56.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

@Composable
private fun BoxScope.HighlightedWaveform(clipRatio: Float) {
    Canvas(modifier = Modifier.matchParentSize()) {
        val barCount = (60 * clipRatio).toInt().coerceAtLeast(4)
        val gap = size.width / barCount
        for (i in 0 until barCount) {
            val h = (12f + (i * 7 + 13) % 30).dp.toPx()
            val x = i * gap + gap / 2
            drawRoundRect(
                color = Color(0xFF6200EE),
                topLeft = Offset(x - 2f, (size.height - h) / 2),
                size = Size(4f, h),
                cornerRadius = CornerRadius(2f)
            )
        }
    }
}

private fun createTrimmerPlayer(context: Context, audioUrl: String?): ExoPlayer {
    val player = ExoPlayer.Builder(context).build()
    if (!audioUrl.isNullOrBlank()) {
        player.setMediaItem(MediaItem.fromUri(Uri.parse(audioUrl)))
        player.prepare()
    }
    player.repeatMode = Player.REPEAT_MODE_ONE
    return player
}

private fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
