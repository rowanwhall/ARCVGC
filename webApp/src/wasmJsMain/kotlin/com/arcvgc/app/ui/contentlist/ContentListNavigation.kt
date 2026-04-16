package com.arcvgc.app.ui.contentlist

import com.arcvgc.app.ui.model.ContentListMode

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
