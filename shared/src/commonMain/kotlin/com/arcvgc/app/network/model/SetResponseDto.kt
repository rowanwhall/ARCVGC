package com.arcvgc.app.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SetListResponseDto(
    val success: Boolean,
    val data: List<SetDto>,
    val pagination: PaginationDto
)

@Serializable
data class SetDto(
    val id: Int,
    @SerialName("max_rating") val maxRating: Int?,
    @SerialName("match_count") val matchCount: Int,
    val format: FormatDto,
    val matches: List<SetMatchModelDto>,
    val players: List<SetPlayerDto>
)

@Serializable
data class SetMatchModelDto(
    @SerialName("position_in_set") val positionInSet: Int,
    val id: Int,
    @SerialName("showdown_id") val showdownId: String,
    @SerialName("upload_time") val uploadTime: String,
    val rating: Int?,
    val private: Boolean,
    @SerialName("winner_id") val winnerId: Int?
)

@Serializable
data class SetPlayerDto(
    val id: Int,
    val name: String,
    @SerialName("win_count") val winCount: Int
)

@Serializable
data class SetDetailResponseDto(
    val success: Boolean,
    val data: List<SetDetailDto>
)

@Serializable
data class SetDetailDto(
    val id: Int,
    @SerialName("max_rating") val maxRating: Int?,
    @SerialName("match_count") val matchCount: Int,
    val format: FormatDto,
    val matches: List<SetMatchModelDto>,
    val players: List<SetPlayerDetailDto>
)

@Serializable
data class SetPlayerDetailDto(
    val id: Int,
    val name: String,
    @SerialName("win_count") val winCount: Int,
    val team: List<PokemonDetailDto>
)
