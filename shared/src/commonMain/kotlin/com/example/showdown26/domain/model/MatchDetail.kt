package com.example.showdown26.domain.model

data class MatchDetail(
    val id: Int,
    val showdownId: String,
    val uploadTime: String,
    val rating: Int?,
    val isPrivate: Boolean,
    val format: Format,
    val players: List<PlayerDetail>
) {
    val replayUrl: String
        get() = "https://replay.pokemonshowdown.com/$showdownId"
}
