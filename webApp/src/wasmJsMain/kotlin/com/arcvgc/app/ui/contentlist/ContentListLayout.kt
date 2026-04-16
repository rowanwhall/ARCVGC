package com.arcvgc.app.ui.contentlist

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.arcvgc.app.ui.tokens.AppTokens.ContentListItemSpacing

// Layout math and design constants for the desktop-web ContentListPage.
//
// This file exists as a single home for the dp constants and pure functions that
// govern the responsive desktop-web layout — battle-card sizing, detail-pane
// reservation, Top Pokémon tile packing, and SectionGroup column derivation.
// Keeping them together (rather than inlined at the bottom of ContentListPage.kt)
// makes the layout contract easier to reason about and tune without scrolling
// past the presentational code that consumes it.

// region: Battle card sizing (desktop web)
//
// `DEFAULT` is the natural designed width and the fallback used for compact mobile
// layouts and any viewport too narrow for multi-column. On wider viewports the cell
// width is derived from the window width at discrete column-count breakpoints, so
// cards grow within `[MIN, MAX]` to fill the available space instead of leaving a
// fixed 620dp column with dead gutter to the right.
//
// The derivation keys on **total window width**, not the grid-box width that
// shrinks when the detail pane opens. That way the cell width is stable across
// pane-open/close transitions — only the column count changes, so the individual
// card size doesn't snap mid-animation.
internal val BATTLE_CARD_MIN_WIDTH = 560.dp
internal val BATTLE_CARD_DEFAULT_WIDTH = 620.dp
// Upper bound on the dynamically-grown cell width. Named "GROWN_MAX" rather
// than "MAX" because this isn't a universal ceiling — narrow viewports still
// produce cards at [BATTLE_CARD_DEFAULT_WIDTH] (or smaller on sub-2-col
// viewports via the fallback path). This constant only bounds how far cards
// grow past default when a wide viewport has surplus space to distribute.
internal val BATTLE_CARD_GROWN_MAX_WIDTH = 780.dp
internal val BATTLE_GRID_SPACING = 12.dp
// LazyVerticalGrid's horizontal contentPadding (16.dp × 2). Subtracted when
// converting a window width into the space available for battle-card cells.
internal val BATTLE_GRID_HORIZONTAL_PADDING = 32.dp

// Minimum detail-pane width at which `BattleDetailContent`'s `PlayerTeamSection`
// fits 3 Pokemon cards per row in each team card. Derivation:
//   - Section applies `fillMaxWidth().padding(horizontal = 16.dp)`, so the
//     BoxWithConstraints inside sees `maxWidth = paneWidth − 32`.
//   - Inside, `availableForCards = maxWidth − innerPadding × 2 = maxWidth − 32`.
//   - 3 cols requires `(availableForCards + 12) / 292 ≥ 3`, i.e.
//     `availableForCards ≥ 864` → `paneWidth ≥ 928`.
// Battle-card growth is capped so opening the pane leaves at least this much
// room beside the grid — otherwise wider battle cards would squeeze the pane
// below the 3-pokemon-per-row threshold.
internal val DETAIL_PANEL_PREFERRED_MIN_WIDTH = 928.dp

internal const val DETAIL_PANE_ANIM_DURATION_MS = 300

/**
 * Picks the desired battle-card column count for a given window width. Uses
 * [BATTLE_CARD_DEFAULT_WIDTH] as the per-column minimum so cards never drop
 * below the designed width just to squeeze in another column.
 */
internal fun desiredBattleColumns(windowWidth: Dp): Int {
    // N columns fit when `N * stepWidth - spacing + padding ≤ windowWidth`,
    // i.e. `N ≤ (windowWidth - padding + spacing) / stepWidth`. The `+ spacing`
    // is the inverse of "N columns need only N−1 spacings between them", so it
    // cancels the trailing spacing that `stepWidth * N` over-counts.
    val stepWidth = BATTLE_CARD_DEFAULT_WIDTH + BATTLE_GRID_SPACING
    val available = (windowWidth - BATTLE_GRID_HORIZONTAL_PADDING + BATTLE_GRID_SPACING)
        .coerceAtLeast(0.dp)
    val raw = (available.value / stepWidth.value).toInt()
    return raw.coerceAtLeast(1)
}

/**
 * Derives the battle-card cell width from the full window width. Passed to
 * `GridCells.FixedSize` so the grid fits [desiredBattleColumns] columns exactly,
 * with each column sized to fill the available width within [[BATTLE_CARD_MIN_WIDTH],
 * [BATTLE_CARD_GROWN_MAX_WIDTH]].
 *
 * Narrow viewports (below the 2-column breakpoint) return the default width so
 * compact mobile layouts behave exactly like before the dynamic sizing was added.
 *
 * When the card would otherwise grow past [BATTLE_CARD_DEFAULT_WIDTH], it is
 * further capped so the detail pane retains at least [DETAIL_PANEL_PREFERRED_MIN_WIDTH]
 * room when open — keeping 3-pokemon-per-row pokemon in each team card. The
 * cap floor is pinned at the default width so this only shrinks cards relative
 * to their unconstrained growth; cards never drop *below* the default just to
 * buy pane width, which avoids a visible card-width jolt when resizing through
 * the threshold where the cap first becomes relevant.
 */
internal fun computeBattleCardCellWidth(windowWidth: Dp): Dp {
    val twoColMinWidth = BATTLE_CARD_DEFAULT_WIDTH * 2 +
        BATTLE_GRID_SPACING +
        BATTLE_GRID_HORIZONTAL_PADDING
    if (windowWidth < twoColMinWidth) return BATTLE_CARD_DEFAULT_WIDTH
    val cols = desiredBattleColumns(windowWidth)
    val interCardSpacing = BATTLE_GRID_SPACING * (cols - 1)
    val available = (windowWidth - BATTLE_GRID_HORIZONTAL_PADDING - interCardSpacing)
        .coerceAtLeast(0.dp)
    val natural = (available / cols).coerceIn(BATTLE_CARD_MIN_WIDTH, BATTLE_CARD_GROWN_MAX_WIDTH)

    if (natural <= BATTLE_CARD_DEFAULT_WIDTH) return natural
    // `− 1.dp` matches the 1dp VerticalDivider the expanded branch places
    // between the grid and the detail pane, so `grid + divider + pane` sums to
    // exactly `windowWidth` when the pane is open.
    val paneReservedCap = (windowWidth - DETAIL_PANEL_PREFERRED_MIN_WIDTH - 1.dp)
        .coerceAtLeast(BATTLE_CARD_DEFAULT_WIDTH)
    return natural.coerceAtMost(paneReservedCap)
}

// Placement spec for battle card reflow when the detail pane opens/closes.
// Tweak this to play with animation feel — swap for spring(), tween() with different
// easings/durations, or keyframes. IntOffset generic because grid animateItem() animates
// the item's (x, y) placement in the viewport.
internal val BATTLE_GRID_PLACEMENT_SPEC: FiniteAnimationSpec<IntOffset> =
    tween(durationMillis = DETAIL_PANE_ANIM_DURATION_MS, easing = FastOutSlowInEasing)
// endregion

// region: Top Pokémon tile sizing
internal val TOP_POKEMON_TILE_MIN_WIDTH = 120.dp
internal val TOP_POKEMON_TILE_MAX_WIDTH = 160.dp
internal val TOP_POKEMON_TILE_SPACING = 8.dp
internal const val TOP_POKEMON_MIN_TILES = 3
// Outer padding of the Top Pokémon card (`cardInnerPadding × 2`), subtracted when
// converting an available width into a tile-fit width. Kept in sync with
// `ResponsivePokemonGridCard.cardInnerPadding`.
internal val TOP_POKEMON_CARD_INNER_PADDING_TOTAL = 24.dp

/**
 * Actual rendered width of the battle-card cluster (all columns + inter-column
 * gaps), which is narrower than the grid box when the grid is centered. The Top
 * Pokémon card sizes itself to match this so both sections align visually.
 */
internal fun computeBattleGridRenderedWidth(
    gridBoxWidth: Dp,
    battleCardCellWidth: Dp
): Dp {
    val available = (gridBoxWidth - BATTLE_GRID_HORIZONTAL_PADDING + BATTLE_GRID_SPACING)
        .coerceAtLeast(0.dp)
    val cols = (available.value / (battleCardCellWidth + BATTLE_GRID_SPACING).value)
        .toInt().coerceAtLeast(1)
    return battleCardCellWidth * cols + BATTLE_GRID_SPACING * (cols - 1)
}

internal fun computeTopPokemonTileCount(innerWidth: Dp): Int {
    val tile = (TOP_POKEMON_TILE_MIN_WIDTH + TOP_POKEMON_TILE_SPACING).value
    val raw = ((innerWidth.value + TOP_POKEMON_TILE_SPACING.value) / tile).toInt()
    return raw.coerceAtLeast(TOP_POKEMON_MIN_TILES)
}

internal fun computeTopPokemonTileWidth(innerWidth: Dp, tileCount: Int): Dp {
    if (tileCount <= 0) return TOP_POKEMON_TILE_MIN_WIDTH
    return ((innerWidth - TOP_POKEMON_TILE_SPACING * (tileCount - 1)) / tileCount)
        .coerceIn(TOP_POKEMON_TILE_MIN_WIDTH, TOP_POKEMON_TILE_MAX_WIDTH)
}
// endregion

// region: Section group sizing
//
// `SectionGroup` column sizing on desktop web. The column count is derived
// dynamically from the available width so each column has at least
// [SECTION_GROUP_COLUMN_MIN_WIDTH] of space to live in, never exceeding the
// section count (we'd rather leave a 4-section group in 4 visual columns
// than 3 cols with one column holding two sections). Once col count is
// chosen, every `SectionGroupItem` is forced to exactly
// [SECTION_GROUP_ITEM_WIDTH] — narrower than the natural `grid-inner /
// cols` split — so the packed block sits compressed toward the center with
// visible gutters on either side instead of each chip list hugging the
// left of a wider-than-needed column.
//
// Content width is the grid's inner display width
// (`expandedTopPokemonMaxWidth`), which shrinks when the battle detail
// pane is open.
internal val SECTION_GROUP_COLUMN_MIN_WIDTH = 360.dp
internal val SECTION_GROUP_ITEM_WIDTH = 320.dp

internal fun sectionGroupColumnCount(contentWidth: Dp, sectionCount: Int): Int {
    // Max cols that fit at [SECTION_GROUP_COLUMN_MIN_WIDTH] each, solving:
    //   cols * MIN + (cols − 1) * spacing ≤ contentWidth
    // → cols ≤ (contentWidth + spacing) / (MIN + spacing)
    val step = SECTION_GROUP_COLUMN_MIN_WIDTH + ContentListItemSpacing
    val available = contentWidth + ContentListItemSpacing
    val maxCols = (available.value / step.value).toInt()
    return maxCols.coerceIn(1, sectionCount.coerceAtLeast(1))
}
// endregion
