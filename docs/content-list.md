# Content List Architecture

`ContentListPage` (Android/Web) / `ContentListView` (iOS) is the app's primary reusable screen. It renders paginated, heterogeneous lists of battles, Pokemon, and players across five distinct modes, each with its own header, data-fetching logic, and content structure.

## Shared Models

All shared models live in `shared/.../ui/model/`.

### ContentListMode

Sealed class defining the six modes. Each mode maps to a header via `toHeaderUiModel()`.

**When adding a new mode**, you must also integrate it into the deep linking system (shared parser, resolver, per-platform handling, web browser history, tests, docs). See the "Deep Linking" checklist in `.claude/rules/coding-conventions.md` and the URL scheme table in [`docs/navigation.md`](navigation.md).

| Mode | Parameters | Header | Sort toggle | Pagination |
|---|---|---|---|---|
| `Home` | — | `HomeHero` | No (has format selector) | Yes |
| `Favorites(contentType)` | `FavoriteContentType` enum | `FavoritesHero` | No | No (loads all at once) |
| `Search(params)` | `SearchParams` | `SearchFilters` | Yes | Yes |
| `Pokemon(pokemonId, name, imageUrl, typeImageUrl1, typeImageUrl2, formatId?)` | Optional `formatId` threaded from battle detail | `PokemonHero` | Yes | Yes |
| `Player(playerId, playerName, formatId?)` | Optional `formatId` threaded from battle detail | `PlayerHero` | Yes | Yes |
| `TopPokemon(formatId?)` | Optional `formatId` threaded from Home page | `TopPokemonHero` | No (has format selector + search field) | No |

### ContentListHeaderUiModel

Sealed class with seven variants controlling what renders above the list:

- **`None`** — no header
- **`HomeHero`** — Logo + "ARC" branding text in Orbitron font
- **`TopPokemonHero`** — "Top Pokemon" title (separate type for planned redesign)
- **`FavoritesHero`** — Currently blank (TODO: branded asset pending from artist)
- **`SearchFilters`** — flow row of removable filter chips (Pokemon with items/tera, format, rating range, unrated, player name, date range). Each chip type has `canRemove*()` / `remove*()` methods on `SearchParams` controlling removability. On desktop web (`WindowSizeClass.Expanded`) the chip row spans the grid's full cell-pack width (no 900dp centered cap), matching the width of the battle-card grid below. Mobile web / Android / iOS keep the 900dp centered cap.
- **`PokemonHero`** — large Pokemon avatar (158dp circle / 227dp sprite) + name (headlineMedium/20pt) + type icons (24dp)
- **`PlayerHero`** — player name in rounded pill background

### ContentListItem

Sealed class for heterogeneous list rendering. Each variant has a `listKey: String` for stable LazyList/List keys.

| Variant | Usage | listKey |
|---|---|---|
| `Battle(uiModel)` | Battle card | `"battle_{id}"` |
| `Pokemon(id, name, imageUrl, types)` | Pokemon row (favorites, search pinned) | `"pokemon_{id}"` |
| `Player(id, name)` | Player row (favorites, search pinned) | `"player_{id}"` |
| `Section(header, items, trailingAction?)` | Grouping container with title and optional trailing action (e.g., `SectionAction.SeeMore` renders "See More" + chevron) | `"section_{header}"` |
| `SectionGroup(sections)` | Wraps multiple `Section`s so desktop web can render them as a responsive multi-column row. Other platforms flatten via `unwrapSectionGroups()` and render each inner section vertically with no visual change. | `"section_group_{joined headers}"` |
| `HighlightButtons(buttons)` | Player profile highlight cards (Top Rated / Latest Rated) | `"highlight_buttons"` |
| `PokemonGrid(pokemon)` | Grid of Pokemon (home "Top Pokémon", player profile "Favorite Pokemon"). 3 columns on iPhone/Android phone, up to 6 on iPad/Android tablet/desktop web | `"pokemon_grid"` |
| `StatChipRow(chips)` | Horizontal scrolling row of chips with name+percent and optional image (mobile), FlowRow (desktop web). Used for Top Abilities, Items, Moves, Tera Types, and pokemon profile "Top Teammates" (chips carry a `pokemonId` and render a Pokemon avatar; tapping navigates to that Pokemon). | `"stat_chip_row"` |
| `FormatSelector` | Format dropdown rendered as a list item (Home, TopPokemon, Pokemon, Player modes) | `"format_selector"` |
| `SearchField(query)` | Text input for client-side filtering (TopPokemon mode) | `"search_field"` |

`ContentListItemMapper` (in `shared/.../ui/mapper/`) provides factory methods: `fromBattles()`, `fromPokemon()`, `fromPlayers()`, `fromPokemonCatalog()`.

## Page 1 vs Page 2+ Content Structure

This is a key behavioral detail: several modes compose a richer page 1 with sections, while pages 2+ append bare battle items.

### Home mode
- **Page 1**: Up to 3 items — format detail + battles fetched in parallel via `getFormatDetail(formatId, topPokemonCount=N)` + `getBestPreviousDay(formatId)` (server-cached endpoint, returns flat list without pagination). `N` defaults to 6 (mobile/Android/iOS) and is bumped up on desktop web — see "Responsive Top Pokémon row" below. Format detail errors are silently swallowed; page still shows battles. Pagination (`hasNext`) is inferred from result size (>= 10 implies more pages). `currentPage` is set to `battlesCount / 10` so that page 2+ pagination aligns with `searchMatches`'s default limit of 10 (e.g., 50 battles → `currentPage=5`, next page requests page 6). Deduplication in `paginate()` handles any overlap when the count is not evenly divisible.
  1. `FormatSelector` — format dropdown (same as Pokemon/Player modes), fed from app config default format
  2. `Section("Top Pokémon", [PokemonGrid([...])])` — grid of top usage Pokemon with usage %. Mobile/iPhone/iPad/Android: 3-col card (iPhone/phone) or up to 6-col card (iPad/tablet). Desktop web: responsive single-row card — see "Responsive Top Pokémon row" below. Has `SectionAction.SeeMore` trailing action (renders "See More" + chevron button in section header). Only shown if format detail succeeds and has Pokemon.
  3. `Section("Today's Top Battles", [...])` — battle results sorted by rating from last 24 hours. No sort toggle.
  Both sections are omitted if their data is empty. If both API calls fail, the error state shows. If one fails, that section is omitted.
- **Pages 2+**: bare `Battle` items via `searchMatches` (last 24h, rating sort, limit=10)
- **Format change**: reloads all sections (`loadingSections = {"format_selector", "Top Pokémon", "Today's Top Battles"}`)

#### Responsive Top Pokémon row (desktop web only)

On the `WindowSizeClass.Expanded` branch of `webApp/.../ui/contentlist/ContentListContent.kt`, the Home page's Top Pokémon section is rendered as a single horizontal row of Pokémon tiles that fills the available grid-box width and is centered within the grid box (via `SectionContentAlignedHeader`'s `(reportedWidth − contentWidth) / 2` placement), instead of the centered 900dp-capped card used on mobile/iPad.

- **Fetch count**: `ContentListLogic.setTopPokemonFetchCount(count)` (Home mode only) targets the count that would fill the **pane-closed** battle grid rendered width, so when the user closes the battle detail pane, enough tiles are already cached to fill the wider row without a re-fetch. The count is derived from `computeBattleGridRenderedWidth(maxWidth, battleCardCellWidth)` minus card padding, via `computeTopPokemonTileCount()`, and passed via `LaunchedEffect` from inside the expanded branch's `BoxWithConstraints`.
- **Monotonic fetch-above-peak**: `setTopPokemonFetchCount` tracks `topPokemonFetchedCount` (the largest count actually fetched so far) separately from `topPokemonFetchCount` (the current target). Decrease-then-increase-below-peak does not re-fetch — the UI re-slices the already-loaded list. Only a count strictly greater than `topPokemonFetchedCount` triggers a network round-trip.
- **Race with initial load**: `setTopPokemonFetchCount` awaits `_uiState.first { !it.isLoading }` before applying its state update, so the initial `loadContent()` coroutine (which may have captured the old `topPokemonFetchCount` at async-launch time) can't overwrite the re-fetch's replacement of the Top Pokémon section.
- **Display count on pane toggle**: `ResponsivePokemonGridCard` (in `webApp/.../ui/contentlist/ContentListItemRow.kt`) receives an `availableWidth: Dp` parameter — the current grid-box width (shrinks when the detail pane opens). It uses that to derive the visible tile count and wraps invisible overflow tiles in `AnimatedVisibility(visible = false)` so they fade out during the 300ms pane animation. No network traffic on pane toggle.
- **Escape the battle grid's cell-pack constraint**: the LazyVerticalGrid uses `GridCells.FixedSize(battleCardCellWidth)` so a `fullSpan` item's natural max width is `cellCount × battleCardCellWidth + (cellCount−1) × 12`, which can leave unused space on the right. `ResponsivePokemonGridCard` uses a `Modifier.layout` shim that re-measures its child with the parent-provided `availableWidth` (the true grid-box width) but **reports** the grid's original `constraints.maxWidth` as its layout size. Under the grid's `CenterHorizontally` arrangement, the full-span item is placed at the cell-pack-centered position; `SectionContentAlignedHeader` then places the wider child at `(reportedWidth − childWidth) / 2` (a negative offset in the overflow case) so the tiles draw symmetrically into both gutters.
- **Hit-testing caveat**: Compose tests pointer events against the placeable's actual (wider) bounds, so tiles drawn beyond `reportedWidth` still receive clicks. This works today but is an implementation detail — if it ever breaks, the symptom would be rightmost tiles becoming unclickable while still visible.
- **Player "Favorite Pokémon"** flows through the same `ResponsivePokemonGridCard` render path and reflows on pane toggle. It does *not* drive `setTopPokemonFetchCount` (that's Home-only) — it just displays as many of the already-fetched Pokemon as fit. Pokemon "Top Teammates" is rendered as a `StatChipRow` with clickable pokemon-avatar chips and does not use `ResponsivePokemonGridCard`.
- **Tile sizing**: Tiles flex within `[TOP_POKEMON_TILE_MIN_WIDTH = 120.dp, TOP_POKEMON_TILE_MAX_WIDTH = 160.dp]` (spacing: `TOP_POKEMON_TILE_SPACING = 8.dp`, min tiles: `TOP_POKEMON_MIN_TILES = 3`). The tile width is computed to make the card's outer width exactly match `computeBattleGridRenderedWidth()` — the actual rendered width of the battle-card cluster below. `computeTopPokemonTileCount()` maximizes the number of tiles that fit at `>= MIN_WIDTH`, then `computeTopPokemonTileWidth()` distributes the remaining space evenly so the fit is exact. The card wraps to its tile content (border follows the rightmost tile's edge).
- **Mobile web / Android / iOS are unaffected** — they continue using the legacy 3/6-col `PokemonGrid` render in `ContentListItemRow.kt`'s existing branch.

### Favorites mode
- Single page (no pagination): flat list of `Battle`, `Pokemon`, or `Player` items depending on `contentType`

### Search mode
- **Page 1**: Up to 3 sections —
  1. `Section("Pokemon", [...])` — pinned Pokemon from search filters (resolved from catalog)
  2. `Section("Players", [...])` — matching players (if `playerName` filter set, fuzzy-matched via `searchPlayersByName`)
  3. `Section("Battles", [...])` — battle results
- **Pages 2+**: bare `Battle` items (appended to flat list, no wrapping section)

### Pokemon mode
- **Page 1**: Up to 3 top-level items — profile + battles fetched in parallel via `getPokemonProfile(id, formatId)` + `searchMatches(...)`. Profile errors are silently swallowed; page still shows battles.
  1. `FormatSelector` — format dropdown (rendered as a centered list item)
  2. `SectionGroup([...])` — wraps the five profile stat sections (below) into a single group so desktop web can lay them out as a responsive 1/2/3-column row. Only emitted when the profile succeeds and contains at least one non-empty stat section. The group contains only non-empty sections — a section is omitted when the profile has no data for it (e.g. formats without tera types skip `Top Tera Types`).
     - `Section("Top Teammates", [StatChipRow([...])])` — chip row of top teammates with pokemon avatar, name, and usage %. Each chip carries a `pokemonId`; tapping navigates to that Pokemon's page.
     - `Section("Top Items", [StatChipRow([...])])` — chip row of items with image, name, and usage %.
     - `Section("Top Tera Types", [StatChipRow([...])])` — chip row of tera types with image, name, and usage %.
     - `Section("Top Moves", [StatChipRow([...])])` — chip row of moves with usage %. Name + percent only.
     - `Section("Top Abilities", [StatChipRow([...])])` — chip row of abilities with usage %. Name + percent only.
  3. `Section("Battles", [...])` — battle results
  All profile sections are from the pokemon profile API (count / matchCount).

  **Desktop web rendering**: the `SectionGroup` is emitted as one full-span grid item handled by the private `SectionGroupLayout` composable in `webApp/.../ui/contentlist/ContentListContent.kt`. It's a `SubcomposeLayout` that:

  1. Computes column count dynamically via `sectionGroupColumnCount(contentWidth, sections.size)` — the max number of columns that fit at `SECTION_GROUP_COLUMN_MIN_WIDTH (280dp)` steps, capped at `sections.size` so a 4-section group gets 4 columns as soon as the grid-inner width permits. The lower threshold ensures 2 columns fit when the battle detail pane is open (~588dp content width, including the Usage tab's left Pokemon list pane).
  2. Subcomposes each section (inline `Text` header + `StatChipRow` content, wrapped in `SectionGroupItem`) and measures it at a column constraint capped at `SECTION_GROUP_ITEM_WIDTH` (320dp) or the available-per-column width when space is tight (e.g., detail pane open). `SectionGroupItem` forces its outer `Column` to a fixed width via `Modifier.width(SECTION_GROUP_ITEM_WIDTH)` (320dp), and uses an inline `Text` header — not the shared `SectionHeader` — so it can actually shrink below the col-slot width (the shared `SectionHeader` calls `fillMaxWidth()`).
  3. Greedy-packs sections into columns in shared-emission order (Teammates, Items, Tera, Moves, Abilities): each section goes into the column with the smallest cumulative height, with `IntArray.indices.minBy` breaking ties toward the leftmost column. Because the first sections are the tall ones, this naturally places Teammates and Items on row 1 side-by-side, and drops Abilities into whichever column ended up shortest (tie → left).
  4. Places each section's placeable at its assigned column's `(baseX + colIdx × (colWidthPx + spacing), y)`, where `baseX = (reportedWidth − totalContentWidth) / 2` centers the packed block horizontally within the full-span item's reported box. `colWidthPx` is the widest measured section (uniform col slots keep stacked sections in the same column sharing a left edge). Under the grid's `CenterHorizontally` arrangement, the item's reported box itself sits at the cell-pack-centered position, so the net effect is a packed block centered within the grid box with visible gutters on either side. Columns grow independently in `y` (ragged bottom).
  5. The item still reports `constraints.maxWidth` (the cell-pack width) as its layout size, so the enclosing `LazyVerticalGrid` treats it like any other full-span item. When the packed block is narrower than the cell pack, `baseX` is positive and it sits centered inside it. When it's wider (doesn't happen with current data but possible under extreme viewports), `baseX` goes negative and overflows symmetrically into both gutters — same hit-testing caveat as `ResponsivePokemonGridCard`.

  Per-section loading opacity is applied inside `SectionGroupItem` (`Modifier.alpha(0.5f)` when the section's header is in `loadingSections`), so the architecture supports independently-loading sections in the future even though Pokemon mode currently loads them as a single profile API call.

  **Android / iOS / mobile web**: these platforms call `unwrapSectionGroups()` on `uiState.items` at the rendering boundary, which replaces each `SectionGroup` with its inner sections in place. The existing per-`Section` render path then handles them exactly as before (vertical stacking). Android tablet and iPad multi-column layouts can opt in to rendering the group directly in the future. The pagination-dedup logic in `ContentListLogic.paginate()` uses a recursive `collect()` that walks through both `Section.items` and `SectionGroup.sections` so deduplication keys stay correct regardless of how sections are nested.

  **Edge-to-edge children on compact web**: the `StatChipRow` child of a section has `edgeToEdge = true`. On compact web, the section-children emission loop detects this flag and renders the child via a `Modifier.layout` escape (`escapeGridHorizontalPadding`) instead of wrapping it in `CenteredItem`. The helper re-measures the child at `constraints.maxWidth + 32dp` and places it at `x = -16dp`, which negates the parent `LazyVerticalGrid`'s `padding(horizontal = 16.dp)` so the chip carousel's `LazyRow` scrolls flush to the viewport edges. The layout still reports `constraints.maxWidth` back up so sibling items are unaffected.
- **Progressive loading**: Initial load uses `loadPokemonPage1()` which handles the two parallel API calls with progressive rendering. If the profile finishes before battles, profile sections are shown immediately with the Battles section header displaying a loading indicator in the sort toggle (`loadingSections = {"Battles"}`). If battles finishes before the profile, the unified loading spinner is maintained until the profile also completes (avoids layout jumpiness). Format change and sort toggle use the standard `fetchContent()` path with section-level loading.
- **Pages 2+**: bare `Battle` items
- **Format change**: reloads all profile sections and battles (`loadingSections = {"format_selector", "Top Teammates", "Top Items", "Top Moves", "Top Abilities", "Top Tera Types", "Battles"}`) since the pokemon profile endpoint accepts `format_id`

### Player mode
- **Page 1**: Up to 4 items —
  1. `HighlightButtons([...])` — "Top Rated Battle" + "Latest Rated Battle" cards (from player profile API, if available)
  2. `Section("Favorite Pokemon", [PokemonGrid([...])])` — 3-column grid of most-used Pokemon (from player profile API, if available)
  3. `FormatSelector` — format dropdown (rendered as a centered list item)
  4. `Section("Battles", [...])` — battle results
- **Pages 2+**: bare `Battle` items

### TopPokemon mode
- **Single page** (no pagination): Fetches top 100 Pokemon via `getFormatDetail(formatId, topPokemonCount=100)`.
  1. `FormatSelector` — format dropdown
  2. `SearchField("")` — text field for client-side name filtering
  3. `Pokemon` items with `usagePercent` — full list filtered by search query
- **Format change**: reloads from API (`loadingSections = {"format_selector"}`), clears search query
- **Search filtering**: client-side via `setSearchQuery()`, filters stored Pokemon list by name (case-insensitive), no API call
- `ContentListLogic.allTopPokemonItems` exposes the unfiltered loaded list as a `StateFlow` so the desktop-web Usage layout can measure left-pane width against the longest name
- Navigated to from Home page's "See More" button on the "Top Pokemon" section, threading the current format

#### Desktop-web Usage layout
On the `WindowSizeClass.Expanded` branch, the Usage tab is rendered by `webApp/.../ui/contentlist/UsageDesktopPage.kt` instead of the standard `ContentListPage`. Mobile web, Android, and iOS continue to use the standard `ContentListPage` rendering of TopPokemon mode (single column with type icons).

- **Two-pane master/detail**: a left column pinned to the width of the longest Pokemon name + avatar + usage percent, and a right pane that takes the remaining width.
- **Left column**: format dropdown, search field (fills column width), then a vertically scrolling list of simplified rows `[avatar][name][usage%]` with no type icons. Width is computed once via `rememberTextMeasurer()` over `allTopPokemonItems` after the catalog loads, sized to fit the longest Pokemon name + avatar + usage percent (and the widest format dropdown display name, whichever is larger). Selected row uses a `primary` border at 2× width. While the catalog is loading the entire pane shows a centered `LoadingIndicator`.
- **Right pane**: renders a nested `ContentListPage(mode = Pokemon(...))` for the currently-selected Pokemon. The first Pokemon is auto-selected once data is ready. Selecting a different Pokemon from the left list resets the right pane's nested nav stack via `onClearUsageNestedStack`.
- **Nested navigation**: `WebApp.kt` owns a `usageNestedStack: List<NavEntry>` parallel to `desktopNavStack` (Pokemon/Player drill-downs from inside the right pane). Browser back pops `usageNestedStack` first (before `desktopNavStack`), then `searchOverlayParams`. Battle detail clicks inside the right pane are handled by the nested page's own inline pane, producing a three-pane layout `[Usage list | Pokemon page | Battle detail]` when the viewport is wide enough.
- **URL mirroring**: the selected Pokemon ID is reflected in the URL as `/top-pokemon?f={formatId}&pokemon={id}` (or `/usage?pokemon={id}` when typed manually). On mobile platforms the same URL navigates directly to `/pokemon/{id}` instead of selecting it on a Usage list — see the Deep Linking section in `docs/navigation.md`.

## Section Loading & Sort Toggle

Documented in detail in [`docs/search.md`](search.md) under "Sort Toggle & Section Loading". Key points:

- Sort toggle appears on the "Battles" `SectionHeader` in Search, Pokemon, and Player modes
- Toggling sets `loadingSections = setOf("Battles")`, re-fetches page 1 with new sort order
- Section children render at 50% opacity (Android/Web) or with a spinner overlay (iOS) while loading
- **Pagination guard**: `paginate()` refuses to run when `loadingSections.isNotEmpty()` — prevents race conditions between sort/format fetches and pagination

## Format Selection (Home, TopPokemon, Pokemon & Player Modes)

A `FormatSelector` list item renders a format dropdown in Home, TopPokemon, Pokemon, and Player modes. It appears as a centered dropdown between the header/profile content and the Battles section.

- **Default format**: inherited from `formatId` parameter (threaded from battle detail or parent page's selected format) or falls back to app config's default format
- **On change**: sets `loadingSections = setOf("Battles")`, re-fetches page 1 with new format
- **Format threading**: When navigating to a Player from battle detail, the battle's `formatId` is injected via `wrappedOnPlayerClick` (same pattern as Pokemon). When navigating from search results or another Player/Pokemon page, the current page's `selectedFormatId` is passed.
- **Platform differences**:
  - Android: `DropdownMenu` composable
  - iOS: `Menu` with `.presentationDetents([.medium])`
  - Web: `Dialog` composable

## Favorites Auto-Refresh

Pokemon and Player favorites modes observe the corresponding `StateFlow` from `FavoritesRepository`:

- **Android**: ViewModel collects `favoritePokemonIds` / `favoritePlayerNames` flow, auto-refreshes list when set changes
- **iOS**: `ContentListView` uses `.onChange(of: favoritesStore.favoritePokemonIds/favoritePlayerNames)` modifier
- **Web**: same pattern as Android via `collect`

Battle favorites mode uses `fetchContent()` on initial load (no live observation — favorites don't change while viewing the list).

## Battle Detail Page & Navigation

Each `ContentListPage`/`ContentListView` manages three local state variables:
- `selectedBattleId: Int?` — which battle detail page to show
- `pokemonNavTarget` — Pokemon drill-down destination
- `playerNavTarget` — Player drill-down destination

Battle detail renders as a full-screen page overlay (Android: `BattleDetailPage` in `Box` with `BackHandler`; iOS: pushed via `.navigationDestination`; Web desktop: inline pane; Web mobile: `NavEntry.BattleDetail` stack entry).

When a Pokemon or Player is tapped from within battle detail:
1. The nav target is set
2. The Pokemon/Player page pushes on top of battle detail in the nav stack
3. On back, the nav target clears → user returns to battle detail, then back again returns to list

This is recursive — each child instance has its own independent state, creating a natural navigation stack.

**Format threading**: When navigating to a Pokemon or Player from battle detail, the battle's `formatId` is injected at the boundary (Android: inline in `ContentListPage`, iOS: `BattleDetailPage`, Web: `BattleDetailPanel`) by wrapping the click callbacks to append the format. When navigating from search results, the search's format is passed. When navigating from another Pokemon/Player page, the current page's `selectedFormatId` is passed.

### Web Desktop: Multi-Column Battle Grid & Scroll Restoration

On desktop web (expanded size class), the content list uses `LazyVerticalGrid` with `GridCells.FixedSize(battleCardCellWidth)` instead of `LazyColumn`. `battleCardCellWidth` is derived from the total window width at discrete column-count breakpoints via `computeBattleCardCellWidth()` — cards grow within `[BATTLE_CARD_MIN_WIDTH, BATTLE_CARD_GROWN_MAX_WIDTH]` (560–780dp) to fill the available space, with column count stepping up at each `BATTLE_CARD_DEFAULT_WIDTH` (620dp) boundary so each column has at least the designed card width. The value is keyed on **total window width** rather than grid-box width, so it's stable across detail-pane open/close — only the column count changes during the transition, not the individual card size. Narrow viewports (below the 2-column breakpoint) fall back to `BATTLE_CARD_DEFAULT_WIDTH` so compact mobile layouts are unchanged. This provides:

- **Top-level non-section items** (heroes, format selectors, search field, etc.) span the full grid width via `GridItemSpan(maxLineSpan)` and are centered with a `CONTENT_MAX_WIDTH` (900dp) cap via the `CenteredItem` wrapper composable. Section headers follow their own layout rules — see the section-emission bullets below.
- **Section emission** is content-structure-dependent. Sections whose items report `requiresIndividualGridCells = true` (currently only `Battle`) emit a separate full-span header item followed by N individual grid cells — required because battle cards need the grid's column layout, animated reflow (`animateItem`), and `scrollToItem` targeting. The header's width comes from `BoxWithConstraints { maxWidth }` inside the fullSpan item, which is exactly the cell-pack width (`cols × battleCardCellWidth + (cols-1) × BATTLE_GRID_SPACING`) — the right edge of the rightmost battle card. No content-type hardcoding: it's a property of the grid's own layout math applied to any full-span item in a `FixedSize` grid.
- **Non-battle sections on expanded** use `SectionContentAlignedHeader`, a `SubcomposeLayout`-based composable that emits one combined full-span grid item containing both the section header and all its children. Content is composed and measured first with a loose upper bound (the grid-box inner width); the header is then composed at exactly the measured content width (`minWidth = maxWidth = contentWidth`) so a trailing action (See More / sort toggle) lands at the content's actual right edge — which can extend past the cell pack via `ResponsivePokemonGridCard`'s `Modifier.layout` escape. The item always reports the grid's cell-pack width back up (`layout(constraints.maxWidth, ...)`), so `LazyVerticalGrid` places it at x=0 of the content area — the same left edge as the Battles section header and the leftmost battle card — regardless of whether content is narrower or wider than the cell pack. Placeables are placed at (0, y) and may draw beyond the reported box into the grid's unused trailing gutter. A vertical gap of `ContentListItemSpacing` separates header from content, matching the gap the grid's vertical arrangement provides between the Battles header and its cards.
- The dispatch for the two emission strategies hinges on a single declarative property `ContentListItem.requiresIndividualGridCells` (default `false`, overridden on `Battle`). No per-composable content-type checks inside the section branch. Compact window size class always takes the split-emission path (header + children as separate items) with the legacy `CenteredItem` 900dp cap.
- **Battle cards** are emitted as individual grid items (one per card). Column count is derived from the grid's available width and the dynamically-sized `battleCardCellWidth`. Cards are centered (`Arrangement.spacedBy(BATTLE_GRID_SPACING, Alignment.CenterHorizontally)`) so the cell pack sits symmetrically within the grid box. `animateItem(placementSpec = BATTLE_GRID_PLACEMENT_SPEC)` smooths the x/y reflow when the detail pane's open/close changes the column count.
- **Animated reflow**: Each battle card uses `Modifier.animateItem(placementSpec = BATTLE_GRID_PLACEMENT_SPEC)` with a `tween(DETAIL_PANE_ANIM_DURATION_MS, FastOutSlowInEasing)` placement spec, so cards smoothly slide to their new grid positions when the detail pane's width changes the grid's column count. The spec constant is declared at the bottom of `ContentListPage.kt` and can be tuned independently.

**Snap-to-narrow grid width**: When `selectedBattleId` becomes non-null, the grid's outer Box modifier swaps from `Modifier.weight(1f)` to `Modifier.width(gridWidthWhenPaneOpen)` in the same composition. The grid remeasures with the new narrow width in a single frame — typically into a 1-column layout. This is critical because `LazyVerticalGrid.scrollToItem(N)` in a multi-column grid gets clobbered back to the row-start by the next measure pass (`LazyGridScrollPosition.updateFromMeasureResult`). By ensuring the grid is already in its final (usually 1-col) layout before `scrollToItem` runs, the target index sticks.

The pane width is sized dynamically via `BoxWithConstraints`: `panePostWidth = (maxWidth - battleCardCellWidth - 1.dp).coerceIn(0.dp, DETAIL_PANEL_MAX_WIDTH)`. On wide viewports the pane gets its full 960dp; on narrower viewports it shrinks so the grid can hold a full battle card at the currently-derived cell width. `grid + 1dp divider + pane = maxWidth` exactly, so there's no unused Row space. `computeBattleCardCellWidth` caps card growth so the pane retains at least `DETAIL_PANEL_PREFERRED_MIN_WIDTH` (928dp) when the viewport allows it — keeping 3-pokemon-per-row in each team card. The cap floor is pinned at the default width (620dp) so cards never drop *below* the default just to buy pane width, avoiding a visible card-width jolt when resizing through the threshold where the cap first becomes relevant.

**Pane animation**: The pane uses `slideInHorizontally`/`slideOutHorizontally` (not `expandHorizontally`/`shrinkHorizontally`) so the pane's layout bounds snap to their final width the moment `visibleState` becomes true, and the content translates in from off-screen-right. This avoids an artifact where the pane's growing right edge would leave a visible sliver of the outer `surface` background to its right.

**Scroll restoration** (simplified to a single policy): A `lastSelectedBattleId` state variable tracks the most recently selected battle. On every pane open or switch, the grid calls `scrollToItem(index)` (first open) or `animateScrollToItem(index)` (switch while open). On close, the grid re-scrolls to `lastSelectedBattleId`'s index in the now-wide grid. The second argument to `scrollToItem` is `scrollOffsetPx = BATTLE_GRID_SPACING` converted to pixels via `LocalDensity.current`, providing a small top margin.

`computeBattleItemIndex()` mirrors the grid's item emission order (headers, sections, top-level items) to compute the correct index for a given battle ID. Since each battle is its own grid item (no chunking), the index is a simple linear count. It accepts `hasFormats`/`hasSearchQuery` flags to match conditional item emission.

## Toolbar & Favorite Buttons

Pokemon and Player modes show a toolbar with back button + favorite heart:
- **Pokemon mode**: heart toggles `togglePokemonFavorite(pokemonId)`
- **Player mode**: heart toggles `togglePlayerFavorite(playerName)`
- Toolbar floats over content with a gradient fade background (`surfaceContainer` at 70% → 0% alpha)

## Shared Logic (`ContentListLogic`)

All business logic lives in `shared/.../ui/contentlist/ContentListLogic.kt`. This class manages state, data fetching, pagination, sort/format toggling, and favorites observation. Platform ViewModels are thin wrappers (~20-50 LOC) that provide a `CoroutineScope` and delegate all operations.

**Constructor:**
```kotlin
class ContentListLogic(
    scope: CoroutineScope,           // viewModelScope on Android/Web, MainScope on iOS
    repository: BattleRepositoryApi, // shared BattleRepository implements this
    favoritesRepository: FavoritesRepository,
    appConfigRepository: AppConfigRepository,
    mode: ContentListMode,
    pokemonCatalogItems: List<PokemonPickerUiModel> = emptyList(),
    pokemonCatalogState: StateFlow<CatalogState<PokemonPickerUiModel>>? = null,  // for favorites Pokemon tab
    initialTopPokemonFetchCount: Int = DEFAULT_TOP_POKEMON_COUNT  // Home mode only; 6 by default
)
```

**Public API** (all non-suspend — they launch internally via the injected scope):
- `initialize()` — one-shot init, routes to correct startup path based on mode
- `loadContent()` / `refresh()` — fetch page 1 (loading vs refreshing indicator)
- `paginate()` — next page with guards (isPaginating, canPaginate, loadingSections)
- `selectFormat(formatId)` — format change + section reload
- `toggleSortOrder()` — sort toggle + section reload
- `updateSearchParams(params)` — reset state and reload with new search params
- `setTopPokemonFetchCount(count)` — Home mode only. Sets the target count and, if it exceeds the last-fetched peak, re-fetches just the Top Pokémon section in place. See "Responsive Top Pokémon row" under Home mode above.

**Scope injection pattern:**
- **Android/Web**: pass `viewModelScope` (auto-cancelled on ViewModel clear)
- **iOS**: create via `CoroutineScopeFactory.shared.createMainScope()`, cancel in `deinit`

**iOS StateFlow bridging:**
iOS ViewModel bridges `ContentListLogic`'s `StateFlow` properties to `@Published` properties using `for await` loops on SKIE-generated async sequences.

## ViewModel Keying (Per-Platform)

Each platform creates distinct ViewModel instances per mode to avoid state leakage. Each ViewModel creates its own `ContentListLogic` instance:

- **Android**: `hiltViewModel(key = ...)` — `"content_list_home"`, `"content_list_favorites_{contentType}"`, `"content_list_search_{params}"`, `"content_list_pokemon_{pokemonId}"`, `"content_list_player_{playerId}_{formatId}"`
- **iOS**: new `ContentListViewModel()` instance per `ContentListView` appearance
- **Web**: `remember(mode.toString()) { ContentListViewModel(deps...) }` via `DependencyContainer`

## ContentListUiState

Lives in `shared/.../ui/contentlist/ContentListUiState.kt` (shared across all platforms):

```kotlin
data class ContentListUiState(
    val isLoading: Boolean = true,
    val items: List<ContentListItem> = emptyList(),
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val isPaginating: Boolean = false,
    val currentPage: Int = 1,
    val canPaginate: Boolean = true,
    val loadingSections: Set<String> = emptySet()
)
```

## Empty States

- **Loading**: centered spinner (fills 50% of parent height)
- **Error** (with no content items): `ErrorView` / `ErrorBanner` with retry button
- **Empty** (loaded, no content items): `EmptyView` ("There's nothing here"). Non-content items (e.g. `FormatSelector`) are still rendered above the empty view so the user can change format. The `isContentItem` flag on `ContentListItem` determines whether an item counts as content for empty state purposes (default `true`, `FormatSelector` overrides to `false`).
- Sections are only added to the items list when they have data — empty sections (including Battles with no results) are omitted entirely, and the full-screen empty view shows instead.

## Pagination

- Trigger: when user scrolls within 5 items of the list end (`PAGINATION_THRESHOLD = 5`)
- Guards: `!isPaginating && canPaginate && loadingSections.isEmpty()`
- New items appended with `distinctBy { listKey }` to prevent duplicates
- `canPaginate` set from `pagination.hasNext`

## Home Mode Special Behavior

Home mode waits for app config before loading (needs default format ID):
1. If config already available, syncs `_selectedFormatId` and loads immediately
2. Otherwise, shows loading state and waits for `appConfigRepository.config` to emit non-null, then syncs format ID
3. Fetches format detail (top 6 Pokemon) and battles (via `getBestPreviousDay` cached endpoint) in parallel using the selected format. Page 2+ uses `searchMatches` with last-24h time range and rating sort.
4. Format selector allows changing the format, which reloads both sections

## Key File Locations

| Platform | Files |
|---|---|
| Shared logic | `shared/.../ui/contentlist/ContentListLogic.kt`, `ContentListUiState.kt` |
| Shared models | `shared/.../ui/model/ContentListMode.kt`, `ContentListItem.kt`, `ContentListHeaderUiModel.kt` |
| Shared mapper | `shared/.../ui/mapper/ContentListItemMapper.kt` |
| Shared util | `shared/.../util/CoroutineScopeFactory.kt` |
| Shared tests | `shared/src/commonTest/.../ui/contentlist/ContentListLogicTest.kt` |
| Android | `composeApp/.../ui/contentlist/ContentListPage.kt`, `ContentListViewModel.kt`, `SearchFilterChips.kt`, `ContentListItemRow.kt`, `ContentListComponents.kt` |
| iOS | `iosApp/iosApp/ContentListView.swift`, `ContentListViewModel.swift`, `SearchFilterChipsView.swift`, `ContentListComponents.swift` |
| Web | `webApp/.../ui/contentlist/ContentListPage.kt`, `ContentListContent.kt`, `ContentListContentParams.kt`, `ContentListLayout.kt`, `ContentListNavigation.kt`, `ContentListViewModel.kt`, `SearchFilterChips.kt`, `ContentListItemRow.kt`, `ContentListComponents.kt` |
