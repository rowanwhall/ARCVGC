package com.example.showdown26.network.model

import kotlinx.serialization.Serializable

@Serializable
data class TeraTypeListResponseDto(
    val success: Boolean,
    val data: List<TeraTypeDto>,
    val pagination: PaginationDto
)
