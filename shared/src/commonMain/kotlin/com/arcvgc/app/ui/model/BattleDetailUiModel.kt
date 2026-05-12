package com.arcvgc.app.ui.model

data class BattleDetailUiModel(
    val id: Int,
    val player1: PlayerDetailUiModel,
    val player2: PlayerDetailUiModel,
    val formatId: Int,
    val formatName: String,
    val hasSeries: Boolean,
    val rating: Int?,
    val formattedTime: String,
    val replayUrl: String,
    val positionInSet: Int? = null,
    val setMatches: List<SetMatchUiModel> = emptyList()
) {
    /**
     * True when at least one pokemon on either team has `moves.isNotEmpty()`.
     * Drives layout sizing: closed-sheet cards collapse to a narrower width since
     * moves/ability/item rows don't render. Computed from the response (not from
     * the format's `isOpenTeamsheet` flag) so future partial-info responses still
     * trigger the wider open layout when any moves exist.
     */
    val hasMoveData: Boolean
        get() = player1.team.any { it.moves.isNotEmpty() } ||
            player2.team.any { it.moves.isNotEmpty() }
}

data class SetMatchUiModel(
    val id: Int,
    val positionInSet: Int? = null,
    val replayUrl: String
)
