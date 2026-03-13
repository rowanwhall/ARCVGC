package com.arcvgc.app.ui.mapper

import com.arcvgc.app.domain.model.MatchDetail
import com.arcvgc.app.domain.model.MatchPreview
import com.arcvgc.app.domain.model.PlayerDetail
import com.arcvgc.app.domain.model.PlayerPreview
import com.arcvgc.app.domain.model.PokemonDetail
import com.arcvgc.app.domain.model.PokemonPreview
import com.arcvgc.app.ui.model.BattleCardUiModel
import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.ui.model.PlayerUiModel
import com.arcvgc.app.ui.model.PokemonSlotUiModel

object BattleCardUiMapper {

    fun map(matchDetail: MatchDetail): BattleCardUiModel {
        val players = matchDetail.players
        return BattleCardUiModel(
            id = matchDetail.id,
            player1 = players.getOrNull(0)?.toUiModel() ?: emptyPlayer(),
            player2 = players.getOrNull(1)?.toUiModel() ?: emptyPlayer(),
            formatName = matchDetail.format.formattedName ?: matchDetail.format.name,
            rating = matchDetail.rating?.toString() ?: "Unrated",
            formattedTime = formatUploadTime(matchDetail.uploadTime)
        )
    }

    fun map(matchPreview: MatchPreview): BattleCardUiModel {
        val players = matchPreview.players
        return BattleCardUiModel(
            id = matchPreview.id,
            player1 = players.getOrNull(0)?.toUiModel() ?: emptyPlayer(),
            player2 = players.getOrNull(1)?.toUiModel() ?: emptyPlayer(),
            formatName = matchPreview.format.formattedName ?: matchPreview.format.name,
            rating = matchPreview.rating?.toString() ?: "Unrated",
            formattedTime = formatUploadTime(matchPreview.uploadTime)
        )
    }

    fun mapList(matchPreviews: List<MatchPreview>): List<BattleCardUiModel> {
        return matchPreviews.map { map(it) }
    }

    private fun PlayerDetail.toUiModel(): PlayerUiModel {
        return PlayerUiModel(
            name = name,
            isWinner = isWinner,
            team = team.map { it.toSlotUiModel() }
        )
    }

    private fun PokemonDetail.toSlotUiModel(): PokemonSlotUiModel {
        return PokemonSlotUiModel(
            name = name,
            imageUrl = imageUrl,
            item = item?.let {
                ItemUiModel(
                    id = it.id,
                    name = it.displayName,
                    imageUrl = it.imageUrl
                )
            },
            teraType = teraType?.let { TeraTypeUiMapper.map(it) }
        )
    }

    private fun PlayerPreview.toUiModel(): PlayerUiModel {
        return PlayerUiModel(
            name = name,
            isWinner = isWinner,
            team = team.map { it.toUiModel() }
        )
    }

    private fun PokemonPreview.toUiModel(): PokemonSlotUiModel {
        return PokemonSlotUiModel(
            name = name,
            imageUrl = imageUrl,
            item = item?.let {
                ItemUiModel(
                    id = it.id,
                    name = it.displayName,
                    imageUrl = it.imageUrl
                )
            },
            teraType = teraType?.let { TeraTypeUiMapper.map(it) }
        )
    }

    private fun emptyPlayer(): PlayerUiModel {
        return PlayerUiModel(
            name = "Unknown",
            isWinner = false,
            team = emptyList()
        )
    }

}
