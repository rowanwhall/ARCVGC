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
| `Pokemon(pokemonId, name, imageUrl, typeImageUrl1, typeImageUrl2, formatId?, lookback?)` | Optional `formatId` threaded from battle detail; optional `lookback` threaded from a Usage/Pokemon page where the user changed the lookback selector | `PokemonHero` | Yes | Yes |
| `Player(playerId, playerName, formatId?)` | Optional `formatId` threaded from battle detail | `PlayerHero` | Yes | Yes |
| `TopPokemon(formatId?)` | Optional `formatId` threaded from Home page | `None` | No (has format selector + search field) | No |

### ContentListHeaderUiModel

Sealed class with six variants controlling what renders above the list:

- **`None`** — no header (also used by `TopPokemon` mode — the Usage tab has no title header; the format selector sits at the top of the list)
- **`HomeHero`** — Logo + "ARC" branding text in Orbitron font
- **`FavoritesHero`** — Branded favorites mascot image (`favorite` drawable / iOS `Favorite` asset) centered at `HeroLogoHeight`. Renders above the list on all three favorites sub-tabs (Battles / Pokemon / Players), below the tab row. On desktop web it's emitted as a full-span grid item, so `computeBattleItemIndex` counts it like any other header.
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
| `Section(header, items, centerHeader?)` | Grouping container with a title. Desktop-web-only: when `centerHeader = true` the title text is centered within its width instead of left-aligned (only honored when the section has no trailing controls like the sort toggle; mobile + iOS ignore the flag). Used by the Pokemon-profile "Top Players" section. | `"section_{header}"` |
| `SectionGroup(sections, fillWidth)` | Wraps multiple `Section`s so desktop web can render them as a responsive multi-column row. Other platforms flatten via `unwrapSectionGroups()` and render each inner section vertically with no visual change. Desktop web also unwraps singleton groups (`sections.size == 1`) via `unwrapSingletonSectionGroups()` so the lone section renders through the regular full-width Section path instead of being trapped in a 320dp column. `fillWidth` (default `false`, desktop web only): when `true`, columns stretch to evenly fill the full-span grid item's own box — the battle-card cell-pack, centered to the same margins as the battle row below — instead of being capped at 320dp and centered. Used by the Home group so its chip rows spread ~3-4 chips per row and align edge-to-edge with "Today's Top Battles"; the Pokemon-profile group keeps the default capped/centered layout. | `"section_group_{joined headers}"` |
| `HighlightButtons(buttons)` | Player profile highlight cards (Top Rated / Latest Rated) | `"highlight_buttons"` |
| `PokemonGrid(pokemon)` | Grid of Pokemon (player profile "Favorite Pokemon"). 3 columns on iPhone/Android phone, up to 6 on iPad/Android tablet/desktop web | `"pokemon_grid"` |
| `StatChipRow(chips, id)` | Horizontal scrolling row of chips with name+percent and optional image (mobile), FlowRow (desktop web). The `id` disambiguates the `listKey` when a page has several rows. Used for Top Abilities, Items, Moves, Tera Types, pokemon profile "Top Teammates", and the Home page's "Most Used" / "Trending Up" / "Trending Down" sections (chips carry a `pokemonId` and render a Pokemon avatar; tapping navigates to that Pokemon). | `"stat_chip_row_{id}"` |
| `TopPlayerChipRow(players)` | Pokemon profile "Top Players" chips. Compact (mobile + mobile-web): `LazyRow` of two-line chips (name above max rating, matching `StatChipRow`'s layout). Desktop web: `FlowRow` of inline chips rendered as `**name** • maxRating`. Tapping a chip opens `TopPlayerDialog` with a "View player profile" row and a list of the player's top battles; navigating from the dialog dismisses it before pushing the player page / opening the battle detail. | `"top_player_chip_row"` |
| `FormatSelector` | Format dropdown rendered as a list item (Home, Pokemon, Player modes). In TopPokemon mode on mobile/mobile-web, this list item is suppressed — the dropdown is rendered instead inside an anchored `UsageBottomBar` at the bottom of the screen so it's within thumb reach. Desktop web uses `UsageDesktopPage` which has its own format dropdown. | `"format_selector"` |
| `LookbackSelector` | Lookback window segmented selector rendered as a list item adjacent to `FormatSelector` in Home, Pokemon, and TopPokemon modes. The option set is mode-dependent: Pokemon/TopPokemon show the full 4 (`All`/`30 days`/`7 days`/`24 hours`); Home shows only 3 (`30 days`/`7 days`/`24 hours` — no `All`, since the Home usage endpoint rejects `all`) via `LookbackWindow.homeOptions`, defaulting to the middle `7 days`. Suppressed in TopPokemon compact (rendered inside `UsageBottomBar`) and on desktop-web Usage (rendered in `UsageDesktopPage` left pane below the format dropdown). Hidden entirely (and `selectedLookback` forced to `All`) when the current format is historic — see "Lookback Window" section below; Home formats already exclude historic ones from the dropdown so this gating doesn't apply there. Selected segment is outlined in the user's accent color at 2x border width, matching the selected-pokemon outline in `UsageDesktopPage`. Scopes the pokemon profile / format detail / usage API call's `lookback` parameter. | `"lookback_selector"` |
| `SearchField(query)` | Text input for client-side filtering (TopPokemon mode). On mobile/mobile-web, this list item is suppressed and the field is rendered inside the anchored `UsageBottomBar` alongside the format selector. | `"search_field"` |

`ContentListItemMapper` (in `shared/.../ui/mapper/`) provides factory methods: `fromBattles()`, `fromPokemon()`, `fromPlayers()`, `fromPokemonCatalog()`.

## Page 1 vs Page 2+ Content Structure

This is a key behavioral detail: several modes compose a richer page 1 with sections, while pages 2+ append bare battle items.

### Home mode
- **Page 1**: Up to 4 top-level items — format detail + Pokemon usage + battles fetched in parallel via `getFormatDetail(formatId, topPokemonCount=HOME_CHIP_SECTION_COUNT, lookback=selectedLookback)` + `getPokemonUsage(formatId, selectedLookback)` + `getBestPreviousDay(formatId)` (server-cached endpoint, returns flat list without pagination). The two stat sources use the window chosen in the Home `LookbackSelector` (defaults to `Week`); `getBestPreviousDay` is daily and ignores the lookback. `HOME_CHIP_SECTION_COUNT` is 10 — it mirrors the fixed row count the `/pokemon/usage` endpoint returns, so the three chip rows stay visually symmetric. Format-detail and usage errors are silently swallowed; the page still shows battles. Pagination (`hasNext`) is inferred from result size (>= 10 implies more pages). `currentPage` is set to `battlesCount / 10` so that page 2+ pagination aligns with `searchMatches`'s default limit of 10 (e.g., 50 battles → `currentPage=5`, next page requests page 6). Deduplication in `paginate()` handles any overlap when the count is not evenly divisible.
  1. `FormatSelector` — format dropdown (same as Pokemon/Player modes), fed from the preferred-format setting falling back to app config default. Historic formats are excluded from the Home dropdown (`excludeHistoric`), except a user's explicitly-preferred historic format.
  2. `LookbackSelector` — 3-segment selector (`30 days`/`7 days`/`24 hours`, no `All`), defaulting to the middle `7 days`. Drives only the two stat sections below — **not** Today's Top Battles.
  3. `SectionGroup([...], fillWidth = true)` — built by `buildHomeStatGroup(formatDetail, usage)`; wraps up to three `StatChipRow` sections so desktop web lays them out as a responsive multi-column row that **fills the battle-card row width** (`fillWidth = true`), spreading ~3-4 chips per row per section and aligning to the same dynamic horizontal margins as "Today's Top Battles"; other platforms flatten via `unwrapSectionGroups()` and stack them vertically (chips scroll horizontally as a `LazyRow`). Each inner section is omitted when its data is empty; the whole group is omitted if all three are empty. All chips carry a `pokemonId` and navigate to that Pokemon's page on tap.
     - `Section("Most Used", [StatChipRow(...)])` — top usage Pokemon over the selected window; each chip shows the Pokemon avatar, name, and usage % (`count / teamCount`). Section title literal at `ContentListLogic.HOME_MOST_USED_SECTION`. (The full top-usage list lives on the Usage tab, reached via the nav rail / bottom nav.)
     - `Section("Trending Up", [StatChipRow(...)])` — Pokemon whose usage rose most over the window (`usage.increased`); chip subtitle is the signed change, e.g. `+5.09%`. Title at `HOME_TRENDING_UP_SECTION`.
     - `Section("Trending Down", [StatChipRow(...)])` — Pokemon whose usage fell most over the window (`usage.decreased`); chip subtitle e.g. `-6.74%`. Title at `HOME_TRENDING_DOWN_SECTION`.
     - Signed percentages are formatted by `ContentListLogic.formatSignedPercent` (`+`/`-` prefix, 2 decimals).
  4. `Section("Today's Top Battles", [...])` — battle results sorted by rating from the last 24 hours (always daily, via `getBestPreviousDay`; lookback-independent). Title literal at `ContentListLogic.HOME_TOP_BATTLES_SECTION`. No sort toggle. Omitted if empty.
  If all three API calls fail, the error state shows.
- **Pages 2+**: bare `Battle` items via `searchMatches` (last 24h, rating sort, limit=10)
- **Format change**: reloads all sections (`loadingSections = {"format_selector", HOME_MOST_USED_SECTION, HOME_TRENDING_UP_SECTION, HOME_TRENDING_DOWN_SECTION, HOME_TOP_BATTLES_SECTION}`) via the standard `fetchContent()` path.
- **Lookback change**: Home-only path `reloadHomeStatSections()` — fades and re-fetches **only** the three stat sections (`loadingSections = {HOME_MOST_USED_SECTION, HOME_TRENDING_UP_SECTION, HOME_TRENDING_DOWN_SECTION}`), re-running just `getFormatDetail` + `getPokemonUsage` on the new lookback and splicing the rebuilt `SectionGroup` back in after the `LookbackSelector`. `FormatSelector`, `LookbackSelector`, Today's Top Battles, the paginated battle tail, and pagination state are all left untouched (contrast with format change, which reloads all four). The group is dropped entirely if the new window has no mover data.

#### Responsive Pokémon grid row (desktop web only)

The Player profile's "Favorite Pokémon" section is rendered on the `WindowSizeClass.Expanded` branch of `webApp/.../ui/contentlist/ContentListContent.kt` as a single horizontal row of Pokémon tiles (`ResponsivePokemonGridCard` in `ContentListItemRow.kt`) that fills the available grid-box width and is centered within it (via `SectionContentAlignedHeader`'s `(reportedWidth − contentWidth) / 2` placement), instead of the centered 900dp-capped card used on mobile/iPad. It reflows when the battle-detail pane opens/closes without any network traffic.

- **Display count on pane toggle**: `ResponsivePokemonGridCard` receives an `availableWidth: Dp` parameter — the current grid-box width (shrinks when the detail pane opens). It derives the visible tile count from that and wraps invisible overflow tiles in `AnimatedVisibility(visible = false)` so they fade out during the 300ms pane animation.
- **Escape the battle grid's cell-pack constraint**: the LazyVerticalGrid uses `GridCells.FixedSize(battleCardCellWidth)` so a `fullSpan` item's natural max width is `cellCount × battleCardCellWidth + (cellCount−1) × 12`, which can leave unused space on the right. `ResponsivePokemonGridCard` uses a `Modifier.layout` shim that re-measures its child with the parent-provided `availableWidth` (the true grid-box width) but **reports** the grid's original `constraints.maxWidth` as its layout size, so the tiles draw symmetrically into both gutters.
- **Tile sizing**: Tiles flex within `[TOP_POKEMON_TILE_MIN_WIDTH = 120.dp, TOP_POKEMON_TILE_MAX_WIDTH = 160.dp]` (spacing `TOP_POKEMON_TILE_SPACING = 8.dp`, min tiles `TOP_POKEMON_MIN_TILES = 3`). `computeTopPokemonTileCount()` maximizes tiles that fit at `>= MIN_WIDTH`; `computeTopPokemonTileWidth()` distributes the remainder so the fit is exact.
- **Mobile web / Android / iOS** continue using the legacy 3/6-col `PokemonGrid` render in `ContentListItemRow.kt`'s existing branch.
- **Home page chips** do *not* use this path — the home Most Used / Trending Up / Trending Down sections are `StatChipRow`s (see Home mode above), not `PokemonGrid`s.

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
  2. `LookbackSelector` — lookback-window 4-segment selector (centered, directly below the format dropdown). Scopes the pokemon profile API call's `lookback` query param.
  3. `SectionGroup([...])` — wraps the five profile stat sections (below) into a single group so desktop web can lay them out as a responsive 1/2/3-column row. Only emitted when the profile succeeds and contains at least one non-empty stat section. The group contains only non-empty sections — a section is omitted when the profile has no data for it (e.g. formats without tera types skip `Top Tera Types`).
     - `Section("Top Teammates", [StatChipRow([...])])` — chip row of top teammates with pokemon avatar, name, and usage %. Each chip carries a `pokemonId`; tapping navigates to that Pokemon's page.
     - `Section("Top Items", [StatChipRow([...])])` — chip row of items with image, name, and usage %.
     - `Section("Top Tera Types", [StatChipRow([...])])` — chip row of tera types with image, name, and usage %.
     - `Section("Top Moves", [StatChipRow([...])])` — chip row of moves with usage %. Name + percent only.
     - `Section("Top Abilities", [StatChipRow([...])])` — chip row of abilities with usage %. Name + percent only.
  4. `Section("Top Players", [TopPlayerChipRow([...])], centerHeader = true)` — sibling section *below* the stat-section group, centered on its content's natural width on desktop web. Only emitted when `profile.topPlayers` is non-empty. Tapping a chip opens `TopPlayerDialog` (shared Compose on Android/web, `TopPlayerDialogView` on iOS) showing the player's name, "View player profile" navigation row, and a list of that player's top-5 battles. Dialog navigation dismisses the dialog before pushing the player page or opening the battle detail.
  3. `Section("Battles", [...])` — battle results
  All profile sections are from the pokemon profile API (count / matchCount).

  **Desktop web rendering**: when the group contains 2+ sections it's emitted as one full-span grid item handled by the private `SectionGroupLayout` composable in `webApp/.../ui/contentlist/ContentListContent.kt` (described below). When the group contains exactly one section (e.g. closed-teamsheet formats where only `topTeammates` is populated), `unwrapSingletonSectionGroups()` flattens it before rendering so the lone section flows through the standard top-level `Section` path instead — `SectionContentAlignedHeader` renders the header centered with `StatChipRow` content sized to the full grid content width. This avoids the 320dp column trap and matches the visual style of sibling sections like "Battles".

  For multi-section groups, `SectionGroupLayout` is a `SubcomposeLayout` that:

  1. Computes column count dynamically via `sectionGroupColumnCount(contentWidth, sections.size)` — the max number of columns that fit at `SECTION_GROUP_COLUMN_MIN_WIDTH (280dp)` steps, capped at `sections.size` so a 4-section group gets 4 columns as soon as the grid-inner width permits. The lower threshold ensures 2 columns fit when the battle detail pane is open (~588dp content width, including the Usage tab's left Pokemon list pane).
  2. Subcomposes each section (inline `Text` header + `StatChipRow` content, wrapped in `SectionGroupItem`) and measures it at a per-column width passed as `itemWidth`. **Default (`fillWidth = false`)**: `itemWidth = SECTION_GROUP_ITEM_WIDTH` (320dp) or the available-per-column width when space is tight (e.g., detail pane open); `SectionGroupItem` forces its outer `Column` to that fixed width via `Modifier.width(itemWidth)`, and uses an inline `Text` header — not the shared `SectionHeader` — so it can actually shrink below the col-slot width (the shared `SectionHeader` calls `fillMaxWidth()`). **`fillWidth = true` (Home)**: `itemWidth = (reportedWidth − (cols−1)·spacing) / cols` where `reportedWidth` is the full-span item's own box (the battle-card cell-pack), so each column stretches to fill it and the `StatChipRow` `FlowRow` flows ~3-4 chips per row instead of 2.
  3. Greedy-packs sections into columns in shared-emission order (e.g. Teammates, Items, Tera, Moves, Abilities for the profile group): each section goes into the column with the smallest cumulative height, with `IntArray.indices.minBy` breaking ties toward the leftmost column. Because the first sections are the tall ones, this naturally places Teammates and Items on row 1 side-by-side, and drops Abilities into whichever column ended up shortest (tie → left). For the Home group (3 sections, 3 columns on a wide viewport) packing is 1:1 — each section gets its own column.
  4. Places each section's placeable at its assigned column's `(baseX + colIdx × (colWidthPx + spacing), y)`, where `baseX = (reportedWidth − totalContentWidth) / 2` centers the packed block horizontally within the full-span item's reported box. `colWidthPx` is the widest measured section in default mode, or exactly the fill `itemWidth` when `fillWidth`. In default mode the packed block is narrower than the cell pack so `baseX` is positive (centered, side gutters); in `fillWidth` mode the block ≈ the cell pack so `baseX ≈ 0` and the columns align with the box edges — i.e. the same dynamic horizontal margins as the centered battle-card row below. Columns grow independently in `y` (ragged bottom).
  5. The item still reports `constraints.maxWidth` (the cell-pack width) as its layout size, so the enclosing `LazyVerticalGrid` treats it like any other full-span item. When the packed block is narrower than the cell pack, `baseX` is positive and it sits centered inside it. When it's wider (doesn't happen with current data but possible under extreme viewports), `baseX` goes negative and overflows symmetrically into both gutters — same hit-testing caveat as `ResponsivePokemonGridCard`.

  Per-section loading opacity is applied inside `SectionGroupItem` (`Modifier.alpha(0.5f)` when the section's header is in `loadingSections`), so the architecture supports independently-loading sections in the future even though Pokemon mode currently loads them as a single profile API call.

  **Android / iOS / mobile web**: these platforms call `unwrapSectionGroups()` on `uiState.items` at the rendering boundary, which replaces each `SectionGroup` with its inner sections in place. The existing per-`Section` render path then handles them exactly as before (vertical stacking). Android tablet and iPad multi-column layouts can opt in to rendering the group directly in the future. The pagination-dedup logic in `ContentListLogic.paginate()` uses a recursive `collect()` that walks through both `Section.items` and `SectionGroup.sections` so deduplication keys stay correct regardless of how sections are nested.

  **Edge-to-edge children on compact web**: the `StatChipRow` child of a section has `edgeToEdge = true`. On compact web, the section-children emission loop detects this flag and renders the child via a `Modifier.layout` escape (`escapeGridHorizontalPadding`) instead of wrapping it in `CenteredItem`. The helper re-measures the child at `constraints.maxWidth + 32dp` and places it at `x = -16dp`, which negates the parent `LazyVerticalGrid`'s `padding(horizontal = 16.dp)` so the chip carousel's `LazyRow` scrolls flush to the viewport edges. The layout still reports `constraints.maxWidth` back up so sibling items are unaffected.
- **Progressive loading**: Initial load uses `loadPokemonPage1()` which handles the two parallel API calls with progressive rendering. If the profile finishes before battles, profile sections are shown immediately with the Battles section header displaying a loading indicator in the sort toggle (`loadingSections = {"Battles"}`). If battles finishes before the profile, the unified loading spinner is maintained until the profile also completes (avoids layout jumpiness). Format change and sort toggle use the standard `fetchContent()` path with section-level loading.
- **Pages 2+**: bare `Battle` items
- **Format change**: reloads all profile sections and battles (`loadingSections = {"format_selector", "Top Teammates", "Top Items", "Top Moves", "Top Abilities", "Top Tera Types", "Top Players", "Battles"}`) since the pokemon profile endpoint accepts `format_id`
- **Lookback change**: same path as format change — re-fetches both the profile and battles. The battles `searchMatches` call is given a `timeRangeStart`/`timeRangeEnd` window derived from the lookback via `LookbackWindow.timeRangeSecondsEndingAt(nowSeconds)`, so the Battles section narrows to the same horizon as the profile stats (e.g. "24 hours" → last 24h of battles for this Pokemon). `LookbackWindow.All` returns `(null, null)` and clears the filter (full battle history). Routed through the private helper `pokemonBattlesTimeRange()` shared by all three Pokemon-mode `searchMatches` call sites (page-1 `loadPokemonPage1`, page-1 `fetchContent`, page-2+ `fetchContent`).

### Player mode
- **Page 1**: Up to 4 items —
  1. `HighlightButtons([...])` — "Top Rated Battle" + "Latest Rated Battle" cards (from player profile API, if available)
  2. `Section("Favorite Pokemon", [PokemonGrid([...])])` — 3-column grid of most-used Pokemon (from player profile API, if available)
  3. `FormatSelector` — format dropdown (rendered as a centered list item)
  4. `Section("Battles", [...])` — battle results
- **Pages 2+**: bare `Battle` items

### TopPokemon mode
- **Single page** (no pagination): Fetches top 100 Pokemon via `getFormatDetail(formatId, topPokemonCount=100, lookback=...)`.
  1. `FormatSelector` — format dropdown
  2. `LookbackSelector` — lookback-window 4-segment selector (centered, directly below the format dropdown)
  3. `SearchField("")` — text field for client-side name filtering
  4. `Pokemon` items with `usagePercent` and `rank` (1-indexed position in the API-returned list, preserved through client-side search filtering) — full list filtered by search query. Rank renders as a smaller "#N" prefix immediately before the Pokémon name in the cell.
- **Format change**: reloads from API (`loadingSections = {"format_selector", ""}`), clears search query
- **Lookback change**: same path as format change — reloads from API (lookback affects the top pokemon list itself), clears search query
- **Search filtering**: client-side via `setSearchQuery()`, filters stored Pokemon list by name (case-insensitive), no API call
- `ContentListLogic.allTopPokemonItems` exposes the unfiltered loaded list as a `StateFlow` so the desktop-web Usage layout can measure left-pane width against the longest name
- Reached via the "Usage" tab (NavigationRail on desktop web, bottom nav on mobile / Android / iOS)

#### Desktop-web Usage layout
On the `WindowSizeClass.Expanded` branch, the Usage tab is rendered by `webApp/.../ui/contentlist/UsageDesktopPage.kt` instead of the standard `ContentListPage`. Mobile web, Android, and iOS continue to use the standard `ContentListPage` rendering of TopPokemon mode (single column with type icons).

- **Two-pane master/detail**: a left column sized to the 90th-percentile Pokémon name width + avatar + rank + usage percent, and a right pane that takes the remaining width.
- **Left column**: format dropdown, search field (fills column width), then a vertically scrolling list of simplified rows `[avatar][#rank][name][usage%]` with no type icons. Width is computed once via `rememberTextMeasurer()` over `allTopPokemonItems` after the catalog loads. The name budget uses the **90th percentile** of measured name widths rather than the absolute max, so rare long form names (e.g. "Tauros-Paldea-Blaze") don't widen the column for all 100 rows; those outliers wrap to 2 lines via `maxLines = 2`. The widest format dropdown display name is also factored in. Selected row uses a `primary` border at 2× width. While the catalog is loading the entire pane shows a centered `LoadingIndicator`.
- **Right pane**: renders a nested `ContentListPage(mode = Pokemon(...))` for the currently-selected Pokemon. The first Pokemon is auto-selected once data is ready. Selecting a different Pokemon from the left list resets the right pane's nested nav stack via `onClearUsageNestedStack`.
- **Nested navigation**: `WebApp.kt` owns a `usageNestedStack: List<NavEntry>` parallel to `desktopNavStack` (Pokemon/Player drill-downs from inside the right pane). Browser back pops `usageNestedStack` first (before `desktopNavStack`), then `searchOverlayParams`. Battle detail clicks inside the right pane are handled by the nested page's own inline pane, producing a three-pane layout `[Usage list | Pokemon page | Battle detail]` when the viewport is wide enough.
- **URL mirroring**: the selected Pokemon ID is reflected in the URL as `/usage?f={formatId}&pokemon={id}`. On mobile platforms the same URL navigates directly to `/pokemon/{id}` instead of selecting it on a Usage list — see the Deep Linking section in `docs/navigation.md`.

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

## Lookback Window (Home, Pokemon & TopPokemon Modes)

A `LookbackSelector` list item renders a segmented selector adjacent to (directly below) the `FormatSelector` in Home, Pokemon, and TopPokemon modes. The full four segments are `All` (default for Pokemon/TopPokemon), `30 days`, `7 days`, `24 hours`, mapping to the `LookbackWindow` enum values `all`/`30days`/`week`/`day` (the `Week` enum case kept its API value `"week"` while its `displayName` is `"7 days"`; the `Day` case keeps its value `"day"` while its `displayName` is `"24 hours"`).

**Home** renders a restricted 3-segment set (`LookbackWindow.homeOptions` = `30 days`/`7 days`/`24 hours`) — `All` is excluded because the `/pokemon/usage` endpoint feeding the Home mover sections returns 500 for `all` — and defaults to the middle `7 days`. The option list is chosen at each platform's render site from the current mode (Compose passes `options =` to `LookbackSegmentedSelector`; SwiftUI passes `options:`). Home's lookback change uses the dedicated `reloadHomeStatSections()` path (see "Home mode" above), not the shared `selectLookback` → `reloadSections` → `fetchContent` path that Pokemon/TopPokemon use.

The selected window drives two distinct API surfaces:
1. The `lookback` query param on `getPokemonProfile` / `getFormatDetail` (server-side filter on the profile/usage stats).
2. The `timeRangeStart`/`timeRangeEnd` window on the Pokemon-mode `searchMatches` call, via `LookbackWindow.timeRangeSecondsEndingAt(nowSeconds)` — derived from `durationSeconds` on the enum (`null` for `All`, `86400` for `Day`, `7*86400` for `Week`, `30*86400` for `ThirtyDays`). The window ends at "now" and reaches back by `durationSeconds`; `All` yields `(null, null)` and disables the filter.

- **State**: `ContentListLogic._selectedLookback: MutableStateFlow<LookbackWindow>` (defaults to `LookbackWindow.All`). Exposed as `selectedLookback: StateFlow<LookbackWindow>` and mutated via `selectLookback(window)`. Seeded from `ContentListMode.Pokemon.lookback` when set (falling back to the `initialLookback` constructor param), so the user's current lookback selection on a Usage or Pokemon page carries through to the next Pokemon drill-down — the same way `formatId` flows. Wired in:
  - Android: `composeApp/.../ui/contentlist/ContentListPage.kt` click handlers derive `lookback` from the current mode (TopPokemon/Pokemon only) and pack it into `PokemonNavTarget`, which feeds `ContentListMode.Pokemon(..., lookback)` on the recursive call.
  - Web: `webApp/.../ui/contentlist/ContentListNavigation.kt::derivedLookback()` mirrors the existing `derivedFormatId()`. `ContentListMode.Pokemon` includes `lookback` in the cached-VM key so re-clicking the same pokemon with a different master lookback creates a fresh page. On desktop, `UsageDesktopPage` captures `lookbackAtSelection` at click time (mirroring `formatAtSelection`) so the right-pane detail stays decoupled from later master changes.
  - iOS: `ContentListView.swift` click handlers derive `lookback` for TopPokemon/Pokemon modes; `PokemonNavTarget.lookback` is fed as `initialLookback` on the pushed `ContentListView`.
- **On change**: same `reloadSections()` path as `selectFormat` — re-fetches page 1 with the new lookback. In TopPokemon mode, clears the search query (mirrors format-change behavior).
- **Historic-format gating**: `ContentListLogic` accepts an `isFormatHistoric: (Int) -> Boolean` ctor param (defaults to `{ false }`; wired through `ContentListViewModel` on Android/web from `formatCatalogRepository.state.value.items`, and on iOS from the `formatItems` param passed at construction). When the predicate returns `true` for the current `_selectedFormatId.value`, the item-list builders (`buildPokemonProfileSections`, the TopPokemon loader, and the `setSearchQuery` TopPokemon rebuild path) omit the `LookbackSelector` item. `selectFormat` also resets `_selectedLookback.value` to `All` before fetching when the new format is historic, so API calls never send a non-`all` lookback for historic formats. UI-level direct renderers (`UsageDesktopPage` desktop-web, shared `UsageBottomBar` mobile) also hide the segment row when historic — they take `showLookback`/`selectedFormatIsHistoric` flags from the call site.
- **Visual style**: equal-weight segments in a `Row` (Compose) / `HStack` (SwiftUI). The selected segment is outlined in the user's accent color (`MaterialTheme.colorScheme.primary` on Compose, `settingsStore.themeColor` on SwiftUI) at 2x `StandardBorderWidth`; unselected segments use `outlineVariant` / `opaqueSeparator` at 1x. This mirrors the selected-pokemon outline in `UsageDesktopPage`'s left-pane list.
- **Rendering placement**:
  - Home mode: emitted as a list item directly after `FormatSelector` in the Home `fetchContent` page-1 build. Renders the restricted 3-option set.
  - Pokemon mode: emitted as a list item directly after `FormatSelector` in `buildPokemonProfileSections()`. The selector fills the row width (via `Modifier.weight(1f)`).
  - TopPokemon mode (compact: Android, iPhone, mobile web): suppressed from the list and rendered inside `UsageBottomBar` as a second row below the `FormatDropdown` row, filling the row width.
  - TopPokemon mode (desktop web Expanded): rendered in `UsageDesktopPage` left pane directly below the `FormatDropdown` with `fillMaxWidth`.
- **Shared composable**: `LookbackSegmentedSelector` in `shared/.../ui/contentlist/ContentListComponents.kt`, taking an `options: List<LookbackWindow> = LookbackWindow.entries` param so call sites can pass `LookbackWindow.homeOptions` for Home. iOS has a SwiftUI sibling `LookbackSegmentedSelector` in `iosApp/iosApp/ContentListComponents.swift` that accepts an `accentColor: Color` param (passed `settingsStore.themeColor` by call sites) and an `options: [LookbackWindow]` param defaulting to all four — call sites pass `[.thirtyDays, .week, .day]` for Home (the Swift options are hardcoded per call site because Kotlin enum `entries`/companion lists don't bridge cleanly through SKIE). Both implementations render the segment label from `LookbackWindow.displayName`, so display-string changes (e.g. `Week` → `"7 days"`, `Day` → `"24 hours"`) propagate from a single enum edit.

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
- **Non-battle sections on expanded** use `SectionContentAlignedHeader`, a `SubcomposeLayout`-based composable that emits one combined full-span grid item containing both the section header and all its children. Content is composed and measured first with a loose upper bound (the grid-box inner width); the header is then composed at exactly the measured content width (`minWidth = maxWidth = contentWidth`) so a trailing action (the sort toggle) lands at the content's actual right edge — which can extend past the cell pack via `ResponsivePokemonGridCard`'s `Modifier.layout` escape. The item always reports the grid's cell-pack width back up (`layout(constraints.maxWidth, ...)`), so `LazyVerticalGrid` places it at x=0 of the content area — the same left edge as the Battles section header and the leftmost battle card — regardless of whether content is narrower or wider than the cell pack. Placeables are placed at (0, y) and may draw beyond the reported box into the grid's unused trailing gutter. A vertical gap of `ContentListItemSpacing` separates header from content, matching the gap the grid's vertical arrangement provides between the Battles header and its cards.
- The dispatch for the two emission strategies hinges on a single declarative property `ContentListItem.requiresIndividualGridCells` (default `false`, overridden on `Battle`). No per-composable content-type checks inside the section branch. Compact window size class always takes the split-emission path (header + children as separate items) with the legacy `CenteredItem` 900dp cap.
- **Battle cards** are emitted as individual grid items (one per card). Column count is derived from the grid's available width and the dynamically-sized `battleCardCellWidth`. Cards are centered (`Arrangement.spacedBy(BATTLE_GRID_SPACING, Alignment.CenterHorizontally)`) so the cell pack sits symmetrically within the grid box. `animateItem(placementSpec = GRID_ITEM_PLACEMENT_SPEC)` smooths the x/y reflow when the detail pane's open/close changes the column count.
- **Animated reflow**: All grid items use `Modifier.animateItem(placementSpec = GRID_ITEM_PLACEMENT_SPEC)` with a `tween(DETAIL_PANE_ANIM_DURATION_MS, FastOutSlowInEasing)` placement spec, so items smoothly slide to their new grid positions when the detail pane's width changes the layout. Non-battle items get this automatically via the `animatedItem()` helper in `ContentListContent.kt`; battle cards apply it directly on their modifier. The spec constant is declared in `ContentListLayout.kt` and can be tuned independently.

**Snap-to-narrow grid width**: When `selectedBattleId` becomes non-null, the grid's outer Box modifier swaps from `Modifier.weight(1f)` to `Modifier.width(gridWidthWhenPaneOpen)` in the same composition. The grid remeasures with the new narrow width in a single frame — typically into a 1-column layout. This is critical because `LazyVerticalGrid.scrollToItem(N)` in a multi-column grid gets clobbered back to the row-start by the next measure pass (`LazyGridScrollPosition.updateFromMeasureResult`). By ensuring the grid is already in its final (usually 1-col) layout before `scrollToItem` runs, the target index sticks.

The pane width is sized dynamically via `BoxWithConstraints`: `panePostWidth = (maxWidth - battleCardCellWidth - 1.dp).coerceIn(0.dp, DETAIL_PANEL_MAX_WIDTH)`. On wide viewports the pane gets its full 960dp; on narrower viewports it shrinks so the grid can hold a full battle card at the currently-derived cell width. `grid + 1dp divider + pane = maxWidth` exactly, so there's no unused Row space. `computeBattleCardCellWidth` caps card growth so the pane retains at least `DETAIL_PANEL_PREFERRED_MIN_WIDTH` (928dp) when the viewport allows it — keeping 3-pokemon-per-row in each team card. The cap floor is pinned at the default width (620dp) so cards never drop *below* the default just to buy pane width, avoiding a visible card-width jolt when resizing through the threshold where the cap first becomes relevant.

**Pane animation**: The pane uses `slideInHorizontally`/`slideOutHorizontally` (not `expandHorizontally`/`shrinkHorizontally`) so the pane's layout bounds snap to their final width the moment `visibleState` becomes true, and the content translates in from off-screen-right. This avoids an artifact where the pane's growing right edge would leave a visible sliver of the outer `surface` background to its right.

**Scroll restoration**: On every pane open or switch, the grid calls `scrollToItem(index)` (first open) or `animateScrollToItem(index)` (switch while open) to bring the selected battle into view. On close, the grid animates back to `firstVisibleItemIndex` / `firstVisibleItemScrollOffset` — wherever the user was last looking, whether from an auto-scroll on battle selection or manual scrolling while the pane was open. The second argument to `scrollToItem` is `scrollOffsetPx = BATTLE_GRID_SPACING` converted to pixels via `LocalDensity.current`, providing a small top margin.

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
    settingsRepository: SettingsRepository? = null  // for preferred-format setting
)
```

**Public API** (all non-suspend except `watchForStaleness` — they launch internally via the injected scope):
- `initialize()` — one-shot init, routes to correct startup path based on mode
- `loadContent()` / `refresh()` — fetch page 1 (loading vs refreshing indicator)
- `paginate()` — next page with guards (isPaginating, canPaginate, loadingSections)
- `selectFormat(formatId)` — format change + section reload
- `toggleSortOrder()` — sort toggle + section reload
- `updateSearchParams(params)` — reset state and reload with new search params
- `suspend watchForStaleness()` — loops until cancelled. See "Stale-Data Background Refresh" below.

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

## Stale-Data Background Refresh

Web only. The ViewModelStore caches `ContentListViewModel` instances across tab switches, so cached state renders instantly when the user returns to a page — but that cache can grow stale if the user revisits a page after the server has ingested new data. `ContentListLogic.watchForStaleness()` is a suspend loop that wakes exactly once per server ingestion boundary (`:05` and `:35` of the hour — the server ingests at `:00`/`:30` and finishes within 5 minutes) and silently merges a fresh page 1 into the cached items list.

**Invocation** — `webApp/.../ui/contentlist/ContentListPage.kt` calls `LaunchedEffect(viewModel) { viewModel.watchForStaleness() }` for modes whose data actually changes on the `:00`/`:30` cadence: `Home`, `Search`, `Pokemon`, `Player`. `Favorites` and `TopPokemon` are skipped. The `LaunchedEffect` is tied to composition, so polling stops when the page leaves the screen.

**Cycle** — `watchForStaleness` reads `lastLoadedAtMs` (stamped after every successful page-1 load), computes `nextStaleAtMs(loadedAt) - currentTimeMillis()` via the pure helper in `ContentListLogic`'s companion object, and `delay()`s exactly that long. One wake per boundary; `delay()` doesn't burn CPU.

**Soft merge** — `softRefresh()` is internal-private. Instead of the hard-reset semantics of `refresh()` (which wipes `currentPage`/`canPaginate` and replaces the items list), softRefresh:
1. Fetches page 1.
2. Collects keys recursively from the fresh items (`collectListKeys()`).
3. Takes a tail of existing items whose `listKey` isn't in the fresh set — preserves any flat page-2+ entries the user has already paginated through.
4. Replaces items with `freshItems + tail`. The LazyGrid keys items by `listKey` so unchanged items stay put; only the genuine diff animates.

The user's scroll position, pagination state, and any in-flight UI overlays are untouched.

**Concurrency guards** — softRefresh skips when `isLoading`, `isRefreshing`, or `loadingSections.isNotEmpty()` (matching `paginate()`'s guard). This prevents races with initial load, manual refresh, and `selectFormat`/`toggleSortOrder`.

**Failure backoff** — `lastLoadedAtMs` is stamped in both the success and failure paths of softRefresh. Stamping on failure ensures the next loop iteration computes `msUntilStale` from "now" rather than from the original load time, deferring the next attempt to the next `:05`/`:35` instead of looping tight while the API is failing.

The pure `nextStaleAtMs(loadedAtMs: Long)` helper is unit-tested in `ContentListLogicTest.kt` along with the failure-backoff invariant.

## Home Mode Special Behavior

Home mode waits for app config before loading (needs default format ID):
1. If config already available, syncs `_selectedFormatId` and loads immediately
2. Otherwise, shows loading state and waits for `appConfigRepository.config` to emit non-null, then syncs format ID
3. Fetches format detail (top 10 Pokemon) and Pokemon usage movers (`getPokemonUsage`) on the window chosen in the Home `LookbackSelector` (defaults to `Week`), plus battles (via `getBestPreviousDay` cached endpoint, lookback-independent) in parallel using the selected format. Page 2+ uses `searchMatches` with last-24h time range and rating sort.
4. Format selector reloads all sections; the lookback selector (`30 days`/`7 days`/`24 hours`) reloads only the three stat sections via `reloadHomeStatSections()`

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
