package com.morrislabs.fabs_store.ui.screens.posts.createflow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Drafts
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

internal val GreenActiveSet = setOf(
    StickerType.LOCATION,
    StickerType.POLL,
    StickerType.ASK_ME_ANYTHING,
    StickerType.MENTION
)

private val StickerGreen = Color(0xFF13EC5B)

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

internal fun interactiveStickerMeta(type: StickerType): Pair<Color, String> {
    return when (type) {
        StickerType.LOCATION -> Color(0xFF3B82F6) to "Location"
        StickerType.MENTION -> Color(0xFFF97316) to "Mention"
        StickerType.HASHTAG -> Color(0xFF8B5CF6) to "Hashtag"
        StickerType.POLL -> Color(0xFF13EC5B) to "Poll"
        StickerType.ASK_ME_ANYTHING -> Color(0xFF13EC5B) to "AMA"
        StickerType.LINK -> Color(0xFFEC4899) to "Link"
        else -> Color(0xFF94A3B8) to type.name
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
                        .background(if (selectedTab == tab) StickerGreen else Color.Transparent)
                )
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
            colors = ButtonDefaults.buttonColors(containerColor = StickerGreen, contentColor = Color.Black),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Text(" Save Post", fontWeight = FontWeight.Bold)
        }
    }
}
