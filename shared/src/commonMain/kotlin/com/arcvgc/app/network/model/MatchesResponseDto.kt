package com.arcvgc.app.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MatchesResponseDto(
    val success: Boolean,
    val data: List<MatchPreviewDto>,
    val pagination: PaginationDto
)

@Serializable
data class BestMatchesResponseDto(
    val success: Boolean,
    val data: List<MatchPreviewDto>
)

@Serializable
data class PaginationDto(
    val page: Int,
    @SerialName("items_per_page") val itemsPerPage: Int,
    @SerialName("has_next") val hasNext: Boolean
)
