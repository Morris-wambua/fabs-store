package com.morrislabs.fabs_store.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class Badge {
    GOLD, SILVER, TOP_RATED, PLATINUM
}

@Serializable
enum class MainCategory {
    BARBERSHOP_SERVICES,
    BEAUTY_PRODUCTS,
    BODY_SERVICES,
    HAIR_REMOVAL,
    HAIR_SERVICES,
    LASH_AND_BROW,
    MAKEUP_AND_COSMETICS,
    NAIL_SERVICES,
    SKINCARE_SERVICES,
    WELLNESS_AND_SPA
}

@Serializable
enum class SubCategory {
    // Hair
    BRAIDING,
    BLOWOUTS,
    COLORING,
    EXTENSIONS,
    HAIRCUTS,
    KERATIN_TREATMENTS,
    LOCS_AND_DREADLOCKS,
    PERMS,
    RELAXERS,
    SCALP_TREATMENTS,
    STYLING,
    TREATMENTS,
    WEAVES,

    // Nails
    ACRYLIC_NAILS,
    DIPPING_POWDER,
    GEL_NAILS,
    MANICURES,
    NAIL_ART,
    NAIL_CARE_PRODUCTS,
    PARAFFIN_TREATMENTS,
    PEDICURES,

    // Skincare
    ACNE_TREATMENTS,
    ANTI_AGING_TREATMENTS,
    CHEMICAL_PEELS,
    DERMAPLANING,
    FACIALS,
    HYDRAFACIALS,
    LED_THERAPY,
    MICRODERMABRASION,
    MICRONEEDLING,
    OXYGEN_FACIALS,
    SKIN_TIGHTENING,
    SKINCARE_PRODUCTS,

    // Body
    BODY_SCRUBS,
    BODY_WRAPS,
    HYDROTHERAPY,
    JACUZZI_TREATMENTS,
    MASSAGE_THERAPY,
    HOT_STONE_THERAPY,
    SAUNA_SESSIONS,
    STEAM_TREATMENTS,

    // Makeup
    AIRBRUSH_MAKEUP,
    BRIDAL_MAKEUP,
    EVERYDAY_MAKEUP,
    MAKEUP_CONSULTATION,
    MAKEUP_LESSONS,
    MAKEUP_PRODUCTS,
    PERMANENT_MAKEUP,
    SPECIAL_OCCASION_MAKEUP,
    THEATRICAL_MAKEUP,

    // Hair Removal
    DEPILATORY_CREAMS,
    ELECTROLYSIS,
    IPL_TREATMENT,
    LASER_HAIR_REMOVAL,
    SUGARING,
    THREADING,
    WAXING,

    // Lash & Brow
    BROW_HENNA,
    BROW_LAMINATION,
    BROW_SHAPING,
    BROW_TINTING,
    EYELASH_PERMING,
    LASH_EXTENSIONS,
    LASH_LIFT_AND_TINT,
    MICROBLADING,

    // Barbershop
    BEARD_COLORING,
    BEARD_TRIMMING,
    FACIAL_HAIR_DESIGN,
    HEAD_SHAVES,
    HOT_TOWEL_SHAVES,
    MENS_FACIALS,
    MENS_HAIRCUTS,

    // Wellness
    AROMATHERAPY,
    AYURVEDIC_TREATMENTS,
    FLOTATION_THERAPY,
    MEDITATION_SESSIONS,
    MUD_BATHS,
    REFLEXOLOGY,
    SALT_THERAPY,
    SOUND_THERAPY,

    // Beauty Products
    BATH_AND_BODY_PRODUCTS,
    FRAGRANCES,
    HAIR_CARE_PRODUCTS,
    ORGANIC_AND_NATURAL_PRODUCTS,
    TOOLS_AND_ACCESSORIES;

    fun toMainCategory(): MainCategory = when (this) {
        BRAIDING, BLOWOUTS, COLORING, EXTENSIONS, HAIRCUTS, KERATIN_TREATMENTS,
        LOCS_AND_DREADLOCKS, PERMS, RELAXERS, SCALP_TREATMENTS, STYLING, TREATMENTS, WEAVES
            -> MainCategory.HAIR_SERVICES
        ACRYLIC_NAILS, DIPPING_POWDER, GEL_NAILS, MANICURES, NAIL_ART,
        NAIL_CARE_PRODUCTS, PARAFFIN_TREATMENTS, PEDICURES
            -> MainCategory.NAIL_SERVICES
        ACNE_TREATMENTS, ANTI_AGING_TREATMENTS, CHEMICAL_PEELS, DERMAPLANING, FACIALS,
        HYDRAFACIALS, LED_THERAPY, MICRODERMABRASION, MICRONEEDLING, OXYGEN_FACIALS,
        SKIN_TIGHTENING, SKINCARE_PRODUCTS
            -> MainCategory.SKINCARE_SERVICES
        BODY_SCRUBS, BODY_WRAPS, HYDROTHERAPY, JACUZZI_TREATMENTS, MASSAGE_THERAPY,
        HOT_STONE_THERAPY, SAUNA_SESSIONS, STEAM_TREATMENTS
            -> MainCategory.BODY_SERVICES
        AIRBRUSH_MAKEUP, BRIDAL_MAKEUP, EVERYDAY_MAKEUP, MAKEUP_CONSULTATION,
        MAKEUP_LESSONS, MAKEUP_PRODUCTS, PERMANENT_MAKEUP, SPECIAL_OCCASION_MAKEUP,
        THEATRICAL_MAKEUP
            -> MainCategory.MAKEUP_AND_COSMETICS
        DEPILATORY_CREAMS, ELECTROLYSIS, IPL_TREATMENT, LASER_HAIR_REMOVAL,
        SUGARING, THREADING, WAXING
            -> MainCategory.HAIR_REMOVAL
        BROW_HENNA, BROW_LAMINATION, BROW_SHAPING, BROW_TINTING, EYELASH_PERMING,
        LASH_EXTENSIONS, LASH_LIFT_AND_TINT, MICROBLADING
            -> MainCategory.LASH_AND_BROW
        BEARD_COLORING, BEARD_TRIMMING, FACIAL_HAIR_DESIGN, HEAD_SHAVES,
        HOT_TOWEL_SHAVES, MENS_FACIALS, MENS_HAIRCUTS
            -> MainCategory.BARBERSHOP_SERVICES
        AROMATHERAPY, AYURVEDIC_TREATMENTS, FLOTATION_THERAPY, MEDITATION_SESSIONS,
        MUD_BATHS, REFLEXOLOGY, SALT_THERAPY, SOUND_THERAPY
            -> MainCategory.WELLNESS_AND_SPA
        BATH_AND_BODY_PRODUCTS, FRAGRANCES, HAIR_CARE_PRODUCTS,
        ORGANIC_AND_NATURAL_PRODUCTS, TOOLS_AND_ACCESSORIES
            -> MainCategory.BEAUTY_PRODUCTS
    }
}

@Serializable
data class LocationDTO(
    val id: String,
    val name: String,
    val description: String? = null,
    val latitude: Double,
    val longitude: Double
)

data class LocationInput(
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class TypeOfServiceDTO(
    val id: String,
    val name: String,
    val mainCategory: MainCategory,
    val subCategory: SubCategory,
    val price: Int,
    val expertAvailable: Boolean,
    val ratings: Double,
    val duration: Int? = null,
    val description: String? = null,
    val imageUrl: String? = null
)

@Serializable
data class CreateServicePayload(
    val name: String,
    val mainCategory: MainCategory,
    val subCategory: SubCategory,
    val price: Int,
    val expertAvailable: Boolean = false,
    val ratings: Double = 5.0,
    val duration: Int = 60,
    val description: String? = null,
    val imageUrl: String? = null
)

@Serializable
data class StoreDTO(
    val id: String? = null,
    val name: String,
    val username: String,
    val noOfExperts: Int = 0,
    val ratings: Double = 5.0,
    val isVerified: Boolean = false,
    val badge: Badge = Badge.SILVER,
    val discount: Double = 0.0
)

@Serializable
data class CreateStorePayload(
    val name: String,
    val username: String,
    val noOfExperts: Int = 0,
    val ratings: Double = 5.0,
    val badge: Badge = Badge.SILVER,
    val discount: Double = 0.0,
    val location: LocationDTO,
    val servicesOffered: List<String> = emptyList(),
    val phone: String? = null,
    val about: String? = null,
    val email: String? = null,
    val logoUrl: String? = null,
    val logoS3Key: String? = null,
    val coverUrl: String? = null,
    val coverS3Key: String? = null,
    val businessHours: List<BusinessHourDTO>? = null
)

@Serializable
data class FetchStoreResponse(
    val id: String? = null,
    val name: String,
    val username: String,
    val noOfExperts: Int = 0,
    val ratings: Double = 5.0,
    val isVerified: Boolean = false,
    val noOfTags: Int = 0,
    val badge: Badge = Badge.SILVER,
    val discount: Double = 0.0,
    val phone: String? = null,
    val about: String? = null,
    val email: String? = null,
    val logoUrl: String? = null,
    val coverUrl: String? = null,
    val businessHours: List<BusinessHourDTO>? = null,
    val locationDTO: LocationDTO? = null,
    val servicesOffered: List<TypeOfServiceDTO>? = null
)

@Serializable
data class MainCategoryDTO(
    val name: MainCategory,
    val label: String = name.name
)

@Serializable
data class ServicesByCategoryDTO(
    val mainCategory: MainCategory,
    val services: List<TypeOfServiceDTO>
)

@Serializable
data class UpdateStorePayload(
    val name: String? = null,
    val description: String? = null,
    val username: String? = null,
    val noOfExperts: Int? = null,
    val ratings: Double? = null,
    val badge: Badge? = null,
    val discount: Double? = null,
    val phone: String? = null,
    val about: String? = null,
    val email: String? = null,
    val logoUrl: String? = null,
    val logoS3Key: String? = null,
    val coverUrl: String? = null,
    val coverS3Key: String? = null,
    val businessHours: List<BusinessHourDTO>? = null,
    val location: LocationDTO? = null,
    val servicesOffered: List<String> = emptyList()
)

@Serializable
data class BusinessHourDTO(
    val dayName: String,
    val dayIndex: Int,
    @kotlinx.serialization.SerialName("open")
    val isOpen: Boolean,
    val openTime: String? = null,
    val closeTime: String? = null
)

@Serializable
data class UploadMediaResponse(
    val fileName: String,
    val url: String,
    val expiryIn: String? = null
)

@Serializable
data class UploadIntentRequest(
    val fileName: String,
    val sizeBytes: Long,
    val contentType: String,
    val userId: String
)

@Serializable
data class UploadIntentResponse(
    val uploadId: String,
    val objectKey: String,
    val uploadUrl: String,
    val expiresAt: String
)

@Serializable
data class UploadCompleteRequest(
    val uploadId: String,
    val objectKey: String,
    val fileName: String,
    val contentType: String,
    val userId: String
)

@Serializable
enum class PostType {
    IMAGE, VIDEO
}

@Serializable
enum class PostTypeLevel {
    PUBLIC, PRIVATE, FOLLOWERS_ONLY
}

@Serializable
data class MediaS3Data(
    val mediaUrl: String,
    val filename: String,
    val lastRefreshed: String? = null
)

@Serializable
data class PostPayload(
    val caption: String? = null,
    val type: PostType,
    val mediaS3Data: MediaS3Data,
    val autoPlay: Boolean = false,
    val postTypeLevel: PostTypeLevel? = null
)

@Serializable
data class CommentDTO(
    val id: String? = null,
    val userId: String? = null,
    val userFirstName: String? = null,
    val userLastName: String? = null,
    val content: String,
    val dateCreated: String? = null,
    val lastEdited: String? = null,
    val parentCommentId: String? = null,
    val replies: List<CommentDTO> = emptyList(),
    val edited: Boolean = false,
    val totalReplies: Int = 0,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val hasMoreReplies: Boolean = false,
    val likeCount: Int = 0,
    val likedByCurrentUser: Boolean = false
)

@Serializable
data class PostDTO(
    val id: String? = null,
    val storeId: String? = null,
    val storeName: String? = null,
    val caption: String? = null,
    val type: PostType = PostType.IMAGE,
    val mediaUrl: String? = null,
    val comments: List<CommentDTO> = emptyList(),
    val likeCount: Int = 0,
    val shareCount: Int = 0,
    val saveCount: Int = 0,
    val viewCount: Int = 0,
    val dateCreated: String? = null,
    val autoPlay: Boolean = false,
    val likedByCurrentUser: Boolean = false,
    val savedByCurrentUser: Boolean = false,
    val totalComments: Int = 0,
    @kotlinx.serialization.SerialName("currentPage")
    val commentsCurrentPage: Int = 0,
    @kotlinx.serialization.SerialName("totalPages")
    val commentsTotalPages: Int = 0,
    val hasMoreComments: Boolean = false,
    val userId: String? = null,
    val userFirstName: String? = null,
    val userLastName: String? = null,
    val displayName: String? = null,
    val isUserPost: Boolean = false
)

@Serializable
data class PagedPostResponse(
    val content: List<PostDTO> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val last: Boolean = true
)

@Serializable
data class PagedCommentResponse(
    val content: List<CommentDTO> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val last: Boolean = true
)

@Serializable
data class HashtagSuggestionDTO(
    val hashtag: String,
    val usageCount: Long = 0
)
