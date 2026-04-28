package com.arcvgc.app.network.model

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
    @SerialName("formatted_name") val formattedName: String? = null,
    @SerialName("is_historic") val isHistoric: Boolean = false,
    @SerialName("is_open_teamsheet") val isOpenTeamsheet: Boolean = false,
    @SerialName("is_official") val isOfficial: Boolean = false,
    @SerialName("has_series") val hasSeries: Boolean = false
)
