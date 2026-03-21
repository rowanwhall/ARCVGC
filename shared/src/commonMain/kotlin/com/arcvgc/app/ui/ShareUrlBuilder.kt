package com.arcvgc.app.ui

import com.arcvgc.app.domain.model.appendBattleParam
import com.arcvgc.app.domain.model.encodePercent
import com.arcvgc.app.domain.model.encodeSearchPath
import com.arcvgc.app.ui.model.ContentListMode
import com.arcvgc.app.ui.model.FavoriteContentType

private const val SHARE_BASE_URL = "https://arcvgc.com"

fun shareUrlForMode(mode: ContentListMode, battleId: Int?): String {
    val basePath = when (mode) {
        is ContentListMode.Home -> "/"
        is ContentListMode.Pokemon -> "/pokemon/${mode.pokemonId}"
        is ContentListMode.Player -> "/player/${encodePercent(mode.playerName)}"
        is ContentListMode.Favorites -> when (mode.contentType) {
            FavoriteContentType.Battles -> "/favorites/battles"
            FavoriteContentType.Pokemon -> "/favorites/pokemon"
            FavoriteContentType.Players -> "/favorites/players"
        }
        is ContentListMode.Search -> encodeSearchPath(mode.params)
        is ContentListMode.TopPokemon -> "/top-pokemon"
    }
    return "$SHARE_BASE_URL${appendBattleParam(basePath, battleId)}"
}
