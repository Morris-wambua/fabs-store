package com.morrislabs.fabs_store.ui.screens.posts.createflow

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

internal val GreenActiveSet = setOf(
    StickerType.LOCATION,
    StickerType.POLL,
    StickerType.ASK_ME_ANYTHING,
    StickerType.MENTION
)

internal fun stickerUi(type: StickerType): Pair<ImageVector, Color> {
    return when (type) {
        StickerType.LOCATION -> Icons.Default.LocationOn to Color(0xFF94A3B8)
        StickerType.MENTION -> Icons.Default.Quiz to Color(0xFF94A3B8)
        StickerType.HASHTAG -> Icons.Default.TextFields to Color(0xFF94A3B8)
        StickerType.POLL -> Icons.Default.Poll to Color(0xFF94A3B8)
        StickerType.ASK_ME_ANYTHING -> Icons.Default.Schedule to Color(0xFF94A3B8)
        StickerType.LINK -> Icons.Default.Palette to Color(0xFF94A3B8)
        StickerType.SHOPPING_CART -> Icons.Default.ShoppingCart to Color(0xFFCBD5E1)
        StickerType.STAR -> Icons.Default.Star to Color(0xFFCBD5E1)
        StickerType.HEART -> Icons.Default.Favorite to Color(0xFFCBD5E1)
        StickerType.FIRE -> Icons.Default.LocalFireDepartment to Color(0xFFCBD5E1)
        StickerType.NEW_RELEASES -> Icons.Default.NewReleases to Color(0xFFCBD5E1)
        StickerType.SELL -> Icons.Default.Sell to Color(0xFFCBD5E1)
        StickerType.CELEBRATION -> Icons.Default.Celebration to Color(0xFFCBD5E1)
        StickerType.STOREFRONT -> Icons.Default.Storefront to Color(0xFFCBD5E1)
    }
}
