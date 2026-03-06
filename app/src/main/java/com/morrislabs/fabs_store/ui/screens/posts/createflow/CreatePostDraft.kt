package com.morrislabs.fabs_store.ui.screens.posts.createflow

import android.net.Uri
import com.morrislabs.fabs_store.data.model.PostType
import com.morrislabs.fabs_store.data.model.SoundDTO

enum class DurationMode { S15, S60, TEMPLATES }

enum class AspectRatioMode { RATIO_9_16, RATIO_1_1, RATIO_4_5, RATIO_16_9 }

data class TrimSettings(
    val startMs: Long = 0L,
    val endMs: Long = 15_000L
)

data class CropSettings(
    val aspectRatio: AspectRatioMode = AspectRatioMode.RATIO_9_16
)

data class FilterSettings(
    val name: String = "Original",
    val intensity: Float = 1.0f
)

data class SoundSelection(
    val sound: SoundDTO,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 0L
)

sealed class OverlayItem {
    abstract val id: String
    abstract val normalizedX: Float
    abstract val normalizedY: Float
    abstract val scale: Float
    abstract val rotation: Float

    data class TextOverlay(
        override val id: String,
        val text: String,
        val style: TextStyleType = TextStyleType.MODERN,
        val color: Long = 0xFF00FF00,
        override val normalizedX: Float = 0.5f,
        override val normalizedY: Float = 0.5f,
        override val scale: Float = 1f,
        override val rotation: Float = 0f
    ) : OverlayItem()

    data class StickerOverlay(
        override val id: String,
        val stickerType: StickerType,
        override val normalizedX: Float = 0.5f,
        override val normalizedY: Float = 0.5f,
        override val scale: Float = 1f,
        override val rotation: Float = 0f
    ) : OverlayItem()
}

enum class TextStyleType { MODERN, CLASSIC, BOLD, SERIF }

enum class StickerType {
    LOCATION, MENTION, HASHTAG, POLL, ASK_ME_ANYTHING, LINK,
    SHOPPING_CART, STAR, HEART, FIRE, NEW_RELEASES, SELL, CELEBRATION, STOREFRONT
}

data class TaggedStoreInfo(
    val storeId: String,
    val storeName: String,
    val selectedServiceIds: List<String> = emptyList()
)

data class CreatePostDraft(
    val mediaUri: Uri? = null,
    val postType: PostType = PostType.VIDEO,
    val durationMode: DurationMode = DurationMode.S15,
    val recordingSpeed: Float = 1f,
    val flashEnabled: Boolean = false,
    val useFrontCamera: Boolean = false,
    val trim: TrimSettings = TrimSettings(),
    val crop: CropSettings = CropSettings(),
    val filter: FilterSettings = FilterSettings(),
    val soundSelection: SoundSelection? = null,
    val overlays: List<OverlayItem> = emptyList(),
    val taggedStore: TaggedStoreInfo? = null,
    val caption: String = ""
)
