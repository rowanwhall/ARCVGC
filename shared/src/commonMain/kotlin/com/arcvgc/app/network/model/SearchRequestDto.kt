package com.arcvgc.app.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RatingDto(
    val min: Int? = null,
    val max: Int? = null,
    @SerialName("unrated_only") val unratedOnly: Boolean? = null
)

@Serializable
data class TimeRangeDto(
    val start: Long? = null,
    val end: Long? = null
)

@Serializable
data class SearchRequestDto(
    val limit: Int = 50,
    val page: Int = 1,
    @SerialName("format_id") val formatId: Int,
    val rating: RatingDto? = null,
    val pokemon: List<SearchPokemonDto>,
    @SerialName("order_by") val orderBy: String = "time",
    @SerialName("time_range") val timeRange: TimeRangeDto? = null,
    @SerialName("player_name") val playerName: String? = null
)

@Serializable
data class SearchPokemonDto(
    val id: Int,
    @SerialName("item_id") val itemId: Int? = null,
    @SerialName("tera_type_id") val teraTypeId: Int? = null
)
