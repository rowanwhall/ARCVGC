package com.arcvgc.app.domain.model

data class MatchDetail(
    val id: Int,
    val showdownId: String,
    val uploadTime: String,
    val rating: Int?,
    val isPrivate: Boolean,
    val format: Format,
    val players: List<PlayerDetail>,
    val setId: String? = null,
    val positionInSet: Int? = null,
    val setMatches: List<SetMatch> = emptyList()
) {
    val replayUrl: String
        get() = "https://replay.pokemonshowdown.com/$showdownId"
}
