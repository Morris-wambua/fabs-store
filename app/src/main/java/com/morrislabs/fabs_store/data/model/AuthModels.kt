package com.morrislabs.fabs_store.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = UserRoleSerializer::class)
enum class UserRole {
    ADMIN, CUSTOMER, STORE_OWNER, EXPERT
}

object UserRoleSerializer : KSerializer<UserRole> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UserRole", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UserRole) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): UserRole {
        return UserRole.valueOf(decoder.decodeString())
    }
}

@Serializable
data class CredentialsDTO(
    val login: String,
    val password: String
)

@Serializable
data class LoginDTO(
    val id: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val login: String? = null,
    val email: String? = null,
    val token: String? = null,
    val refreshToken: String? = null,
    val role: UserRole? = null
)

@Serializable
data class RefreshTokenDTO(
    val accessToken: String? = null,
    val refreshToken: String? = null
)

@Serializable
data class RegisterDTO(
    val firstName: String,
    val lastName: String,
    val login: String,
    val email: String,
    val password: String,
    @SerialName("role")
    @Required
    val role: UserRole = UserRole.STORE_OWNER
)

@Serializable
data class ErrorResponse(
    val message: String,
    val status: Int? = null,
    val code: String? = null
)
