package com.example.showdown26.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MatchDetailDto(
    val id: Int,
    @SerialName("showdown_id") val showdownId: String,
    @SerialName("upload_time") val uploadTime: String,
    val rating: Int?,
    val private: Boolean,
    val format: FormatDto,
    val players: List<PlayerDetailDto>
)
