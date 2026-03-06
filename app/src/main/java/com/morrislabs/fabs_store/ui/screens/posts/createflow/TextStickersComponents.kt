package com.morrislabs.fabs_store.ui.screens.posts.createflow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlin.math.roundToInt

enum class TextStickerTab { STYLE, STICKERS }

private val StickerPrimaryGreen = Color(0xFF13EC5B)
private val DarkSurface = Color(0xFF1A2E1F)
private val DarkBorder = Color(0xFF2D4A36)

@Composable
fun TextStickersTopBar(
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xAA0C1A10))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Text(
            text = "Add Text & Stickers",
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Done",
            color = StickerPrimaryGreen,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onDone)
        )
    }
}

@Composable
fun VideoEditorPreview(
    player: ExoPlayer?,
    overlays: List<OverlayItem>,
    onOverlayMoved: (OverlayItem) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val maxWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val maxHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)

        if (player != null) {
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        this.player = player
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        overlays.forEach { overlay ->
            DraggableOverlayItem(
                overlay = overlay,
                maxWidthPx = maxWidthPx,
                maxHeightPx = maxHeightPx,
                onOverlayMoved = onOverlayMoved
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(14.dp)
                .background(Color(0x66000000), RoundedCornerShape(99.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.TextFields, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(99.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .height(4.dp)
                        .background(StickerPrimaryGreen, RoundedCornerShape(99.dp))
                )
            }
            Text("0:04", color = Color.White, fontSize = 12.sp)
        }
    }
}

@Composable
private fun DraggableOverlayItem(
    overlay: OverlayItem,
    maxWidthPx: Float,
    maxHeightPx: Float,
    onOverlayMoved: (OverlayItem) -> Unit
) {
    var itemWidthPx by remember(overlay.id) { mutableStateOf(1f) }
    var itemHeightPx by remember(overlay.id) { mutableStateOf(1f) }

    val startX = (overlay.normalizedX * maxWidthPx) - (itemWidthPx / 2f)
    val startY = (overlay.normalizedY * maxHeightPx) - (itemHeightPx / 2f)

    Box(
        modifier = Modifier
            .offset { IntOffset(startX.roundToInt(), startY.roundToInt()) }
            .onSizeChanged {
                itemWidthPx = it.width.toFloat().coerceAtLeast(1f)
                itemHeightPx = it.height.toFloat().coerceAtLeast(1f)
            }
            .pointerInput(overlay.id, maxWidthPx, maxHeightPx, itemWidthPx, itemHeightPx) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val currentLeft = (overlay.normalizedX * maxWidthPx) - (itemWidthPx / 2f)
                    val currentTop = (overlay.normalizedY * maxHeightPx) - (itemHeightPx / 2f)
                    val newLeft = (currentLeft + dragAmount.x).coerceIn(0f, maxWidthPx - itemWidthPx)
                    val newTop = (currentTop + dragAmount.y).coerceIn(0f, maxHeightPx - itemHeightPx)
                    val updatedX = ((newLeft + itemWidthPx / 2f) / maxWidthPx).coerceIn(0f, 1f)
                    val updatedY = ((newTop + itemHeightPx / 2f) / maxHeightPx).coerceIn(0f, 1f)
                    val updated = when (overlay) {
                        is OverlayItem.TextOverlay -> overlay.copy(normalizedX = updatedX, normalizedY = updatedY)
                        is OverlayItem.StickerOverlay -> overlay.copy(normalizedX = updatedX, normalizedY = updatedY)
                    }
                    onOverlayMoved(updated)
                }
            }
    ) {
        when (overlay) {
            is OverlayItem.TextOverlay -> {
                val styleWeight = when (overlay.style) {
                    TextStyleType.MODERN -> FontWeight.Medium
                    TextStyleType.CLASSIC -> FontWeight.Normal
                    TextStyleType.BOLD -> FontWeight.Bold
                    TextStyleType.SERIF -> FontWeight.SemiBold
                }
                Text(
                    text = overlay.text,
                    color = Color(overlay.color),
                    fontWeight = styleWeight,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .background(Color(0x33000000), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            is OverlayItem.StickerOverlay -> {
                val (icon, tint) = stickerUi(overlay.stickerType)
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0x33000000), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = overlay.stickerType.name, tint = tint, modifier = Modifier.size(26.dp))
                }
            }
        }
    }
}

@Composable
fun TextStickerTabs(
    selectedTab: TextStickerTab,
    onTabSelected: (TextStickerTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        listOf(TextStickerTab.STYLE to "Style", TextStickerTab.STICKERS to "Stickers").forEach { (tab, label) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onTabSelected(tab) }
            ) {
                Text(
                    text = label,
                    color = if (selectedTab == tab) Color.White else Color(0xFFA2B4A8),
                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .height(2.dp)
                        .width(32.dp)
                        .background(if (selectedTab == tab) StickerPrimaryGreen else Color.Transparent)
                )
            }
        }
    }
}

@Composable
fun StyleEditorPanel(
    selectedStyle: TextStyleType,
    selectedColor: Long,
    textInput: String,
    onTextInputChange: (String) -> Unit,
    onStyleSelected: (TextStyleType) -> Unit,
    onColorSelected: (Long) -> Unit,
    onAddText: () -> Unit
) {
    val styles = listOf(
        TextStyleType.MODERN to "Modern",
        TextStyleType.CLASSIC to "Classic",
        TextStyleType.BOLD to "Bold",
        TextStyleType.SERIF to "Serif"
    )
    val colors = listOf(0xFF13EC5B, 0xFFFFFFFF, 0xFF000000, 0xFFF43F5E, 0xFF3B82F6, 0xFFEAB308)

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        styles.forEach { (style, label) ->
            val selected = selectedStyle == style
            Box(
                modifier = Modifier
                    .background(if (selected) StickerPrimaryGreen else DarkSurface, RoundedCornerShape(99.dp))
                    .border(1.dp, if (selected) StickerPrimaryGreen else DarkBorder, RoundedCornerShape(99.dp))
                    .clickable { onStyleSelected(style) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = label,
                    color = if (selected) Color.Black else Color(0xFFA2B4A8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        colors.forEach { colorLong ->
            val selected = colorLong == selectedColor
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(Color(colorLong), CircleShape)
                    .border(if (selected) 2.dp else 0.dp, Color.White, CircleShape)
                    .clickable { onColorSelected(colorLong) }
            )
        }
        Box(
            modifier = Modifier.size(30.dp).background(Color(0xFF263B2D), CircleShape).clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Palette, contentDescription = "Palette", tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }

    OutlinedTextField(
        value = textInput,
        onValueChange = onTextInputChange,
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        placeholder = { Text("Enter overlay text", color = Color(0xFF8DA194)) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color(0x33000000),
            unfocusedContainerColor = Color(0x33000000),
            focusedBorderColor = DarkBorder,
            unfocusedBorderColor = DarkBorder,
            cursorColor = StickerPrimaryGreen
        ),
        shape = RoundedCornerShape(14.dp),
        trailingIcon = {
            IconButton(onClick = onAddText) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Add text", tint = StickerPrimaryGreen)
            }
        }
    )
}

@Composable
fun StickerGridPanel(onStickerSelected: (StickerType) -> Unit) {
    var query by remember { mutableStateOf("") }
    val stickers = listOf(
        StickerType.LOCATION,
        StickerType.MENTION,
        StickerType.HASHTAG,
        StickerType.POLL,
        StickerType.SHOPPING_CART,
        StickerType.STAR,
        StickerType.HEART,
        StickerType.FIRE,
        StickerType.NEW_RELEASES,
        StickerType.SELL,
        StickerType.CELEBRATION,
        StickerType.STOREFRONT,
        StickerType.ASK_ME_ANYTHING,
        StickerType.LINK
    )
    val filtered = stickers.filter { it.name.contains(query, ignoreCase = true) }

    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        placeholder = { Text("Search stickers...", color = Color(0xFF8DA194)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8DA194)) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color(0x33000000),
            unfocusedContainerColor = Color(0x33000000),
            focusedBorderColor = DarkBorder,
            unfocusedBorderColor = DarkBorder,
            cursorColor = StickerPrimaryGreen
        ),
        shape = RoundedCornerShape(14.dp)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.fillMaxWidth().height(180.dp).padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filtered) { sticker ->
            val (icon, tint) = stickerUi(sticker)
            val isActiveGreen = sticker in GreenActiveSet
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(Color(0x33000000), RoundedCornerShape(16.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .clickable { onStickerSelected(sticker) },
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = sticker.name, tint = if (isActiveGreen) StickerPrimaryGreen else tint, modifier = Modifier.size(26.dp))
            }
        }
    }
}

@Composable
fun ActionBar(
    onSaveDraft: () -> Unit,
    onSavePost: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = onSaveDraft,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102216), contentColor = Color.White),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Drafts, contentDescription = null)
            Text(" Save Draft")
        }

        Button(
            onClick = onSavePost,
            modifier = Modifier.weight(2f),
            colors = ButtonDefaults.buttonColors(containerColor = StickerPrimaryGreen, contentColor = Color.Black),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Text(" Save Post", fontWeight = FontWeight.Bold)
        }
    }
}
