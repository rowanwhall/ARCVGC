package com.arcvgc.app.ui.contentlist

import com.arcvgc.app.domain.model.LookbackWindow
import com.arcvgc.app.domain.model.SearchParams
import com.arcvgc.app.ui.model.ContentListMode

/**
 * ViewModelStore keys for [ContentListPage]'s per-mode ContentListViewModels.
 *
 * The granular helpers exist so hosting pages (SearchDesktopPage,
 * UsageDesktopPage) can `peek()` the cached VM of the page they are about to
 * render — e.g. to seed their side-pane visibility from `savedBattleId` —
 * without duplicating the key format.
 */
internal fun searchContentListKey(params: SearchParams): String =
    "content_list_search_$params"

internal fun pokemonContentListKey(pokemonId: Int, formatId: Int?, lookback: LookbackWindow?): String =
    "content_list_pokemon_${pokemonId}_${formatId}_${lookback?.value}"

internal fun playerContentListKey(playerId: Int, formatId: Int?): String =
    "content_list_player_${playerId}_${formatId}"

internal fun contentListViewModelKey(mode: ContentListMode): String = when (mode) {
    is ContentListMode.Home -> "content_list_home"
    is ContentListMode.Favorites -> "content_list_favorites_${mode.contentType.name}"
    is ContentListMode.Search -> searchContentListKey(mode.params)
    is ContentListMode.Pokemon -> pokemonContentListKey(mode.pokemonId, mode.formatId, mode.lookback)
    is ContentListMode.Player -> playerContentListKey(mode.playerId, mode.formatId)
    is ContentListMode.TopPokemon -> "content_list_top_pokemon_${mode.formatId}"
}

/**
 * Derives the `formatId` to thread through Pokémon/Player drill-down
 * navigation when a user taps an item inside a ContentListPage.
 *
 * Search mode uses its own pinned format from the search params; every other
 * mode that supports a format selector uses whatever format the user has
 * active on the current page. Favorites have no format scope, so `null`.
 *
 * Extracted from ContentListPage so both the Compact and Expanded branches
 * — and the item-click, pokemon-grid-click, and player-click handlers inside
 * each — share one definition instead of repeating the same `when`.
 */
internal fun derivedFormatId(
    mode: ContentListMode,
    selectedFormatId: Int?
): Int? = when (mode) {
    is ContentListMode.Search -> mode.params.formatId
    is ContentListMode.Home,
    is ContentListMode.TopPokemon,
    is ContentListMode.Pokemon,
    is ContentListMode.Player -> selectedFormatId
    is ContentListMode.Favorites -> null
}

/**
 * Mirrors [derivedFormatId] for the lookback window. Only modes that expose a
 * lookback selector (TopPokemon, Pokemon) propagate their current selection;
 * every other mode returns `null` so the destination page falls back to its
 * own default.
 */
internal fun derivedLookback(
    mode: ContentListMode,
    selectedLookback: LookbackWindow
): LookbackWindow? = when (mode) {
    is ContentListMode.TopPokemon,
    is ContentListMode.Pokemon -> selectedLookback
    else -> null
}
