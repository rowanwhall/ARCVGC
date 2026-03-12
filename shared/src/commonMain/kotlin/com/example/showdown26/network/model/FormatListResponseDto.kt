package com.example.showdown26.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FormatListResponseDto(
    val success: Boolean,
    val data: List<FormatListItemDto>,
    val pagination: PaginationDto
)

@Serializable
data class FormatListItemDto(
    val id: Int,
    val name: String,
    @SerialName("formatted_name") val formattedName: String? = null
)
