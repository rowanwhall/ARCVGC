# Search / Filter Architecture

## Shared layer
- **Domain models**: `PokemonListItem`, `SearchFilterSlot` (ID-based filter state), `Format`, `SearchParams` (bundles filters + formatId + minimumRating + orderBy)
- **DTOs**: `PokemonListResponseDto`, `ItemListResponseDto`, `TeraTypeListResponseDto`, `FormatListResponseDto`, `SearchRequestDto` — all reuse existing `PaginationDto`
- **Network mappers**: `PokemonListMapper.kt`, `ItemListMapper.kt`, `FormatListMapper.kt` — all `Dto.toDomain()` extension functions
- **UI models**: `PokemonPickerUiModel`, `SearchFilterSlotUiModel`, `FormatUiModel` (id + displayName), `ItemUiModel`, `TeraTypeUiModel`
- **UI mappers**: `PokemonPickerUiMapper`, `ItemUiMapper`, `TeraTypeUiMapper`, `FormatUiMapper` — all singleton objects with `map()` / `mapList()`
- **Shared utilities**:
  - `SearchRequestMapper.kt` — `buildSearchRequest()` converts `SearchFilterSlot` list + params into `SearchRequestDto`
  - `CatalogLoader.kt` — `loadFullCatalog()` generic pagination loop + typed loaders (`loadPokemonCatalog()`, `loadItemCatalog()`, `loadTeraTypeCatalog()`, `loadFormatCatalog()`) for iOS interop
  - `TimeFormatter.kt` — `formatUploadTime()` shared by `BattleCardUiMapper` and `BattleDetailUiMapper`
- **SearchStateReducer** (`shared/.../ui/search/SearchStateReducer.kt`): Stateless `object` with pure functions `(SearchUiState, input) → SearchUiState`. Handles all search filter state mutations — `addPokemon`, `removePokemon`, `setItem`, `setTeraType`, `setFormat`, `setDefaultFormat`, `setMinRating`, `setMaxRating`, `setUnratedOnly`, `setTimeRange`, `setPlayerName`, `setOrderBy`. Each platform's SearchViewModel delegates mutations to this reducer. `setDefaultFormat` encapsulates the "only set format if not already set" guard used during config init. `setUnratedOnly(true)` clears ratings and switches sort order from "rating" to "time".
- **SearchUiState** (`shared/.../ui/model/SearchUiState.kt`): Shared data class used by all platforms — `filterSlots`, `selectedFormat`, `selectedMinRating`, `selectedMaxRating`, `unratedOnly`, `selectedOrderBy`, `timeRangeStart`, `timeRangeEnd`, `playerName`, computed `canAddMore`
- **Catalog repos** (`shared/.../data/`): `CatalogState<T>` data class + 4 singleton repo classes (`PokemonCatalogRepository`, `ItemCatalogRepository`, `TeraTypeCatalogRepository`, `FormatCatalogRepository`). Each checks `CatalogCache` on init (TTL-based), falls back to `loadFullCatalog()` on cache miss, using `CoroutineScope(SupervisorJob() + Dispatchers.Default)`. Exposes `StateFlow<CatalogState<T>>` and `reload()` (resets state + re-fetches). Used directly by Android (via Hilt wrappers) and Web; iOS uses a Swift-native `CatalogStore` instead (avoids Kotlin generic bridging issues).
- **FormatSorter** (`shared/.../ui/model/FormatSorter.kt`): Pure utility that sorts formats descending by ID and promotes the default format to the front. Called at the UI layer (not the catalog repo) so sorting uses the live default format ID from config, avoiding race conditions between config and catalog loading. Each platform applies `FormatSorter.sorted()` when passing formats to pickers/dropdowns.
- **ContentListMode**: Sealed class — `Home`, `Favorites(contentType: FavoriteContentType)`, `Search(params: SearchParams)`, `Pokemon(pokemonId, name, imageUrl, typeImageUrl1, typeImageUrl2, formatId: Int? = null)`, or `Player(playerId, name)`. The optional `formatId` on `Pokemon` is threaded from battle detail when navigating to a Pokemon's battles; when null, the ViewModel defaults to the app config's default format.

## Android
- **Catalog repos** (4 Hilt-injectable interfaces + `Impl` classes): Thin wrappers that delegate to shared catalog repos, expose `StateFlow<CatalogState<T>>` and `reload()` from shared `CatalogState` (`com.arcvgc.app.data.CatalogState`)
- **Eager init**: `CatalogInitializer` injected into `ShowdownApplication` forces all 4 catalog repos to load at app startup. Also observes `AppConfigRepository.catalogVersionChanged` to trigger catalog reloads.
- **SearchViewModel**: `@HiltViewModel` injecting all 4 catalog repos + `AppConfigRepository`; delegates all state mutations to shared `SearchStateReducer`. Pre-selects default format from config via `SearchStateReducer.setDefaultFormat()`.
- **Compose UI**: `SearchFilterCard` (auto-sizing text via `AutoSizeText` composable), `PokemonPickerSheet`, `ItemPickerSheet`, `TeraTypePickerSheet`, `FormatPickerSheet`, `MinRatingPickerSheet`, `SortOrderPickerSheet` (all `ModalBottomSheet`). Format picker sizes to content (no `skipPartiallyExpanded`); Pokemon/Item/Tera pickers expand full-screen.
- **ContentListViewModel keying**: `hiltViewModel(key = ...)` uses `mode.params.toString()` for search mode so identical searches reuse cached results (pull-to-refresh available), while different params get a fresh ViewModel. Favorites key includes `contentType.name`: `"content_list_favorites_${mode.contentType.name}"`
- **Button styling**: Search option buttons use `surfaceVariant` background / `onSurfaceVariant` text (same as move chips), not Material `Button`

## iOS
- **CatalogStore** (`@MainActor ObservableObject`): Loads all 4 catalogs in parallel `Task`s on init via shared typed loaders (`CatalogLoaderKt.loadPokemonCatalog()`, etc.). Created eagerly in `DependencyContainer`, survives full app lifecycle. Exposes `@Published` loading/items/error for each catalog. `reload()` resets all state and re-launches all 4 load tasks (used by cache invalidation in Settings and config-driven catalog version changes).
- **SearchViewModel**: `@MainActor ObservableObject` using shared `SearchUiState` (Kotlin data class) as state type. Delegates all mutations to `SearchStateReducer.shared`. Convenience computed properties (`minRating`, `maxRating`, `timeStart`, `timeEnd`) bridge Kotlin types back to Swift-native types for the view.
- **SwiftUI**: `SearchFilterCard` (`.minimumScaleFactor(0.6)` for auto-sizing), picker sheets presented via `SearchSheet` enum + `.sheet(item:)`. Format picker uses `.presentationDetents([.medium])` to avoid full-screen for small content. `SearchView` takes `catalogStore: CatalogStore` and `appConfigStore: AppConfigStore`, pre-selects default format from config.
- **Button styling**: Search option buttons use `systemGray5` background (same as move chips), not `.borderedProminent`

## Web
- **Catalog repos**: 4 shared catalog repos added to `DependencyContainer` (lazy singletons), eagerly initialized in `WebApp()` via `remember {}` block at app startup
- **SearchViewModel**: Plain `ViewModel()` constructor-injecting all 4 catalog repos + optional `AppConfigRepository`. Delegates all state mutations to shared `SearchStateReducer`. Pre-selects default format from config via `SearchStateReducer.setDefaultFormat()`.
- **Compose UI**: `SearchFilterCard` (same as Android), `PickerDialogs.kt` consolidates all 6 pickers as `Dialog` composables (search field at TOP, web convention). `PokemonPickerDialog`, `ItemPickerDialog`, `TeraTypePickerDialog`, `FormatPickerDialog`, `MinRatingPickerDialog`, `SortOrderPickerDialog`

## Sort Toggle & Section Loading

### Sort toggle
The "Battles" section header in Search, Pokemon, and Player modes includes a sort toggle button (time <-> rating). Sort order is **ViewModel-owned state** (`_sortOrder` / `sortOrder` StateFlow), not stored in `SearchParams`. On toggle, the ViewModel re-fetches page 1 content with the new sort order while keeping existing items visible behind a per-section loading indicator.

### Per-section loading (`loadingSections`)
`ContentListUiState` / `ContentListState` includes `loadingSections: Set<String>`. When a section header string (e.g., `"Battles"`) is in this set, the UI renders a progress indicator in place of that section's child items while keeping the section header (and sort toggle) visible. Other sections remain unaffected.

### Pagination guard (critical invariant)
**`paginate()` must check `loadingSections.isNotEmpty()` and refuse to paginate while any section is loading.** Without this guard, replacing section content with a single loading indicator shortens the list, causing the "near end of list" pagination trigger to fire. This results in:
1. Multiple simultaneous loading indicators (section loading + pagination loading)
2. A race between the sort-toggle fetch (page 1) and the pagination fetch (page 2), which can flash mixed old/new content

Similarly, `toggleSortOrder()` sets `canPaginate = false` during the fetch to provide a second layer of defense. The fetch result sets the correct `canPaginate` value on completion.

### Implementation locations
- **Android**: `ContentListViewModel.kt` (`_sortOrder`, `toggleSortOrder()`, pagination guard), `ContentListUiState.kt` (`loadingSections`), `ContentListPage.kt` (`SectionHeader`, `SortToggleButton`, section loading rendering)
- **iOS**: `ContentListViewModel.swift` (`sortOrder`, `toggleSortOrder()`, pagination guard), `ContentListState.swift` (`loadingSections`), `ContentListView.swift` (`SectionHeaderView`, `SortToggleButton`, section loading rendering)
- **Web**: same as Android, in `webApp/.../ui/contentlist/`
