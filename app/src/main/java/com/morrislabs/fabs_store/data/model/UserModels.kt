package com.morrislabs.fabs_store.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserLookupResponseDTO(
    val id: String,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = ""
)
