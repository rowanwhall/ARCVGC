package com.example.showdown26.network.model

import kotlinx.serialization.Serializable

@Serializable
data class MatchDetailResponseDto(
    val success: Boolean,
    val data: List<MatchDetailDto>
)
