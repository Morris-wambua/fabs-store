package com.morrislabs.fabs_store.ui.screens.posts.createflow

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Rectangle
import androidx.compose.material.icons.filled.Square
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlin.math.roundToInt

internal val PrimaryGreen = Color(0xFF13EC5B)
internal val GreenAlpha10 = Color(0x1A13EC5B)
internal val Slate100 = Color(0xFFF1F5F9)
internal val Slate500 = Color(0xFF64748B)
internal val Slate900 = Color(0xFF0F172A)

@Composable
internal fun VideoPreview(
    player: ExoPlayer?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    trimStartMs: Long,
    trimEndMs: Long,
    onTogglePlay: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .heightIn(max = 400.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Slate900)
            .clickable { onTogglePlay() },
        contentAlignment = Alignment.Center
    ) {
        if (player != null) {
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

        if (!isPlaying) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0x66000000)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0x99000000))
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                val progress = if (durationMs > 0) {
                    ((currentPositionMs - trimStartMs).toFloat() /
                            (trimEndMs - trimStartMs).coerceAtLeast(1L)).coerceIn(0f, 1f)
                } else 0f

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.5.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(Color(0x4DFFFFFF))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(1.5.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(PrimaryGreen)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .padding(start = 0.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(PrimaryGreen)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(currentPositionMs), color = Color.White, fontSize = 12.sp)
                    Text(formatTime(durationMs), color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
internal fun TrimDurationSection(
    trimStartMs: Long,
    trimEndMs: Long,
    videoDurationMs: Long,
    thumbnails: List<Bitmap>,
    onTrimChanged: (Long, Long) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.ContentCut,
                contentDescription = null,
                tint = Slate500,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "TRIM DURATION",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = Slate500
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .background(GreenAlpha10, RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    "${formatTime(trimStartMs)} - ${formatTime(trimEndMs)}",
                    color = PrimaryGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        FilmstripTrimmer(
            thumbnails = thumbnails,
            trimStartMs = trimStartMs,
            trimEndMs = trimEndMs,
            videoDurationMs = videoDurationMs,
            onTrimChanged = onTrimChanged
        )
    }
}

@Composable
private fun FilmstripTrimmer(
    thumbnails: List<Bitmap>,
    trimStartMs: Long,
    trimEndMs: Long,
    videoDurationMs: Long,
    onTrimChanged: (Long, Long) -> Unit
) {
    val density = LocalDensity.current
    var containerWidthPx by remember { mutableFloatStateOf(0f) }
    val handleWidthDp = 16.dp
    val handleWidthPx = with(density) { handleWidthDp.toPx() }

    val startFraction = if (videoDurationMs > 0) trimStartMs.toFloat() / videoDurationMs else 0f
    val endFraction = if (videoDurationMs > 0) trimEndMs.toFloat() / videoDurationMs else 1f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Slate100)
            .onSizeChanged { containerWidthPx = it.width.toFloat() }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            thumbnails.forEach { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                )
            }
            if (thumbnails.isEmpty()) {
                repeat(6) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(Slate100)
                    )
                }
            }
        }

        val usableWidth = containerWidthPx - 2 * handleWidthPx
        val leftOffsetPx = handleWidthPx + startFraction * usableWidth
        val rightOffsetPx = handleWidthPx + endFraction * usableWidth

        if (startFraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(startFraction)
                    .height(64.dp)
                    .background(Color(0x80000000))
            )
        }
        if (endFraction < 1f) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .fillMaxWidth(1f - endFraction)
                    .height(64.dp)
                    .background(Color(0x80000000))
            )
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(leftOffsetPx.roundToInt(), 0) }
                .width(with(density) { ((rightOffsetPx - leftOffsetPx).coerceAtLeast(0f)).toDp() })
                .height(64.dp)
                .background(GreenAlpha10)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.TopCenter)
                    .background(PrimaryGreen)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.BottomCenter)
                    .background(PrimaryGreen)
            )
        }

        TrimHandle(
            offsetPx = (leftOffsetPx - handleWidthPx).roundToInt(),
            widthDp = handleWidthDp,
            shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp),
            onDrag = { dragAmount ->
                val newStartPx = (leftOffsetPx - handleWidthPx + dragAmount)
                    .coerceIn(0f, rightOffsetPx - handleWidthPx - handleWidthPx)
                val newFraction = (newStartPx / usableWidth).coerceIn(0f, endFraction - 0.05f)
                onTrimChanged((newFraction * videoDurationMs).toLong(), trimEndMs)
            },
            dragKey = videoDurationMs
        )

        TrimHandle(
            offsetPx = rightOffsetPx.roundToInt(),
            widthDp = handleWidthDp,
            shape = RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp),
            onDrag = { dragAmount ->
                val newEndPx = (rightOffsetPx + dragAmount)
                    .coerceIn(leftOffsetPx + handleWidthPx, containerWidthPx - handleWidthPx)
                val newFraction = ((newEndPx - handleWidthPx) / usableWidth)
                    .coerceIn(startFraction + 0.05f, 1f)
                onTrimChanged(trimStartMs, (newFraction * videoDurationMs).toLong())
            },
            dragKey = videoDurationMs
        )
    }
}

@Composable
private fun TrimHandle(
    offsetPx: Int,
    widthDp: androidx.compose.ui.unit.Dp,
    shape: RoundedCornerShape,
    onDrag: (Float) -> Unit,
    dragKey: Any
) {
    Box(
        modifier = Modifier
            .offset { IntOffset(offsetPx, 0) }
            .width(widthDp)
            .height(64.dp)
            .clip(shape)
            .background(PrimaryGreen)
            .pointerInput(dragKey) {
                detectHorizontalDragGestures { _, dragAmount -> onDrag(dragAmount) }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(Color(0x80FFFFFF))
        )
    }
}

@Composable
internal fun AspectRatioSection(
    selected: AspectRatioMode,
    onSelect: (AspectRatioMode) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Crop,
                contentDescription = null,
                tint = Slate500,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "ASPECT RATIO",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = Slate500
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Slate100),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AspectRatioMode.entries.forEach { mode ->
                val isSelected = mode == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .height(40.dp)
                        .then(
                            if (isSelected) Modifier
                                .shadow(4.dp, RoundedCornerShape(12.dp))
                                .background(Color.White, RoundedCornerShape(12.dp))
                            else Modifier
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelect(mode) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = mode.icon(),
                            contentDescription = mode.label(),
                            tint = if (isSelected) PrimaryGreen else Slate500,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            mode.label(),
                            color = if (isSelected) PrimaryGreen else Slate500,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun AspectRatioMode.icon(): ImageVector = when (this) {
    AspectRatioMode.RATIO_9_16 -> Icons.Default.PhoneIphone
    AspectRatioMode.RATIO_1_1 -> Icons.Default.Square
    AspectRatioMode.RATIO_4_5 -> Icons.Default.CropPortrait
    AspectRatioMode.RATIO_16_9 -> Icons.Default.Rectangle
}

private fun AspectRatioMode.label(): String = when (this) {
    AspectRatioMode.RATIO_9_16 -> "9:16"
    AspectRatioMode.RATIO_1_1 -> "1:1"
    AspectRatioMode.RATIO_4_5 -> "4:5"
    AspectRatioMode.RATIO_16_9 -> "16:9"
}

internal fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

internal fun extractThumbnails(uriString: String?, count: Int): List<Bitmap> {
    if (uriString == null) return emptyList()
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(uriString)
        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val durationUs = (durationStr?.toLongOrNull() ?: 0L) * 1000
        if (durationUs <= 0) return emptyList()
        val interval = durationUs / count
        (0 until count).mapNotNull { i ->
            retriever.getFrameAtTime(i * interval, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        }
    } catch (_: Exception) {
        emptyList()
    } finally {
        retriever.release()
    }
}
