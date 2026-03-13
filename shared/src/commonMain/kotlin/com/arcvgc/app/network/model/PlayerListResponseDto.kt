package com.arcvgc.app.network.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerListResponseDto(
    val success: Boolean,
    val data: List<PlayerListItemDto>,
    val pagination: PaginationDto
)

@Serializable
data class PlayerListItemDto(val id: Int, val name: String)
