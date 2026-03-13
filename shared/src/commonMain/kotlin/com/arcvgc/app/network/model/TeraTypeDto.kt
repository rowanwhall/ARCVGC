package com.arcvgc.app.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeraTypeDto(
    val id: Int,
    val name: String,
    @SerialName("image_url") val imageUrl: String? = null
)
