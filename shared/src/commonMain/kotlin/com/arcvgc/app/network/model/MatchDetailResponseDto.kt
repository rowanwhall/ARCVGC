package com.arcvgc.app.network.model

import kotlinx.serialization.Serializable

@Serializable
data class MatchDetailResponseDto(
    val success: Boolean,
    val data: List<MatchDetailDto>? = null
)
