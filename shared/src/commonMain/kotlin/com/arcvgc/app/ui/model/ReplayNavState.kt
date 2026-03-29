package com.arcvgc.app.ui.model

data class ReplayGame(
    val positionInSet: Int?,
    val replayUrl: String
)

data class ReplayNavState(
    val games: List<ReplayGame>,
    val initialIndex: Int
)

fun BattleDetailUiModel.toReplayNavState(tappedUrl: String): ReplayNavState {
    val allGames = buildList {
        add(ReplayGame(positionInSet, replayUrl))
        setMatches.forEach { add(ReplayGame(it.positionInSet, it.replayUrl)) }
    }.sortedBy { it.positionInSet ?: Int.MAX_VALUE }
    val tappedIndex = allGames.indexOfFirst { it.replayUrl == tappedUrl }.coerceAtLeast(0)
    return ReplayNavState(games = allGames, initialIndex = tappedIndex)
}
