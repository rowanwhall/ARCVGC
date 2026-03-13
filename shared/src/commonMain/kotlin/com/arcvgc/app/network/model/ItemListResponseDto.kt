package com.arcvgc.app.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemListResponseDto(
    val success: Boolean,
    val data: List<ItemListItemDto>,
    val pagination: PaginationDto
)

@Serializable
data class ItemListItemDto(
    val id: Int,
    val name: String,
    @SerialName("image_url") val imageUrl: String? = null
)
