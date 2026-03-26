package com.arcvgc.app.domain.model

data class MatchSet(
    val id: Int,
    val maxRating: Int?,
    val matchCount: Int,
    val format: Format,
    val matches: List<SetMatchInfo>,
    val players: List<SetPlayer>
)

data class SetMatchInfo(
    val positionInSet: Int,
    val id: Int,
    val showdownId: String,
    val uploadTime: String,
    val rating: Int?,
    val isPrivate: Boolean,
    val winnerId: Int?
)

data class SetPlayer(
    val id: Int,
    val name: String,
    val winCount: Int
)

data class SetDetail(
    val id: Int,
    val maxRating: Int?,
    val matchCount: Int,
    val format: Format,
    val matches: List<SetMatchInfo>,
    val players: List<SetPlayerDetail>
)

data class SetPlayerDetail(
    val id: Int,
    val name: String,
    val winCount: Int,
    val team: List<PokemonDetail>
)
