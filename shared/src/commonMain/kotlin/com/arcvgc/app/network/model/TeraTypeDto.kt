package com.arcvgc.app.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeraTypeDto(
    val id: Int? = null,
    val name: String? = null,
    @SerialName("image_url") val imageUrl: String? = null
)
