package com.arcvgc.app.ui.mapper

import com.arcvgc.app.domain.model.MatchDetail
import com.arcvgc.app.domain.model.PlayerDetail
import com.arcvgc.app.domain.model.PokemonDetail
import com.arcvgc.app.ui.model.BattleDetailUiModel
import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.ui.model.PlayerDetailUiModel
import com.arcvgc.app.ui.model.PokemonDetailUiModel
import com.arcvgc.app.ui.model.TeraTypeUiModel
import com.arcvgc.app.ui.model.TypeUiModel

object BattleDetailUiMapper {

    private const val DEFAULT_RATING = 1000

    fun map(matchDetail: MatchDetail): BattleDetailUiModel {
        val players = matchDetail.players
        val rating = matchDetail.rating?.takeIf { it >= DEFAULT_RATING } ?: DEFAULT_RATING

        return BattleDetailUiModel(
            id = matchDetail.id,
            player1 = players.getOrNull(0)?.toUiModel() ?: emptyPlayer(),
            player2 = players.getOrNull(1)?.toUiModel() ?: emptyPlayer(),
            formatName = matchDetail.format.formattedName ?: matchDetail.format.name,
            rating = rating,
            formattedTime = formatUploadTime(matchDetail.uploadTime),
            replayUrl = matchDetail.replayUrl
        )
    }

    private fun PlayerDetail.toUiModel(): PlayerDetailUiModel {
        return PlayerDetailUiModel(
            id = id,
            name = name,
            isWinner = isWinner,
            team = team.map { it.toUiModel() }
        )
    }

    private fun PokemonDetail.toUiModel(): PokemonDetailUiModel {
        return PokemonDetailUiModel(
            id = id,
            name = name,
            imageUrl = imageUrl,
            item = item?.let {
                ItemUiModel(
                    id = it.id,
                    name = it.displayName,
                    imageUrl = it.imageUrl
                )
            },
            abilityName = ability.displayName,
            moves = moves.map { it.displayName },
            types = types.map {
                TypeUiModel(
                    name = it.name,
                    imageUrl = it.imageUrl
                )
            },
            teraType = teraType?.let {
                TeraTypeUiModel(
                    id = it.id,
                    name = it.name,
                    imageUrl = it.imageUrl
                )
            }
        )
    }

    private fun emptyPlayer(): PlayerDetailUiModel {
        return PlayerDetailUiModel(
            id = 0,
            name = "Unknown",
            isWinner = null,
            team = emptyList()
        )
    }

}
