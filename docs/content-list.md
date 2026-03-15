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
| `Player(playerId, playerName)` | — | `PlayerHero` | Yes | Yes |

### ContentListHeaderUiModel

Sealed class with six variants controlling what renders above the list:

- **`None`** — no header
- **`HomeHero`** — "ARC" title + "Today's Top Battles" subtitle
- **`FavoritesHero`** — heart icon + "Favorites" subtitle
- **`SearchFilters`** — flow row of removable filter chips (Pokemon with items/tera, format, rating range, unrated, player name, date range). Each chip type has `canRemove*()` / `remove*()` methods on `SearchParams` controlling removability.
- **`PokemonHero`** — large Pokemon avatar (176dp circle / 252dp sprite) + name + type icons + format dropdown
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
| `PokemonGrid(pokemon)` | 3-column grid of Pokemon (player profile "Favorite Pokemon") | `"pokemon_grid"` |

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
- **Page 1**: `Section("Battles", [...])` wrapping battle results
- **Pages 2+**: bare `Battle` items

### Player mode
- **Page 1**: Up to 3 items —
  1. `HighlightButtons([...])` — "Top Rated Battle" + "Latest Rated Battle" cards (from player profile API, if available)
  2. `Section("Favorite Pokemon", [PokemonGrid([...])])` — 3-column grid of most-used Pokemon (from player profile API, if available)
  3. `Section("Battles", [...])` — battle results
- **Pages 2+**: bare `Battle` items

## Section Loading & Sort Toggle

Documented in detail in [`docs/search.md`](search.md) under "Sort Toggle & Section Loading". Key points:

- Sort toggle appears on the "Battles" `SectionHeader` in Search, Pokemon, and Player modes
- Toggling sets `loadingSections = setOf("Battles")`, re-fetches page 1 with new sort order
- Section children render at 50% opacity (Android/Web) or with a spinner overlay (iOS) while loading
- **Pagination guard**: `paginate()` refuses to run when `loadingSections.isNotEmpty()` — prevents race conditions between sort/format fetches and pagination

## Format Selection (Pokemon Mode)

The Pokemon hero header includes a format dropdown when format catalog data is available:

- **Default format**: inherited from `formatId` parameter (threaded from battle detail) or falls back to app config's default format
- **On change**: sets `loadingSections = setOf("Battles")`, re-fetches page 1 with new format
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

**Format threading**: When navigating to a Pokemon from battle detail, the battle's `formatId` is injected at the boundary (`BattleDetailSheetWrapper` on Android, similar on iOS/Web) by wrapping the 4-param `onPokemonClick` into a 5-param version that appends the format.

## Toolbar & Favorite Buttons

Pokemon and Player modes show a toolbar with back button + favorite heart:
- **Pokemon mode**: heart toggles `togglePokemonFavorite(pokemonId)`
- **Player mode**: heart toggles `togglePlayerFavorite(playerName)`
- Toolbar floats over content with a gradient fade background (`surfaceContainer` at 70% → 0% alpha)

## ViewModel Keying (Per-Platform)

Each platform creates distinct ViewModel instances per mode to avoid state leakage:

- **Android**: `hiltViewModel(key = ...)` — `"content_list_home"`, `"content_list_favorites_{contentType}"`, `"content_list_search_{params}"`, `"content_list_pokemon_{pokemonId}"`, `"content_list_player_{playerId}"`
- **iOS**: new `ContentListViewModel()` instance per `ContentListView` appearance
- **Web**: `remember(mode.toString()) { ContentListViewModel(deps...) }` via `DependencyContainer`

## ContentListUiState

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
- **Empty** (loaded, no items): `EmptyView` ("No battles found" or similar)

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
| Shared models | `shared/.../ui/model/ContentListMode.kt`, `ContentListItem.kt`, `ContentListHeaderUiModel.kt` |
| Shared mapper | `shared/.../ui/mapper/ContentListItemMapper.kt` |
| Android | `composeApp/.../ui/contentlist/ContentListPage.kt`, `ContentListViewModel.kt`, `ContentListUiState.kt` |
| iOS | `iosApp/iosApp/ContentListView.swift`, `ContentListViewModel.swift`, `ContentListState.swift` |
| Web | `webApp/.../ui/contentlist/ContentListPage.kt`, `ContentListViewModel.kt`, `ContentListUiState.kt` |
