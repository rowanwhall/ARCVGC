package com.arcvgc.app.network.model

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
    val players: List<PlayerDetailDto>,
    @SerialName("set_id") val setId: String? = null,
    @SerialName("position_in_set") val positionInSet: Int? = null,
    @SerialName("set_matches") val setMatches: List<SetMatchDto>? = null
)
