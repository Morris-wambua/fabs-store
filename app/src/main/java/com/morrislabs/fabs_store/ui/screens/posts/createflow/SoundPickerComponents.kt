package com.morrislabs.fabs_store.ui.screens.posts.createflow

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.morrislabs.fabs_store.data.model.SoundDTO

@Composable
internal fun SoundRow(
    sound: SoundDTO,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderMod = if (isSelected) {
        Modifier.border(1.5.dp, PrimaryGreen, RoundedCornerShape(12.dp))
    } else Modifier

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(borderMod)
            .clickable { onSelect() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = sound.coverImageUrl,
            contentDescription = sound.title,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                sound.title ?: "Unknown",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Slate900
            )
            Text(
                buildString {
                    if (!sound.artistName.isNullOrBlank()) append(sound.artistName)
                    if (sound.duration > 0) {
                        if (isNotEmpty()) append(" . ")
                        append(formatTime(sound.duration))
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = Slate500,
                maxLines = 1
            )
        }

        if (isSelected) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = PrimaryGreen,
                modifier = Modifier.size(24.dp)
            )
        } else {
            IconButton(onClick = onBookmark, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = Slate500,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
internal fun SoundCategoryCard(
    sound: SoundDTO,
    onSelect: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onSelect() }
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = sound.coverImageUrl,
                contentDescription = sound.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x66000000)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            sound.title ?: "Unknown",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Slate900
        )
        if (sound.duration > 0) {
            Text(
                formatTime(sound.duration),
                style = MaterialTheme.typography.labelSmall,
                color = Slate500
            )
        }
    }
}

@Composable
internal fun CategoryHorizontalRow(
    title: String,
    sounds: List<SoundDTO>,
    onSelect: (SoundDTO) -> Unit
) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Slate900,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            sounds.forEach { sound ->
                SoundCategoryCard(sound = sound, onSelect = { onSelect(sound) })
            }
        }
    }
}

@Composable
internal fun SectionHeader(
    title: String,
    showSeeAll: Boolean = false,
    onSeeAll: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )
        if (showSeeAll) {
            Text(
                "See all",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = PrimaryGreen,
                modifier = Modifier.clickable { onSeeAll() }
            )
        }
    }
}

@Composable
internal fun SoundDetailOverlay(
    sound: SoundDTO,
    isPlaying: Boolean,
    currentPositionMs: Long,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onClose: () -> Unit,
    onDone: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x99000000))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF333333)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        "Select Sound",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White)
                            .clickable { onDone() }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "Done",
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF333333).copy(alpha = 0.9f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            sound.title ?: "Unknown",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            sound.artistName ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )

                        Spacer(Modifier.height(20.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(
                                Icons.Default.GridView,
                                Icons.Default.PlaylistPlay,
                                Icons.Default.Tune
                            ).forEach { icon ->
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1A1A1A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onTogglePlay, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            val progress = if (sound.duration > 0) {
                                (currentPositionMs.toFloat() / sound.duration).coerceIn(0f, 1f)
                            } else 0f

                            Slider(
                                value = progress,
                                onValueChange = onSeek,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = PrimaryGreen,
                                    activeTrackColor = PrimaryGreen,
                                    inactiveTrackColor = Color(0xFF555555)
                                )
                            )

                            Spacer(Modifier.width(8.dp))
                            Text(
                                formatTime(currentPositionMs),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        val remainingMs = (sound.duration - currentPositionMs).coerceAtLeast(0)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${remainingMs / 1000}s",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .width(160.dp)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1A1A1A))
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                WaveformBars(
                                    progress = if (sound.duration > 0) {
                                        currentPositionMs.toFloat() / sound.duration
                                    } else 0f
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WaveformBars(progress: Float) {
    val barHeights = listOf(0.3f, 0.6f, 0.9f, 0.5f, 0.8f, 1f, 0.4f, 0.7f, 0.5f, 0.9f,
        0.6f, 0.3f, 0.7f, 1f, 0.5f, 0.8f, 0.4f, 0.6f, 0.9f, 0.3f,
        0.7f, 0.5f, 0.8f, 0.6f, 1f, 0.4f, 0.7f, 0.3f, 0.9f, 0.5f)
    val totalBars = barHeights.size
    val filledBars = (progress * totalBars).toInt()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        barHeights.forEachIndexed { index, heightFraction ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height((24 * heightFraction).dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(
                        if (index < filledBars) PrimaryGreen
                        else Color(0xFF555555)
                    )
            )
        }
    }
}
