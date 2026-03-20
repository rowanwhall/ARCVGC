# Content List Architecture

`ContentListPage` (Android/Web) / `ContentListView` (iOS) is the app's primary reusable screen. It renders paginated, heterogeneous lists of battles, Pokemon, and players across five distinct modes, each with its own header, data-fetching logic, and content structure.

## Shared Models

All shared models live in `shared/.../ui/model/`.

### ContentListMode

Sealed class defining the five modes. Each mode maps to a header via `toHeaderUiModel()`.

| Mode | Parameters | Header | Sort toggle | Pagination |
|---|---|---|---|---|
| `Home` | — | `HomeHero` | No | Yes |
| `Favorites(contentType)` | `FavoriteContentType` enum | `FavoritesHero` | No | No (loads all at once) |
| `Search(params)` | `SearchParams` | `SearchFilters` | Yes | Yes |
| `Pokemon(pokemonId, name, imageUrl, typeImageUrl1, typeImageUrl2, formatId?)` | Optional `formatId` threaded from battle detail | `PokemonHero` | Yes | Yes |
| `Player(playerId, playerName, formatId?)` | Optional `formatId` threaded from battle detail | `PlayerHero` | Yes | Yes |

### ContentListHeaderUiModel

Sealed class with six variants controlling what renders above the list:

- **`None`** — no header
- **`HomeHero`** — "ARC" title + "Today's Top Battles" subtitle
- **`FavoritesHero`** — heart icon + "Favorites" subtitle
- **`SearchFilters`** — flow row of removable filter chips (Pokemon with items/tera, format, rating range, unrated, player name, date range). Each chip type has `canRemove*()` / `remove*()` methods on `SearchParams` controlling removability.
- **`PokemonHero`** — large Pokemon avatar (158dp circle / 227dp sprite) + name (headlineMedium/20pt) + type icons (24dp)
- **`PlayerHero`** — player name in rounded pill background

### ContentListItem

Sealed class for heterogeneous list rendering. Each variant has a `listKey: String` for stable LazyList/List keys.

| Variant | Usage | listKey |
|---|---|---|
| `Battle(uiModel)` | Battle card | `"battle_{id}"` |
| `Pokemon(id, name, imageUrl, types)` | Pokemon row (favorites, search pinned) | `"pokemon_{id}"` |
| `Player(id, name)` | Player row (favorites, search pinned) | `"player_{id}"` |
| `Section(header, items)` | Grouping container with title | `"section_{header}"` |
| `HighlightButtons(buttons)` | Player profile highlight cards (Top Rated / Latest Rated) | `"highlight_buttons"` |
| `PokemonGrid(pokemon)` | 3-column grid of Pokemon (player profile "Favorite Pokemon", pokemon profile "Top Teammates") | `"pokemon_grid"` |
| `StatChipRow(chips)` | Horizontal scrolling row of chips with name+percent and optional image (mobile), FlowRow (desktop web). Used for Top Abilities, Items, Moves, Tera Types. | `"stat_chip_row"` |
| `FormatSelector` | Format dropdown rendered as a list item (Pokemon, Player modes) | `"format_selector"` |

`ContentListItemMapper` (in `shared/.../ui/mapper/`) provides factory methods: `fromBattles()`, `fromPokemon()`, `fromPlayers()`, `fromPokemonCatalog()`.

## Page 1 vs Page 2+ Content Structure

This is a key behavioral detail: several modes compose a richer page 1 with sections, while pages 2+ append bare battle items.

### Home mode
- All pages: flat list of `Battle` items (no sections)

### Favorites mode
- Single page (no pagination): flat list of `Battle`, `Pokemon`, or `Player` items depending on `contentType`

### Search mode
- **Page 1**: Up to 3 sections —
  1. `Section("Pokemon", [...])` — pinned Pokemon from search filters (resolved from catalog)
  2. `Section("Players", [...])` — pinned player (if `playerName` filter set, resolved via API)
  3. `Section("Battles", [...])` — battle results
- **Pages 2+**: bare `Battle` items (appended to flat list, no wrapping section)

### Pokemon mode
- **Page 1**: Up to 3 items — profile + battles fetched in parallel via `getPokemonProfile(id, formatId)` + `searchMatches(...)`. Profile errors are silently swallowed; page still shows battles.
  1. `FormatSelector` — format dropdown (rendered as a centered list item)
  2. `Section("Top Teammates", [PokemonGrid([...])])` — 3-column grid of top teammates with usage %. Only shown if profile succeeds and has teammates.
  3. `Section("Top Abilities", [StatChipRow([...])])` — chip carousel of abilities with usage %. Name + percent only.
  4. `Section("Top Items", [StatChipRow([...])])` — chip carousel of items with image, name, and usage %.
  5. `Section("Top Moves", [StatChipRow([...])])` — chip carousel of moves with usage %. Name + percent only.
  6. `Section("Top Tera Types", [StatChipRow([...])])` — chip carousel of tera types with image, name, and usage %.
  7. `Section("Battles", [...])` — battle results
  All profile sections are from pokemon profile API (count / matchCount), only shown if data is non-empty.
- **Pages 2+**: bare `Battle` items
- **Format change**: reloads all profile sections and battles (`loadingSections = {"Top Teammates", "Top Abilities", "Top Items", "Top Moves", "Top Tera Types", "Battles"}`) since the pokemon profile endpoint accepts `format_id`

### Player mode
- **Page 1**: Up to 4 items —
  1. `HighlightButtons([...])` — "Top Rated Battle" + "Latest Rated Battle" cards (from player profile API, if available)
  2. `Section("Favorite Pokemon", [PokemonGrid([...])])` — 3-column grid of most-used Pokemon (from player profile API, if available)
  3. `FormatSelector` — format dropdown (rendered as a centered list item)
  4. `Section("Battles", [...])` — battle results
- **Pages 2+**: bare `Battle` items

## Section Loading & Sort Toggle

Documented in detail in [`docs/search.md`](search.md) under "Sort Toggle & Section Loading". Key points:

- Sort toggle appears on the "Battles" `SectionHeader` in Search, Pokemon, and Player modes
- Toggling sets `loadingSections = setOf("Battles")`, re-fetches page 1 with new sort order
- Section children render at 50% opacity (Android/Web) or with a spinner overlay (iOS) while loading
- **Pagination guard**: `paginate()` refuses to run when `loadingSections.isNotEmpty()` — prevents race conditions between sort/format fetches and pagination

## Format Selection (Pokemon & Player Modes)

A `FormatSelector` list item renders a format dropdown in both Pokemon and Player modes. It appears as a centered dropdown between the header/profile content and the Battles section.

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

## Battle Detail Sheet & Navigation Suppression

Each `ContentListPage`/`ContentListView` manages three local state variables:
- `selectedBattleId: Int?` — which battle detail sheet to show
- `pokemonNavTarget` — Pokemon drill-down destination
- `playerNavTarget` — Player drill-down destination

When a Pokemon or Player is tapped from within a battle detail sheet:
1. The nav target is set
2. The detail sheet is **suppressed** (not dismissed) — `selectedBattleId` stays set
3. A child `ContentListPage`/`ContentListView` renders on top with the appropriate mode
4. On back, the nav target clears and the detail sheet reappears with the battle still loaded

This is recursive — each child instance has its own independent state, creating a natural navigation stack.

**Format threading**: When navigating to a Pokemon or Player from battle detail, the battle's `formatId` is injected at the boundary (`BattleDetailSheetWrapper` on Android, `BattleDetailSheet` on iOS, `BattleDetailPanel` on Web) by wrapping the click callbacks to append the format. When navigating from search results, the search's format is passed. When navigating from another Pokemon/Player page, the current page's `selectedFormatId` is passed.

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
    pokemonCatalogItems: List<PokemonPickerUiModel> = emptyList()
)
```

**Public API** (all non-suspend — they launch internally via the injected scope):
- `initialize()` — one-shot init, routes to correct startup path based on mode
- `loadContent()` / `refresh()` — fetch page 1 (loading vs refreshing indicator)
- `paginate()` — next page with guards (isPaginating, canPaginate, loadingSections)
- `selectFormat(formatId)` — format change + section reload
- `toggleSortOrder()` — sort toggle + section reload
- `updateSearchParams(params)` — reset state and reload with new search params

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
- **Error** (with no items): `ErrorView` / `ErrorBanner` with retry button
- **Empty** (loaded, no items): `EmptyView` ("There's nothing here")
- **Empty section** (section with 0 items, not loading): inline `EmptyView` / `BattleEmptyView` below the section header. Used in Pokemon and Player modes when the Battles section is always present (even with no results) so the section header + sort toggle remain visible.

## Pagination

- Trigger: when user scrolls within 5 items of the list end (`PAGINATION_THRESHOLD = 5`)
- Guards: `!isPaginating && canPaginate && loadingSections.isEmpty()`
- New items appended with `distinctBy { listKey }` to prevent duplicates
- `canPaginate` set from `pagination.page < pagination.totalPages`

## Home Mode Special Behavior

Home mode waits for app config before loading (needs default format ID):
1. If config already available, loads immediately
2. Otherwise, shows loading state and waits for `appConfigRepository.config` to emit non-null
3. Fetches battles sorted by rating from the last 24 hours using the config's default format

## Key File Locations

| Platform | Files |
|---|---|
| Shared logic | `shared/.../ui/contentlist/ContentListLogic.kt`, `ContentListUiState.kt` |
| Shared models | `shared/.../ui/model/ContentListMode.kt`, `ContentListItem.kt`, `ContentListHeaderUiModel.kt` |
| Shared mapper | `shared/.../ui/mapper/ContentListItemMapper.kt` |
| Shared util | `shared/.../util/CoroutineScopeFactory.kt` |
| Shared tests | `shared/src/commonTest/.../ui/contentlist/ContentListLogicTest.kt` |
| Android | `composeApp/.../ui/contentlist/ContentListPage.kt`, `ContentListViewModel.kt` |
| iOS | `iosApp/iosApp/ContentListView.swift`, `ContentListViewModel.swift` |
| Web | `webApp/.../ui/contentlist/ContentListPage.kt`, `ContentListViewModel.kt` |
