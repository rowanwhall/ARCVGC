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
data class PaginationDto(
    val page: Int,
    @SerialName("items_per_page") val itemsPerPage: Int,
    @SerialName("total_items") val totalItems: Int,
    @SerialName("total_pages") val totalPages: Int
)
