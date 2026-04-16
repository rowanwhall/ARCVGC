package com.arcvgc.app.ui.contentlist

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arcvgc.app.domain.model.SearchParams
import com.arcvgc.app.ui.model.ContentListItem
import com.arcvgc.app.ui.model.FormatUiModel

/**
 * Parameter groups for [ContentListContent]. Extracted into data classes so the
 * composable signature stays short and intent is legible at the call sites in
 * `ContentListPage` (Compact vs Expanded).
 *
 * Keep these data classes small and purpose-specific — one grouping per concern.
 * If a new field doesn't fit any of these three, prefer adding it as a top-level
 * parameter on [ContentListContent] rather than inventing a fourth grouping.
 */

/**
 * Every callback `ContentListContent` forwards from `ContentListPage`. A single
 * bag of lambdas rather than positional args, so the Compact and Expanded paths
 * don't each have to list 10 identical callback bindings.
 */
internal data class ContentListCallbacks(
    val onRetry: () -> Unit,
    val onPaginate: () -> Unit,
    val onItemClick: (ContentListItem) -> Unit,
    val onHighlightBattleClick: (Int) -> Unit = {},
    val onPokemonGridClick: (ContentListItem.PokemonGridItem) -> Unit = {},
    val onSearchParamsChanged: ((SearchParams) -> Unit)? = null,
    val onToggleSortOrder: (() -> Unit)? = null,
    val onFormatSelected: ((Int) -> Unit)? = null,
    val onSearchQueryChanged: ((String) -> Unit)? = null,
    val onSeeMore: (() -> Unit)? = null,
)

/**
 * Format/sort/search state that drives header chips, format dropdown, sort toggle,
 * and the Pokémon search field. Not *all* modes use every field — Search uses
 * `searchParams`, Home/TopPokemon use `formats` + `selectedFormatId`, TopPokemon
 * uses `searchQuery`, etc. Everything defaults to "disabled" so a mode that
 * doesn't need a field can just leave it out.
 */
internal data class ContentListFormatState(
    val searchParams: SearchParams? = null,
    val sortOrder: String? = null,
    val formats: List<FormatUiModel> = emptyList(),
    val selectedFormatId: Int = 0,
    val searchQuery: String = "",
)

/**
 * Desktop-web responsive grid sizing. Defaults encode the Compact behavior:
 * [battleCardCellWidth] holds the designed default so single-column layouts
 * render battle cards at the unscaled size, and [expandedTopPokemonMaxWidth]
 * is 0 because the content-aligned header/wide Pokemon grid paths only run
 * under the Expanded branch.
 */
internal data class ContentListGridConfig(
    val battleCardCellWidth: Dp = BATTLE_CARD_DEFAULT_WIDTH,
    val expandedTopPokemonMaxWidth: Dp = 0.dp,
    val topPokemonTargetWidth: Dp = 0.dp,
    val topPokemonTileCount: Int = TOP_POKEMON_MIN_TILES,
    val topPokemonTileWidth: Dp = TOP_POKEMON_TILE_MIN_WIDTH,
)
