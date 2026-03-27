# Search / Filter Architecture

## Shared layer
- **Domain models**: `PokemonListItem`, `SearchFilterSlot` (ID-based filter state), `Format`, `SearchParams` (bundles filters + formatId + minimumRating + orderBy)
- **DTOs**: `PokemonListResponseDto`, `ItemListResponseDto`, `TeraTypeListResponseDto`, `AbilityListResponseDto`, `FormatListResponseDto`, `SearchRequestDto` — all reuse existing `PaginationDto`
- **Network mappers**: `PokemonListMapper.kt`, `ItemListMapper.kt`, `AbilityListMapper.kt`, `FormatListMapper.kt` — all `Dto.toDomain()` extension functions
- **UI models**: `PokemonPickerUiModel`, `SearchFilterSlotUiModel`, `FormatUiModel` (id + displayName), `ItemUiModel`, `TeraTypeUiModel`, `AbilityUiModel` (id + name, no imageUrl)
- **UI mappers**: `PokemonPickerUiMapper`, `ItemUiMapper`, `TeraTypeUiMapper`, `AbilityUiMapper`, `FormatUiMapper` — all singleton objects with `map()` / `mapList()`
- **Shared utilities**:
  - `SearchRequestMapper.kt` — `buildSearchRequest()` converts team1 (`filters`) and team2 (`team2Filters`) into `SearchTeamDto` objects on the `SearchRequestDto`. The legacy top-level `pokemon` field is deprecated and sent as an empty list
  - `CatalogLoader.kt` — `loadFullCatalog()` generic pagination loop + typed loaders (`loadPokemonCatalog()`, `loadItemCatalog()`, `loadTeraTypeCatalog()`, `loadAbilityCatalog()`, `loadFormatCatalog()`) for iOS interop
  - `AbilityInitials.kt` — `abilityInitials()` helper for rendering ability initials in filter badges/circles (first letter of first 1-2 words, ignoring parentheticals)
  - `TimeFormatter.kt` — `formatUploadTime()` shared by `BattleCardUiMapper` and `BattleDetailUiMapper`
- **SearchLogic** (`shared/.../ui/search/SearchLogic.kt`): Shared class that owns `MutableStateFlow<SearchUiState>` and all mutation methods. Accepts optional `CoroutineScope` + `StateFlow<AppConfig?>` for automatic default format observation. Platform ViewModels delegate to this class (similar pattern to `ContentListLogic`). Android/Web pass `viewModelScope` + config flow; iOS passes a `CoroutineScopeFactory` scope + `AppConfigRepository.config`.
- **SearchStateReducer** (`shared/.../ui/search/SearchStateReducer.kt`): Stateless `object` with pure functions `(SearchUiState, input) → SearchUiState`. Used internally by `SearchLogic`. Handles all search filter state mutations — `addPokemon`, `removePokemon`, `setItem`, `setTeraType`, `setAbility` (team1) + `addTeam2Pokemon`, `removeTeam2Pokemon`, `setTeam2Item`, `setTeam2TeraType`, `setTeam2Ability` (team2) + `setFormat`, `setDefaultFormat`, `setMinRating`, `setMaxRating`, `setUnratedOnly`, `setTimeRange`, `setPlayerName`, `setOrderBy`. `removePokemon` promotes team2 to team1 when team1 becomes empty. `setDefaultFormat` encapsulates the "only set format if not already set" guard used during config init. `setUnratedOnly(true)` clears ratings and switches sort order from "rating" to "time".
- **SearchUiState** (`shared/.../ui/model/SearchUiState.kt`): Shared data class used by all platforms — `filterSlots` (team1), `team2FilterSlots`, `selectedFormat`, `selectedMinRating`, `selectedMaxRating`, `unratedOnly`, `selectedOrderBy`, `timeRangeStart`, `timeRangeEnd`, `playerName`, computed `canAddMoreTeam1`, `canAddMoreTeam2`, `hasTeam2`
- **SearchParams** (`shared/.../domain/model/SearchParams.kt`): `filters` (team1) + `team2Filters` + other search parameters. `removePokemonAt()` promotes team2 to team1 when team1 becomes empty (mirrors reducer logic)
- **Catalog repos** (`shared/.../data/`): `CatalogState<T>` data class + 5 singleton repo classes (`PokemonCatalogRepository`, `ItemCatalogRepository`, `TeraTypeCatalogRepository`, `AbilityCatalogRepository`, `FormatCatalogRepository`). Each checks `CatalogCache` on init (TTL-based), falls back to `loadFullCatalog()` on cache miss, using `CoroutineScope(SupervisorJob() + Dispatchers.Default)`. Exposes `StateFlow<CatalogState<T>>` and `reload()` (resets state + re-fetches). Used directly by Android (via Hilt wrappers) and Web; iOS uses a Swift-native `CatalogStore` instead (avoids Kotlin generic bridging issues).
- **FormatSorter** (`shared/.../ui/model/FormatSorter.kt`): Pure utility that sorts formats descending by ID and promotes the default format to the front. Called at the UI layer (not the catalog repo) so sorting uses the live default format ID from config, avoiding race conditions between config and catalog loading. Each platform applies `FormatSorter.sorted()` when passing formats to pickers/dropdowns.
- **ContentListMode**: Sealed class — `Home`, `Favorites(contentType: FavoriteContentType)`, `Search(params: SearchParams)`, `Pokemon(pokemonId, name, imageUrl, typeImageUrl1, typeImageUrl2, formatId: Int? = null)`, `Player(playerId, playerName, formatId: Int? = null)`, or `TopPokemon(formatId: Int? = null)`. The optional `formatId` on `Pokemon`/`Player`/`TopPokemon` is threaded from the calling context; when null, the ViewModel defaults to the app config's default format.

## Android
- **Catalog repos** (5 Hilt-injectable interfaces + `Impl` classes): Thin wrappers that delegate to shared catalog repos, expose `StateFlow<CatalogState<T>>` and `reload()` from shared `CatalogState` (`com.arcvgc.app.data.CatalogState`)
- **Eager init**: `CatalogInitializer` injected into `ShowdownApplication` forces all 5 catalog repos to load at app startup. Also observes `AppConfigRepository.catalogVersionChanged` to trigger catalog reloads.
- **SearchViewModel**: `@HiltViewModel` injecting all 5 catalog repos + `AppConfigRepository`; delegates to shared `SearchLogic` (passes `viewModelScope` + config flow for automatic default format observation).
- **Compose UI**: `SearchFilterCard` supports `compact` mode for two-team side-by-side layout — compact cards show stacked sub-filter badge icons (item/tera with outlined circles, ability with initials text) and a `MoreVert` dropdown menu instead of inline buttons. Full-width cards (single-team) retain inline Item/Tera/Ability buttons. Ability button shows initials in outlined circle when set, or "Ability" text button when unset (no `SearchFilterRestrictions` — all Pokemon can filter by ability). `PokemonPickerSheet`, `ItemPickerSheet`, `TeraTypePickerSheet`, `AbilityPickerSheet`, `FormatPickerSheet`, `MinRatingPickerSheet`, `SortOrderPickerSheet` (all `ModalBottomSheet`). Format picker sizes to content (no `skipPartiallyExpanded`); Pokemon/Item/Tera/Ability pickers expand full-screen. Picker state tracks team (1 or 2) to route selections to the correct filter list.
- **ContentListViewModel keying**: `hiltViewModel(key = ...)` uses `mode.params.toString()` for search mode so identical searches reuse cached results (pull-to-refresh available), while different params get a fresh ViewModel. Favorites key includes `contentType.name`: `"content_list_favorites_${mode.contentType.name}"`
- **Button styling**: Search option buttons use `surfaceVariant` background / `onSurfaceVariant` text (same as move chips), not Material `Button`

## iOS
- **CatalogStore** (`@MainActor ObservableObject`): Loads all 5 catalogs in parallel `Task`s on init via shared typed loaders (`CatalogLoaderKt.loadPokemonCatalog()`, etc.). Created eagerly in `DependencyContainer`, survives full app lifecycle. Exposes `@Published` loading/items/error for each catalog. `reload()` resets all state and re-launches all 5 load tasks (used by cache invalidation in Settings and config-driven catalog version changes).
- **SearchViewModel**: `@MainActor ObservableObject` that delegates to shared `SearchLogic` (passes `CoroutineScopeFactory` scope + `AppConfigRepository.config` for automatic default format observation). Observes `SearchLogic.uiState` via `for await` loop and republishes as `@Published state`. Convenience computed properties (`minRating`, `maxRating`, `timeStart`, `timeEnd`) bridge Kotlin types back to Swift-native types for the view.
- **SwiftUI**: `SearchFilterCard` supports `compact` mode — compact cards show stacked sub-filter badges (overlapping circles with outline for item/tera, initials text for ability) and a SwiftUI `Menu` (system context menu). Full-width cards retain inline Item/Tera/Ability buttons. Ability button shows initials in outlined circle when set, or "Ability" text button when unset. Picker sheets presented via `SearchSheet` enum (with `team` parameter on `.pokemon`, `.item`, `.teraType`, `.ability`) + `.sheet(item:)`. Format picker uses `.presentationDetents([.medium])` to avoid full-screen for small content. `SearchView` takes `catalogStore: CatalogStore` and `appConfigStore: AppConfigStore`.
- **Button styling**: Search option buttons use `systemGray5` background (same as move chips), not `.borderedProminent`

## Web
- **Catalog repos**: 5 shared catalog repos added to `DependencyContainer` (lazy singletons), eagerly initialized in `WebApp()` via `remember {}` block at app startup
- **SearchViewModel**: Plain `ViewModel()` constructor-injecting all 5 catalog repos + optional `AppConfigRepository`. Delegates to shared `SearchLogic` (passes `viewModelScope` + config flow for automatic default format observation).
- **Compose UI**: `SearchFilterCard` supports `compact` mode (same pattern as Android — stacked badges + `MoreVert` dropdown). On desktop web (`Expanded` window size), compact cards render the same full layout as single-team cards (name + inline buttons). `PickerDialogs.kt` consolidates all 7 pickers as `Dialog` composables (search field at TOP, web convention). `PokemonPickerDialog`, `ItemPickerDialog`, `TeraTypePickerDialog`, `AbilityPickerDialog`, `FormatPickerDialog`, `MinRatingPickerDialog`, `SortOrderPickerDialog`. Picker state tracks team for routing.

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
