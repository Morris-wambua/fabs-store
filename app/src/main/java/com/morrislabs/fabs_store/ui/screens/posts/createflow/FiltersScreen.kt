package com.morrislabs.fabs_store.ui.screens.posts.createflow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

private val FilterPanelBg = Color.Black
private val CloseButtonBg = Color(0xFF333333)
private val SoundPillBg = Color(0x66000000)
private val NextButtonPink = Color(0xFFFF2B55)
private val SelectedGreen = Color(0xFF13EC5B)
private val FilterItemBg = Color(0xFF333333)
private val WhiteDimmed = Color(0x99FFFFFF)

private val filterOptions = listOf(
    "Original", "Vibrant", "Soft Glow", "Professional", "Bold", "Warm", "Cool", "Mono"
)

private val filterColors = mapOf(
    "Vibrant" to Color(0xFFFF6B35),
    "Soft Glow" to Color(0xFFFFC3A0),
    "Professional" to Color(0xFF4A90D9),
    "Bold" to Color(0xFFE74C3C),
    "Warm" to Color(0xFFF39C12),
    "Cool" to Color(0xFF3498DB),
    "Mono" to Color(0xFF7F8C8D)
)

@Composable
fun FiltersScreen(
    viewModel: CreatePostFlowViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val draft by viewModel.draft.collectAsState()
    val context = LocalContext.current
    var localIntensity by remember(draft.filter.name) {
        mutableFloatStateOf(draft.filter.intensity)
    }

    val exoPlayer = remember(draft.mediaUri) {
        draft.mediaUri?.let { uri ->
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(uri))
                repeatMode = ExoPlayer.REPEAT_MODE_ONE
                prepare()
                playWhenReady = true
            }
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer?.release() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Video preview - fills entire screen
        if (exoPlayer != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = exoPlayer
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        useController = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top bar overlay
        TopBarOverlay(
            soundName = draft.soundSelection?.sound?.title,
            onClose = onBack,
            onNext = onNext
        )

        // Bottom filter panel
        FilterPanel(
            selectedFilter = draft.filter.name,
            intensity = localIntensity,
            onFilterSelected = { name ->
                viewModel.setFilter(name, localIntensity)
            },
            onIntensityChanged = { value ->
                localIntensity = value
                viewModel.setFilter(draft.filter.name, value)
            },
            onReset = {
                localIntensity = 1.0f
                viewModel.setFilter("Original", 1.0f)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun TopBarOverlay(
    soundName: String?,
    onClose: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Close button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CloseButtonBg)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        // Sound pill
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(SoundPillBg)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = soundName ?: "Store Summer Vibes",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.weight(1f))

        // Next button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(NextButtonPink)
                .clickable(onClick = onNext)
                .padding(horizontal = 24.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Next",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun FilterPanel(
    selectedFilter: String,
    intensity: Float,
    onFilterSelected: (String) -> Unit,
    onIntensityChanged: (Float) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(FilterPanelBg)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 16.dp)
    ) {
        // Title row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Filters",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onReset),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = WhiteDimmed,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Intensity label row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "INTENSITY",
                color = WhiteDimmed,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
            Text(
                "${(intensity * 100).toInt()}%",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(4.dp))

        // Intensity slider
        Slider(
            value = intensity,
            onValueChange = onIntensityChanged,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Filter items
        FilterItemsRow(
            selectedFilter = selectedFilter,
            onFilterSelected = onFilterSelected
        )
    }
}

@Composable
private fun FilterItemsRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        filterOptions.forEach { name ->
            FilterItem(
                name = name,
                isSelected = name == selectedFilter,
                onClick = { onFilterSelected(name) }
            )
        }
    }
}

@Composable
private fun FilterItem(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick)
    ) {
        val isOriginal = name == "Original"
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isOriginal) FilterItemBg else (filterColors[name] ?: FilterItemBg))
                .then(
                    if (isSelected && !isOriginal) Modifier.border(
                        2.dp, SelectedGreen, RoundedCornerShape(16.dp)
                    ) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isOriginal) {
                Icon(
                    Icons.Default.Block,
                    contentDescription = "Original",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            name,
            color = if (isSelected) SelectedGreen else Color.White,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
