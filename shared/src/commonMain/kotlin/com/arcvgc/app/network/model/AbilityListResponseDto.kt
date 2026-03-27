package com.arcvgc.app.network.model

import kotlinx.serialization.Serializable

@Serializable
data class AbilityListResponseDto(
    val success: Boolean,
    val data: List<AbilityListItemDto>,
    val pagination: PaginationDto
)

@Serializable
data class AbilityListItemDto(
    val id: Int,
    val name: String
)
