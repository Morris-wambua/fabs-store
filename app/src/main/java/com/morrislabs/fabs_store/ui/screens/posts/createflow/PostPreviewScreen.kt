package com.morrislabs.fabs_store.ui.screens.posts.createflow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.morrislabs.fabs_store.data.model.PostType

private val PreviewPrimaryGreen = Color(0xFF13EC5B)

@Composable
fun PostPreviewScreen(
    viewModel: CreatePostFlowViewModel,
    storeName: String,
    onEdit: () -> Unit,
    onPublish: () -> Unit
) {
    val draft by viewModel.draft.collectAsState()
    val context = LocalContext.current

    val isVideo = draft.postType == PostType.VIDEO

    val player = remember(draft.mediaUri, isVideo) {
        if (isVideo) {
            draft.mediaUri?.let { uri ->
                ExoPlayer.Builder(context).build().apply {
                    setMediaItem(MediaItem.fromUri(uri))
                    repeatMode = Player.REPEAT_MODE_ONE
                    prepare()
                    playWhenReady = true
                }
            }
        } else null
    }

    DisposableEffect(player) {
        onDispose { player?.release() }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (isVideo && player != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else if (!isVideo && draft.mediaUri != null) {
            coil.compose.AsyncImage(
                model = draft.mediaUri,
                contentDescription = "Image preview",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        OverlayPreviewContent(
            overlays = draft.overlays,
            caption = if (draft.caption.isBlank()) "Fresh looks this week #FlashSale #StyleInspo #ShoppingDay" else draft.caption,
            storeName = storeName,
            onEdit = onEdit,
            onPublish = onPublish
        )
    }
}

@Composable
private fun OverlayPreviewContent(
    overlays: List<OverlayItem>,
    caption: String,
    storeName: String,
    onEdit: () -> Unit,
    onPublish: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val height = constraints.maxHeight.toFloat().coerceAtLeast(1f)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 42.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33000000), contentColor = Color.White),
                shape = RoundedCornerShape(99.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null)
                Text("Edit")
            }
            Text(
                text = "PREVIEW",
                color = Color.White.copy(alpha = 0.8f),
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Box(modifier = Modifier.size(64.dp))
        }

        overlays.forEach { overlay ->
            val x = (overlay.normalizedX * width).toInt()
            val y = (overlay.normalizedY * height).toInt()
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = (x - 40).dp, top = (y - 20).dp)
            ) {
                when (overlay) {
                    is OverlayItem.TextOverlay -> Text(
                        text = overlay.text,
                        color = Color(overlay.color),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.background(Color(0x33000000), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    is OverlayItem.StickerOverlay -> Text(
                        text = stickerEmoji(overlay.stickerType),
                        fontSize = 24.sp
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 22.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                )
                Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
                    Text("@$storeName", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Sponsored", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
                ) {
                    Text("FOLLOW", fontSize = 10.sp)
                }
            }

            Text(caption, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)

            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.fillMaxWidth().size(2.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(99.dp)))
                }
                Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    ActionStat(Icons.Default.Favorite, "12.4K")
                    ActionStat(Icons.Default.Comment, "842")
                    ActionStat(Icons.Default.Share, "Share")
                }
            }

            Button(
                onClick = onPublish,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PreviewPrimaryGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Text(" Publish Post", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ActionStat(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(28.dp))
        Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

private fun stickerEmoji(type: StickerType): String {
    return when (type) {
        StickerType.LOCATION -> "📍"
        StickerType.MENTION -> "❓"
        StickerType.HASHTAG -> "#"
        StickerType.POLL -> "📊"
        StickerType.ASK_ME_ANYTHING -> "⏰"
        StickerType.LINK -> "🔗"
        StickerType.SHOPPING_CART -> "🛒"
        StickerType.STAR -> "⭐"
        StickerType.HEART -> "❤️"
        StickerType.FIRE -> "🔥"
        StickerType.NEW_RELEASES -> "🆕"
        StickerType.SELL -> "💸"
        StickerType.CELEBRATION -> "🎉"
        StickerType.STOREFRONT -> "🏪"
    }
}
