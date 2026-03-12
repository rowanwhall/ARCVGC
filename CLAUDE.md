# Showdown26

Kotlin Multiplatform (KMP) app for browsing Pokemon Showdown battle replays. Targets Android (Jetpack Compose), iOS (SwiftUI), and Web (Compose for wasmJs) with a shared core.

## Tech Stack

- **Kotlin** 2.3.0, **Compose Multiplatform** 1.10.0
- **Ktor** 3.4.0 (OkHttp on Android, Darwin on iOS, Js on Web)
- **kotlinx-serialization** 1.10.0, **kotlinx-coroutines** 1.10.2
- **Coil 3** for image loading (Android, Web); native `AsyncImage` (iOS)
- **Hilt** 2.59.1 for Android DI
- **Sentry KMP** 0.24.0 for crash reporting (Android/iOS via KMP SDK, Web via JS Browser SDK + `@JsFun` bridge)
- **SKIE** 0.10.9 for Kotlin/Swift sealed class interop
- AGP 9.0.0, minSdk 24, targetSdk 36

## Project Structure

```
shared/src/commonMain/    — Shared Kotlin code (network, domain, data, UI models, mappers)
shared/src/androidMain/   — Android HTTP engine (OkHttp), FavoritesStorage (SharedPreferences)
shared/src/iosMain/       — iOS HTTP engine (Darwin), FavoritesStorage (NSUserDefaults)
shared/src/wasmJsMain/    — Web HTTP engine (Js), FavoritesStorage (localStorage via @JsFun)
composeApp/src/androidMain/ — Android app (Compose UI, Hilt DI, ViewModels)
iosApp/                   — iOS app (SwiftUI views, ViewModels)
webApp/src/wasmJsMain/    — Web app (Compose for wasmJs, manual DI, desktop-optimized layouts)
```

## Architecture

Clean architecture with three layers and explicit mappers between them:

```
Network DTOs  →  Domain Models  →  UI Models  →  Screen
  (Ktor)        (toDomain())      (toUiModel())   (Compose/SwiftUI)
```

- **Network layer** (`shared/.../network/`): `ApiService` with Ktor, DTO models, `ApiConstants` for base URL/endpoints, `CatalogLoader` (generic pagination), `SearchRequestMapper` (search request building)
- **Domain layer** (`shared/.../domain/model/`): Pure Kotlin data classes — `MatchPreview`, `MatchDetail`, `PlayerDetail`, `PokemonDetail`, `PokemonListItem`, `SearchFilterSlot`, etc.
- **UI layer** (`shared/.../ui/`): Platform-agnostic UI models and mappers (including `TimeFormatter` for shared time formatting); platform-specific screens in `composeApp/` and `iosApp/`
- **Data layer** (`shared/.../data/`): Shared business logic used by both platforms
  - `BattleRepository` — Match data (getMatches, searchMatches, getMatchDetail, getMatchesByIds, getPokemonByIds, getPlayersByNames). Throws exceptions on error. Android wraps in `Result<T>` via a thin adapter; iOS uses directly via SKIE `async throws` bridge.
  - `FavoritesRepository` — Favorites toggle/check with `StateFlow` state, delegates persistence to `FavoritesStorage`
  - `FavoritesStorage` — `expect`/`actual` for platform persistence (SharedPreferences on Android, NSUserDefaults on iOS, localStorage on Web)
  - `SettingsRepository` — App settings with `StateFlow` per setting + combined `settingItems: StateFlow<List<SettingItem>>`. Delegates persistence to `SettingsStorage`. Takes optional `CatalogCacheStorage` param for cache-clearing actions via `performAction(key)`
  - `SettingsStorage` — `expect`/`actual` for platform persistence (SharedPreferences on Android, NSUserDefaults on iOS, localStorage on Web)
  - `MatchesResult` — Data class returned by `BattleRepository.getMatches()`/`searchMatches()`
  - `CatalogState<T>` — Generic data class for catalog loading state (`isLoading`, `items`, `error`)
  - `CatalogCache` — Singleton object providing `load()` / `save()` (generic with TTL) and `clearAll()` for catalog data. Uses `CatalogCacheStorage` for platform persistence
  - `CatalogCacheStorage` — `expect`/`actual` for catalog cache persistence (`getString`/`putString`/`getLong`/`putLong`). Android uses `SharedPreferences("catalog_cache")`, iOS uses `NSUserDefaults`, Web uses `localStorage` via `@JsFun`
  - `PokemonCatalogRepository`, `ItemCatalogRepository`, `TeraTypeCatalogRepository`, `FormatCatalogRepository` — Singleton repos that check `CatalogCache` first (TTL: 30 days for Pokemon/Items, 7 days for TeraTypes/Formats), then fetch via `loadFullCatalog()` on cache miss. Expose `StateFlow<CatalogState<T>>` and `reload()` method (resets to loading state, re-fetches from cache/network)
- **Android repositories** (`composeApp/.../data/repository/`): Thin Hilt-injectable wrappers
  - `BattleRepository.kt` — Interface + `BattleRepositoryImpl` delegating to shared `BattleRepository`, wrapping results in `Result<T>`
  - `FavoritesRepository.kt` — Interface + `FavoritesRepositoryImpl` delegating to shared `FavoritesRepository`
  - `SettingsRepository.kt` — Interface + `SettingsRepositoryImpl` delegating to shared `SettingsRepository`. Passes `CatalogCacheStorage(context)` to shared repo
  - `PokemonCatalogRepository.kt`, `ItemCatalogRepository.kt`, `TeraTypeCatalogRepository.kt`, `FormatCatalogRepository.kt` — Interface + `Impl` delegates to shared catalog repos, expose `StateFlow<CatalogState<T>>` and `reload()`

Error handling uses `NetworkResult<T>` sealed class (`Success`/`Error`) at the API layer. Shared `BattleRepository` converts to thrown exceptions; shared `CatalogLoader` converts to `CatalogResult<T>`.

## API

Base URL: `https://arcvgc.com`

| Endpoint | Purpose |
|---|---|
| `GET /api/v0/matches/?limit=50&page=1` | Paginated battle list |
| `GET /api/v0/matches/{id}` | Battle detail |
| `GET /api/v0/pokemon/?exclude_illegal=true&limit=50&page=N` | Pokemon catalog (825 items, 17 pages) |
| `GET /api/v0/items/?limit=50&page=N` | Item catalog (412 items, 9 pages) |
| `GET /api/v0/types/tera?limit=50&page=N` | Tera type catalog (20 items, 1 page) |
| `GET /api/v0/formats?limit=50&page=N` | Format catalog |
| `POST /api/v0/matches/search` | Search matches (body: `SearchRequestDto`) |

All catalog endpoints return `{ success, data, pagination }` using the existing `PaginationDto` shape. Constants defined in `shared/.../network/ApiConstants.kt`.

## Key Screens

- **ContentListPage** — Generic paginated list page rendering heterogeneous `ContentListItem` types (Battle, Pokemon, Player). Supports pull-to-refresh (mobile) or refresh button (web); tapping battles opens a detail bottom sheet (mobile) or inline detail panel (web). Used for Home, Search results, per-Pokemon battles, and all three Favorites sub-tabs. Sort toggle button in the "Battles" section header for Search, Pokemon, and Player modes.
- **BattleDetailScreen** — Two tabs: "Team Preview" (full Pokemon details per player) and "Replay" (WebView on Android, "Open Replay" button on web)
- **SearchPage** — Pokemon filter UI: select up to 6 Pokemon, each with optional item and tera type. Also has user-selectable format (loaded from API), minimum rating (fixed list 1000–1700), and sort order (rating/time). Picker bottom sheets on mobile, `Dialog` composables on web. All search params bundled into `SearchParams` and passed to `ContentListMode.Search`.
- **FavoritesPage** — Three sub-tabs: "Battles", "Pokemon", and "Players" — all rendered via `ContentListPage` with `ContentListMode.Favorites(contentType)`. Each tab auto-refreshes when its respective favorites set changes.
- **SettingsPage** — List of app settings rendered from shared `SettingItem` metadata. Current settings: "Dark Mode" (system/light/dark picker), "Theme Color" (accent color picker), "Winner Highlight" toggle (green border on winning team), "Invalidate Cache" action (clears cached catalogs and reloads from network), "Privacy Policy" and "Terms of Service" links (open hosted web pages in browser)

## Favorites Architecture

### Shared layer
- **`FavoritesRepository`** (`shared/.../data/`): In-memory `StateFlow<Set<Int>>` for pokemon and battle IDs, `StateFlow<Set<String>>` for player names. Delegates persistence to `FavoritesStorage`. Methods: `togglePokemonFavorite()`, `toggleBattleFavorite()`, `togglePlayerFavorite()`, `isPokemonFavorited()`, `isBattleFavorited()`, `isPlayerFavorited()`, `currentPokemonIds()`, `currentBattleIds()`, `currentPlayerNames()` (list snapshots for iOS interop)
- **`FavoritesStorage`** (`expect`/`actual`): `loadIds(key)` / `saveIds(key, ids)` — Android uses `SharedPreferences`, iOS uses `NSUserDefaults`
- **`ContentListItem`** (`shared/.../ui/model/`): Sealed class enabling heterogeneous list rendering. Variants: `Battle(uiModel: BattleCardUiModel)`, `Pokemon(id, name, imageUrl, types: List<TypeUiModel>)`, `Player(id, name)`. Each has abstract `listKey: String` for stable list keys.
- **`FavoriteContentType`** (`shared/.../ui/model/`): Enum — `Battles`, `Pokemon`, `Players`. Parameterizes `ContentListMode.Favorites`.
- **`ContentListItemMapper`** (`shared/.../ui/mapper/`): Singleton object with `fromBattles()`, `fromPokemon()`, `fromPlayers()` — wraps domain models into `ContentListItem` variants. Used by all 3 platform ViewModels. Critical for iOS since constructing sealed class variants from Swift via this mapper is cleanest.

### Android
- `FavoritesRepository` interface + `FavoritesRepositoryImpl` (delegates to shared, injected via Hilt)
- `ContentListViewModel` handles all favorites modes: `Favorites(Battles)` loads battle IDs, `Favorites(Pokemon)` observes `favoritePokemonIds` flow and loads Pokemon details via `BattleRepository.getPokemonByIds()`, `Favorites(Players)` observes `favoritePlayerNames` flow and loads Player details via `BattleRepository.getPlayersByNames()`. Auto-refreshes when favorites change.
- `FavoritesPage`: Three sub-tabs (Battles, Pokemon, Players), all rendered via `ContentListPage(mode = ContentListMode.Favorites(contentType))`
- Heart buttons: `BattleDetailSheetWrapper` toggles battle favorites; `ContentListPage` toolbar toggles Pokemon favorites in Pokemon mode

### iOS
- `FavoritesStore` (`@MainActor ObservableObject`): Thin wrapper around shared `FavoritesRepository`, exposes `@Published` sets for SwiftUI reactivity, syncs state after each mutation
- `ContentListViewModel` handles all favorites modes (same pattern as Android). Takes optional `favoritesStore: FavoritesStore` param for reading favorite IDs.
- `FavoritesView`: Three sub-tabs (Battles, Pokemon, Players), all rendered via `ContentListView(mode: .favorites(contentType: ...))`. Auto-refresh via `.onChange(of: favoritesStore.favoritePokemonIds/favoritePlayerNames)`.
- Heart buttons: `BattleDetailSheet` toggles battle favorites; `ContentListView` toolbar toggles Pokemon favorites in Pokemon mode

### Web
- Uses shared `FavoritesRepository` directly via `DependencyContainer` singleton (same instance across all screens)
- `ContentListViewModel` handles all favorites modes (same pattern as Android but with try/catch instead of Result<T>)
- `FavoritesPage`: Three sub-tabs (Battles, Pokemon, Players), all rendered via `ContentListPage(mode = ContentListMode.Favorites(contentType))`
- Heart buttons: `BattleDetailPanel` toggles battle favorites; `ContentListPage` toolbar toggles Pokemon favorites in Pokemon mode
- Persistence via `FavoritesStorage` using browser `localStorage` (survives page refreshes)

## Settings Architecture

### Shared layer
- **`SettingsRepository`** (`shared/.../data/`): Manages all app settings. Constructor: `SettingsRepository(storage: SettingsStorage, cacheStorage: CatalogCacheStorage? = null)`. Each setting has its own `MutableStateFlow` (e.g., `_showWinnerHighlight`, `_selectedThemeId`, `_darkModeId`) plus a combined `settingItems: StateFlow<List<SettingItem>>` that auto-updates when any setting changes. Methods: `setBooleanSetting(key, value)` / `setIntSetting(key, value)` (generic dispatchers), `performAction(key)` (one-shot actions like cache clearing), `getSettingItems()` / `isShowWinnerHighlightEnabled()` / `getSelectedThemeId()` / `getDarkModeId()` (snapshots for iOS interop)
- **`SettingsStorage`** (`expect`/`actual`): `getBoolean`/`putBoolean`/`getInt`/`putInt` — Android uses `SharedPreferences`, iOS uses `NSUserDefaults`, Web uses `localStorage` via `@JsFun`
- **`SettingItem`** (`shared/.../ui/model/`): Sealed class with five subclasses: `DarkModeChoice` (key, title, subtitle, selectedModeId), `Toggle` (key, title, subtitle, isEnabled), `ColorChoice` (key, title, subtitle, selectedThemeId), `Action` (key, title, subtitle, confirmationMessage — triggers one-shot action), `Link` (key, title, subtitle, url — opens external URL in browser). Metadata defined once in shared `buildSettingItems()` — platforms never hardcode setting titles/subtitles
- **`DarkModeOption`** (`shared/.../ui/model/`): Enum with `System` (0), `Light` (1), `Dark` (2). `fromId()` companion for safe lookup.
- **Adding a new boolean setting**: Add a `MutableStateFlow` + setter in `SettingsRepository`, add to `buildSettingItems()`, add key routing in `setBooleanSetting()` — no platform UI changes needed. For new `SettingItem` subclass types, each platform's settings UI needs a new rendering branch.

### Android
- `SettingsRepository` interface + `SettingsRepositoryImpl` (Hilt-injectable, delegates to shared). Passes `CatalogCacheStorage(context)` to shared repo. Exposes `settingItems: StateFlow<List<SettingItem>>`, `showWinnerHighlight: StateFlow<Boolean>`, `selectedThemeId: StateFlow<Int>`, `darkModeId: StateFlow<Int>`, `performAction(key)`
- `SettingsViewModel` (`@HiltViewModel`): Injects `SettingsRepository` + all 4 catalog repos. `performAction(key)` calls `settingsRepository.performAction()` then `reload()` on all 4 catalog repos
- `SettingsPage`: Collects `settingItems` flow, renders each via `when`: `DarkModeChoiceSettingRow` (tappable → `DarkModePickerSheet`), `ToggleSettingRow` (switch), `ColorChoiceSettingRow` (tappable with color swatch → `ThemePickerSheet`), `ActionSettingRow` (tappable → confirmation `AlertDialog`), `LinkSettingRow` (tappable → opens URL via `Intent(ACTION_VIEW)`)

### iOS
- `SettingsStore` (`@MainActor ObservableObject`): Wraps shared `SettingsRepository` (passes `CatalogCacheStorage()` to constructor), exposes `@Published settingItems`, `@Published showWinnerHighlight`, `@Published selectedThemeId`, `@Published darkModeId`, `@Published themeColor`, computed `colorSchemeOverride: ColorScheme?`. Methods: `setBooleanSetting()`, `setIntSetting()`, `performAction()`. `syncState()` refreshes all after mutations
- `SettingsView`: Takes `settingsStore` + optional `catalogStore`. Iterates `settingsStore.settingItems` with SKIE `switch onEnum(of:)` — `.darkModeChoice` renders tappable row → `DarkModePickerSheet`, `.toggle` renders `Toggle`, `.colorChoice` renders tappable row → `ThemePickerSheet`, `.action` renders tappable row → confirmation `.alert()` that calls `performAction()` + `catalogStore?.reload()`, `.link` renders SwiftUI `Link` that opens URL in browser

### Web
- Uses shared `SettingsRepository` directly via `DependencyContainer` (passes `cacheStorage` to constructor)
- `SettingsPage`: Collects `settingItems` flow, same `when` pattern as Android — `DarkModeChoiceSettingRow` (→ `DarkModePickerDialog`), `ToggleSettingRow`, `ColorChoiceSettingRow` (→ `ThemePickerDialog`), `ActionSettingRow` (→ confirmation `AlertDialog` that calls `performAction()` + `reload()` on all 4 catalog repos from `DependencyContainer`), `LinkSettingRow` (opens URL via `window.open()`)

## Crash Reporting (Sentry)

Sentry is used for crash and error reporting across all three platforms. Each platform has its own Sentry project and DSN.

### Architecture
- **Shared layer**: `expect fun initializeSentry()` in `shared/.../data/SentryInit.kt` with platform `actual` implementations
- **Android** (`SentryInit.android.kt`): Uses Sentry KMP SDK (`io.sentry.kotlin.multiplatform.Sentry.init {}`)
- **iOS** (`SentryInit.ios.kt`): Uses Sentry KMP SDK (bridges to Sentry Cocoa SDK). Requires Sentry Cocoa framework via SPM in Xcode
- **Web** (`SentryInit.wasmJs.kt`): The KMP SDK's wasmJs target is a no-op stub. Real error tracking uses the Sentry JavaScript Browser SDK loaded via `<script>` tag in `webApp/.../resources/index.html`, bridged from Kotlin via `@JsFun`

### Initialization
Called once at app startup, before any other initialization:
- **Android**: `ShowdownApplication.onCreate()` calls `initializeSentry()`
- **iOS**: `iOSApp.init()` calls `SentryInit_iosKt.initializeSentry()`
- **Web**: `main()` in `Main.kt` calls `initializeSentry()` before `ComposeViewport`

### Gradle
- Plugin: `io.sentry.kotlin.multiplatform.gradle` applied in `shared/build.gradle.kts`
- The plugin auto-installs the `sentry-kotlin-multiplatform` dependency into `commonMain`
- iOS requires the Sentry Cocoa SDK added via Swift Package Manager in Xcode (`https://github.com/getsentry/sentry-cocoa.git`)

## Dark Mode

The app supports three dark mode options: **System** (default, follows OS), **Light**, and **Dark**. Accent/primary colors stay the same across light and dark modes.

### Theme implementation
- **Android** (`App.kt`): 4 light + 4 dark `ColorScheme` definitions (Red/Blue/Yellow/Purple). `colorSchemeForTheme(themeId, isDark)` selects the right one. `isDark` derived from `darkModeId` + `isSystemInDarkTheme()`.
- **iOS** (`ContentView.swift`): `.preferredColorScheme(settingsStore.colorSchemeOverride)` on TabView. `nil` = system, `.light` / `.dark` = forced. SwiftUI handles dark mode colors natively.
- **Web** (`WebApp.kt`): Same 4+4 color scheme pattern as Android. Content wrapped in `Surface` (critical — see below).

### Color rules for new UI code
- **NEVER use hardcoded `Color.Black` or `Color.White` for text or backgrounds.** Use theme-aware colors instead:
  - **Android/Web (Compose)**: `MaterialTheme.colorScheme.onSurface` (text), `MaterialTheme.colorScheme.surface` (backgrounds), `MaterialTheme.colorScheme.surfaceContainer` (page backgrounds), `MaterialTheme.colorScheme.onSurfaceVariant` (secondary text)
  - **iOS (SwiftUI)**: `Color(.label)` (text), `Color(.secondarySystemBackground)` (page backgrounds), `Color(.systemBackground)` (card backgrounds), `Color(.systemGray6)` (inner section backgrounds), `Color(.secondaryLabel)` (secondary text)
- **Exception**: `Color.Black`/`Color.White` in `PokemonAvatar.kt` / `PokemonAvatar.swift` are intentional Pokeball design colors — leave those alone.
- **Surface hierarchy** (dark mode contrast, from outermost to innermost):
  - **Android/Web**: `surfaceContainer` (page) → `surface` (card) → `surfaceContainer` (inner section)
  - **iOS**: `secondarySystemBackground` (page) → `systemBackground` (card) → `systemGray6` (inner section)
  - **iOS detail sheet cards/chips**: Use `Color(.secondarySystemBackground)` (not `systemBackground`) for elements inside `systemGray6` sections, so they contrast against the section background

### Web-specific: `Surface` wrapper requirement
`WebApp.kt` wraps all content in a `Surface` composable inside `MaterialTheme`. This is **required** because `MaterialTheme` alone does NOT set `LocalContentColor` — only `Surface` does. Without it, `Text` composables default to `Color.Black` regardless of dark mode. Android avoids this issue because `Scaffold` provides `Surface` behavior. **Do not remove the `Surface` wrapper in `WebApp.kt`.**

## Search / Filter Architecture

### Shared layer
- **Domain models**: `PokemonListItem`, `SearchFilterSlot` (ID-based filter state), `Format`, `SearchParams` (bundles filters + formatId + minimumRating + orderBy)
- **DTOs**: `PokemonListResponseDto`, `ItemListResponseDto`, `TeraTypeListResponseDto`, `FormatListResponseDto`, `SearchRequestDto` — all reuse existing `PaginationDto`
- **Network mappers**: `PokemonListMapper.kt`, `ItemListMapper.kt`, `FormatListMapper.kt` — all `Dto.toDomain()` extension functions
- **UI models**: `PokemonPickerUiModel`, `SearchFilterSlotUiModel`, `FormatUiModel` (id + displayName), `ItemUiModel`, `TeraTypeUiModel`
- **UI mappers**: `PokemonPickerUiMapper`, `ItemUiMapper`, `TeraTypeUiMapper`, `FormatUiMapper` — all singleton objects with `map()` / `mapList()`
- **Shared utilities**:
  - `SearchRequestMapper.kt` — `buildSearchRequest()` converts `SearchFilterSlot` list + params into `SearchRequestDto`
  - `CatalogLoader.kt` — `loadFullCatalog()` generic pagination loop + typed loaders (`loadPokemonCatalog()`, `loadItemCatalog()`, `loadTeraTypeCatalog()`, `loadFormatCatalog()`) for iOS interop
  - `TimeFormatter.kt` — `formatUploadTime()` shared by `BattleCardUiMapper` and `BattleDetailUiMapper`
- **Catalog repos** (`shared/.../data/`): `CatalogState<T>` data class + 4 singleton repo classes (`PokemonCatalogRepository`, `ItemCatalogRepository`, `TeraTypeCatalogRepository`, `FormatCatalogRepository`). Each checks `CatalogCache` on init (TTL-based), falls back to `loadFullCatalog()` on cache miss, using `CoroutineScope(SupervisorJob() + Dispatchers.Default)`. Exposes `StateFlow<CatalogState<T>>` and `reload()` (resets state + re-fetches). Used directly by Android (via Hilt wrappers) and Web; iOS uses a Swift-native `CatalogStore` instead (avoids Kotlin generic bridging issues).
- **ContentListMode**: Sealed class — `Home`, `Favorites(contentType: FavoriteContentType)`, `Search(params: SearchParams)`, `Pokemon(pokemonId, name, imageUrl, typeImageUrl1, typeImageUrl2)`, or `Player(playerId, name)`

### Android
- **Catalog repos** (4 Hilt-injectable interfaces + `Impl` classes): Thin wrappers that delegate to shared catalog repos, expose `StateFlow<CatalogState<T>>` and `reload()` from shared `CatalogState` (`com.example.showdown26.data.CatalogState`)
- **Eager init**: `CatalogInitializer` injected into `ShowdownApplication` forces all 4 catalog repos to load at app startup
- **SearchViewModel**: `@HiltViewModel` injecting all 4 catalog repos; manages `SearchUiState` (filter slots + selectedFormat + selectedMinRating + selectedOrderBy)
- **Compose UI**: `SearchFilterCard` (auto-sizing text via `AutoSizeText` composable), `PokemonPickerSheet`, `ItemPickerSheet`, `TeraTypePickerSheet`, `FormatPickerSheet`, `MinRatingPickerSheet`, `SortOrderPickerSheet` (all `ModalBottomSheet`). Format picker sizes to content (no `skipPartiallyExpanded`); Pokemon/Item/Tera pickers expand full-screen.
- **ContentListViewModel keying**: `hiltViewModel(key = ...)` uses `mode.params.toString()` for search mode so identical searches reuse cached results (pull-to-refresh available), while different params get a fresh ViewModel. Favorites key includes `contentType.name`: `"content_list_favorites_${mode.contentType.name}"`
- **Button styling**: Search option buttons use `surfaceVariant` background / `onSurfaceVariant` text (same as move chips), not Material `Button`

### iOS
- **CatalogStore** (`@MainActor ObservableObject`): Loads all 4 catalogs in parallel `Task`s on init via shared typed loaders (`CatalogLoaderKt.loadPokemonCatalog()`, etc.). Created eagerly in `DependencyContainer`, survives full app lifecycle. Exposes `@Published` loading/items/error for each catalog. `reload()` resets all state and re-launches all 4 load tasks (used by cache invalidation in Settings).
- **SearchViewModel**: `@MainActor ObservableObject` managing only filter slot UI state (no catalog loading). No init params needed.
- **SwiftUI**: `SearchFilterCard` (`.minimumScaleFactor(0.6)` for auto-sizing), picker sheets presented via `SearchSheet` enum + `.sheet(item:)`. Format picker uses `.presentationDetents([.medium])` to avoid full-screen for small content. `SearchView` takes `catalogStore: CatalogStore` and reads catalog state from it.
- **Button styling**: Search option buttons use `systemGray5` background (same as move chips), not `.borderedProminent`

### Web
- **Catalog repos**: 4 shared catalog repos added to `DependencyContainer` (lazy singletons), eagerly initialized in `WebApp()` via `remember {}` block at app startup
- **SearchViewModel**: Plain `ViewModel()` constructor-injecting all 4 catalog repos. Exposes their `StateFlow<CatalogState<T>>` directly (no internal loading logic).
- **Compose UI**: `SearchFilterCard` (same as Android), `PickerDialogs.kt` consolidates all 6 pickers as `Dialog` composables (search field at TOP, web convention). `PokemonPickerDialog`, `ItemPickerDialog`, `TeraTypePickerDialog`, `FormatPickerDialog`, `MinRatingPickerDialog`, `SortOrderPickerDialog`

### Pokemon image styling
Pokemon images across the app use a consistent pattern: a smaller gray circle background (`surfaceVariant` / `systemGray5`) with the Pokemon sprite rendered larger and overlaying (not clipped by) the circle. Sizes vary by context:
- **BattleCard**: 70% circle / 100% sprite (relative to slot)
- **BattleDetail**: 100dp circle / 144dp sprite
- **SearchFilterCard**: 40dp circle / 56dp sprite
- **PokemonPickerSheet**: 46dp circle / 64dp sprite

## Sort Toggle & Section Loading

### Sort toggle
The "Battles" section header in Search, Pokemon, and Player modes includes a sort toggle button (time ↔ rating). Sort order is **ViewModel-owned state** (`_sortOrder` / `sortOrder` StateFlow), not stored in `SearchParams`. On toggle, the ViewModel re-fetches page 1 content with the new sort order while keeping existing items visible behind a per-section loading indicator.

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

## Navigation

### Android (`App.kt`)
- Tab bar with Top, Search, Favorites, and Settings tabs, managed by `selectedTab` state
- `searchOverlayParams: SearchParams?` controls a full-screen `ContentListPage` overlay for search results (hides tab bar)
- `onBack` on the search overlay sets `searchOverlayParams = null` to return to tabs
- Pokemon/Player navigation is **not** handled in `App.kt` — each `ContentListPage` manages its own (see below)

### iOS (`ContentView.swift`, `SearchView.swift`)
- `TabView` with Top, Search, Favorites, and Settings tabs, each with its own `NavigationStack`
- Search results pushed via `navigationDestination(isPresented:)` bound to `searchParams` state
- Pokemon pages pushed via `navigationDestination(isPresented:)` bound to `pokemonNavTarget` state in `ContentListView`
- Player pages pushed via `navigationDestination(isPresented:)` bound to `playerNavTarget` state in `ContentListView`
- Back navigation handled automatically by SwiftUI's NavigationStack

### Web (`WebApp.kt`)
- `NavigationRail` on the left (Top / Search / Favorites / Settings) instead of bottom `NavigationBar`
- `Row { NavigationRail { ... }; Box { content } }` layout
- `searchOverlayParams: SearchParams?` replaces content area with search results; cleared on tab switch
- Coil `setSingletonImageLoaderFactory` with Ktor network fetcher for image loading
- Pokemon/Player navigation managed per-`ContentListPage` instance (same recursive pattern as other platforms)

### Per-instance Pokemon/Player navigation (all platforms)
Each `ContentListPage` / `ContentListView` manages its own `pokemonNavTarget` and `playerNavTarget` state locally. When a Pokemon or Player is tapped in a detail sheet or list:
1. `pokemonNavTarget`/`playerNavTarget` is set (detail sheet is **suppressed**, not dismissed — `selectedBattleId` stays set)
2. A child `ContentListPage` / `ContentListView` renders on top with `mode = Pokemon` or `mode = Player`
3. On back, the nav target clears → detail sheet reappears with the battle still loaded
4. This is recursive — each child has its own independent state, creating a natural navigation stack
- Android: child renders in a `Box` overlay; `BackHandler` at each level intercepts system back
- iOS: child pushed via `navigationDestination(isPresented:)`; NavigationStack handles back automatically

### Battle detail presentation
- Android: `ModalBottomSheet` in `ContentListPage.kt` via `BattleDetailSheetWrapper`, keyed by `selectedBattleId`
- iOS: `.sheet(isPresented:)` in `ContentListView.swift`, bound to `selectedBattleId`
- Web: `BattleDetailPanel` renders inline in a right pane (`weight(0.6f)`) of a master-detail `Row` layout. List pane takes `weight(0.4f)`. Selected battle highlighted with `primaryContainer` background. No sheet/overlay.
- Sheet is suppressed (not dismissed) during Pokemon navigation: Android gates composition on `pokemonNavTarget == null`; iOS gates the sheet binding getter on `pokemonNavTarget == nil`; Web replaces the entire page with a child `ContentListPage(mode = Pokemon(...))`
- Pokemon/Player tap callbacks are handled locally within each `ContentListPage` / `ContentListView` — they do not bubble up to parent

### Key file locations — content list
- Android: `ContentListPage.kt`, `ContentListViewModel.kt`, `ContentListUiState.kt` (all in `composeApp/.../ui/contentlist/`)
- iOS: `ContentListView.swift`, `ContentListViewModel.swift`, `ContentListState.swift` (in `iosApp/iosApp/`)
- Web: `ContentListPage.kt`, `ContentListViewModel.kt`, `ContentListUiState.kt` (all in `webApp/.../ui/contentlist/`)

### Key file locations — battle detail
- Android: `BattleDetailScreen.kt` (sheet + tabs), `TeamPreviewTab.kt`, `PokemonDetailCard.kt` (all in `composeApp/.../ui/battledetail/`)
- iOS: `BattleDetailSheet.swift` (sheet + tabs + TeamPreviewTab + PlayerTeamDetailSection), `PokemonDetailCard.swift` (in `iosApp/iosApp/`)
- Web: `BattleDetailPanel.kt` (inline panel + tabs), `TeamPreviewTab.kt`, `PokemonDetailCard.kt`, `ReplayTab.kt` (all in `webApp/.../ui/battledetail/`)
- Shared `BattleRepository`: `shared/.../data/BattleRepository.kt` (used by all platforms)

## Build & Run

```bash
# Android
./gradlew :composeApp:assembleDebug

# iOS — build shared framework first, then open in Xcode
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
open iosApp/iosApp.xcodeproj

# iOS — full build via command line
cd iosApp && xcodebuild build -project iosApp.xcodeproj -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 17 Pro'

# Web — dev server with hot reload
./gradlew :webApp:wasmJsBrowserDevelopmentRun

# Web — production build (output in webApp/build/dist/wasmJs/productionExecutable/)
./gradlew :webApp:wasmJsBrowserDistribution
```

## Conventions

- **Compose `modifier` parameter ordering**: In all `@Composable` functions (Android and Web), `modifier: Modifier = Modifier` must be the **first optional parameter** — immediately after all required parameters, before any other optional parameters. This follows the official Compose API guidelines.
- **Previews required**: All `@Composable` functions (Android) and SwiftUI `View` structs (iOS) should include `@Preview` / `#Preview` blocks. Use `PreviewAsyncImage` for any image that loads from a URL at runtime:
  - **Android**: `PreviewAsyncImage(url, previewDrawable = R.drawable.preview_xxx, contentDescription, modifier)` — shows the drawable in `@Preview` mode (`LocalInspectionMode`), `AsyncImage` at runtime. Located in `composeApp/.../ui/components/PreviewAsyncImage.kt`.
  - **iOS**: `PreviewAsyncImage(url: url, previewAsset: "PreviewXxx")` — shows the asset catalog image in Xcode previews (`isPreview` flag), `AsyncImage` at runtime. Located in `iosApp/iosApp/PreviewAsyncImage.swift`. The `isPreview` constant is defined in `PokemonAvatar.swift`.
  - Available preview assets: `preview_pokemon` / `PreviewPokemon` (Pokemon sprites), `preview_item` / `PreviewItem` (held items), `preview_tera` / `PreviewTera` (tera type icons), `preview_type_1` / `PreviewType1` and `preview_type_2` / `PreviewType2` (type icons — Dragon and Flying)
- DTOs in `network/model/`, domain models in `domain/model/`, UI models in `ui/model/`
- Network mappers are extension functions (`Dto.toDomain()`); UI mappers are singleton objects (`UiMapper.map()` / `UiMapper.mapList()`)
- Display names computed via properties (e.g., `Ability.displayName` converts camelCase to Title Case)
- Android uses Hilt `@Inject` + `@HiltViewModel`; iOS uses a manual `DependencyContainer` with `@StateObject` ViewModels; Web uses a manual `DependencyContainer` singleton with `remember { ViewModel(deps...) }`
- Pagination triggers when user is within 5 items of the list end. **`paginate()` must guard against `loadingSections.isNotEmpty()`** — see "Sort Toggle & Section Loading" section
- Package: `com.example.showdown26`
- Auto-sizing text: Android uses custom `AutoSizeText` composable (Compose Multiplatform 1.10.0 lacks `TextDefaults.AutoSize`); iOS uses native `.minimumScaleFactor()`
- iOS Xcode project uses `PBXFileSystemSynchronizedRootGroup` — new Swift files are auto-discovered, no pbxproj edits needed
- Kotlin `Int` becomes `Int32` in Swift via SKIE bridge; use `onEnum(of:)` for sealed class pattern matching
- Kotlin/Native bridging caveats: `List<Int>` bridges to `[KotlinInt]` (not `[Int32]`), requiring `.map { KotlinInt(int:) }` / `.map { $0.int32Value }` conversions; generic Kotlin classes lose type parameters in Swift (e.g., `CatalogResult<T>.items` becomes `[Any]?`, requiring casts like `as? [SpecificType]`); Kotlin default parameter values do NOT bridge to Swift — all params must be passed explicitly
- Kotlin/Wasm caveats: `kotlinx.browser.localStorage` is NOT available in the shared module's wasmJsMain — use `@JsFun` external functions for JS interop instead; `Dispatchers.IO` is not available on wasmJs — use `Dispatchers.Default` or inherit caller's dispatcher; web ViewModels use `collectAsState()` instead of `collectAsStateWithLifecycle()`
- Web UI differences from Android: `NavigationRail` instead of `NavigationBar`; `BattleDetailPanel` inline instead of `ModalBottomSheet`; `Dialog` composables instead of `ModalBottomSheet` for pickers; `IconButton` refresh instead of `PullToRefreshBox`; no `BackHandler` — back buttons are visible UI elements; `TeamPreviewTab` uses fixed 280.dp card width instead of `LocalConfiguration.current.screenWidthDp`
- **ContentListItem rendering**: `ContentListPage` / `ContentListView` renders items heterogeneously via sealed class dispatch — `ContentListItem.Battle` → `BattleCard`/`BattleCardView`, `ContentListItem.Pokemon` → `PokemonListRow`/`SimplePokemonRow`, `ContentListItem.Player` → `PlayerListRow`/player `HStack`. iOS uses SKIE `onEnum(of:)` for pattern matching.
- **Dark mode colors**: Never use `Color.Black`/`Color.White` for text or backgrounds — use `MaterialTheme.colorScheme.*` (Android/Web) or semantic UIKit colors like `Color(.label)` (iOS). See the "Dark Mode" section for the full surface hierarchy and platform-specific color mappings.
- **Code quality — warnings and unused imports**: After completing any code changes, review all modified and newly created files for compiler warnings and unused imports before considering the task done. Specifically:
  - **Kotlin**: No unused imports, no redundant `!!` (use `?.let {}`, local val smart casts, or `.orEmpty()` instead), no `println()` debug statements left in code, all `CoroutineScope.launch {}` blocks must have try/catch on Kotlin/Native to prevent crashes
  - **Swift**: No unused imports — but note that `import Foundation` is **required** in any Swift file that `import Shared` (the Kotlin/Native framework depends on Foundation types via NSObject). Only remove `import Foundation` from files that also `import SwiftUI` (which re-exports Foundation). No force unwraps (`!`) where safe alternatives exist (use `if let`, `guard let`, `.map {}`)
  - Build all affected platforms after changes to verify no warnings or errors were introduced
- **Spelling**: All user-facing instances of the word "Pokemon" must use the accented spelling **"Pokémon"** (with é, U+00E9). Internal identifiers (class names, variable names, asset names, package names) remain unaccented.
- **README.md**: Keep `README.md` up to date when making changes that affect the public-facing project description. Specifically, update the README when: adding or removing a feature listed in the Features section, changing the tech stack (e.g., replacing a library), modifying the project structure (new modules or major directory changes), changing build commands or setup steps, or adding new prerequisites. Do not update the README for internal refactors, bug fixes, or changes that don't affect the information presented there.

## Legal

The app's public name is **ARC** (Automatic Replay Compiler). Legal documents live in `legal/`:
- `legal/privacy-policy.html` — Privacy policy (hosted as a web page, linked from Settings)
- `legal/terms-of-service.html` — Terms of service (hosted as a web page, linked from Settings)
- `legal/TODO.md` — Pre-launch checklist and ongoing maintenance reminders

Hosted URLs are defined in `SettingsRepository.companion` (`URL_PRIVACY_POLICY`, `URL_TERMS_OF_SERVICE`) — currently placeholders that must be updated before launch.

**Key legal claims** the documents make (must stay accurate):
- No personal data is collected or transmitted to any server
- All data (favorites, settings, cache) is stored locally on-device only
- Sentry crash reporting is the only third-party service integrated (no analytics, advertising, or tracking)
- No user accounts or authentication
- App is not affiliated with Nintendo, The Pokémon Company, Game Freak, Creatures Inc., or Pokémon Showdown/Smogon

**After implementing features that affect privacy or terms, review and update the legal documents.** Specifically:
- Adding analytics or ad SDKs → update Privacy Policy sections 4 and 5
- Adding user accounts, authentication, or remote data storage → update Privacy Policy sections 1, 2, 7, 8 and Terms of Service section 1
- Adding monetization or paid features → review both documents for necessary additions
- Collecting any new category of data → update Privacy Policy section 1
- Changing data sources or adding new third-party API integrations → update Terms of Service section 4

## Web App Deployment

The web app is hosted at **https://arcvgc.com** on a DigitalOcean droplet that also runs the Django API. nginx serves the static webapp files and reverse-proxies `/api/` and `/static/` to gunicorn. Server connection details are in `secrets.properties` (see `secrets.properties.example`).

### How to deploy

From the project root on the local machine:

```bash
./deploy/deploy.sh
```

This builds the webapp (`./gradlew :webApp:wasmJsBrowserDistribution`), uploads the production files to `/var/www/arcvgc/` on the server via rsync, and uploads the legal HTML pages. No server-side config changes or restarts are needed — nginx serves the new files immediately.

The script reads `DEPLOY_HOST` from `secrets.properties`. You can also pass the host as an argument: `./deploy/deploy.sh user@host`.

### When to deploy

Deploy the web app after any changes to:
- `webApp/` (web UI code)
- `shared/` (shared code used by web)
- `legal/*.html` (privacy policy, terms of service)

### Deployment files

- `deploy/deploy.sh` — Build + upload script (run from local machine)
- `deploy/arcvgc.conf` — nginx server config (installed on server at `/etc/nginx/sites-available/arcvgc.conf`)
- `deploy/SETUP.md` — One-time server setup guide (DNS, nginx, HTTPS)

### Server details

- **nginx config**: `/etc/nginx/sites-available/arcvgc.conf` (symlinked to `sites-enabled`)
- **Webapp files**: `/var/www/arcvgc/`
- **Django API**: gunicorn proxied by nginx
- **SSL**: Let's Encrypt via certbot (auto-renews)
- **Domain**: `arcvgc.com` — DNS A records point to the droplet

### If nginx config changes are needed

After editing `deploy/arcvgc.conf` locally, upload and reload:
```bash
scp deploy/arcvgc.conf $DEPLOY_HOST:/etc/nginx/sites-available/arcvgc.conf
# Then on the server:
sudo nginx -t && sudo systemctl reload nginx
```

## Web CORS & Image URL Handling

The API and web app are served from the same origin (`https://arcvgc.com`) via nginx reverse proxy, so CORS is not an issue in production. Two mechanisms handle dev and image URLs:

### API requests
- `getPlatformBaseUrl()` returns `""` on wasmJs, so all API calls use relative paths (`/api/v0/...`)
- `webApp/webpack.config.d/devServer.js` configures a webpack dev server proxy that forwards `/api` and `/static` to `https://arcvgc.com`
- In production, nginx reverse-proxies `/api/` and `/static/` to gunicorn on the same server

### Image URLs
- The API returns absolute image URLs (`https://arcvgc.com/static/images/...`) in all responses
- `normalizeImageUrl()` (`expect`/`actual` in `shared/.../network/`) rewrites these at the DTO-to-domain mapping layer:
  - **Android/iOS**: No-op (returns URL unchanged — direct HTTPS works fine)
  - **wasmJs**: Replaces the API host with `window.location.origin` (e.g., `http://localhost:8082/static/images/...` in dev), so requests go through the webpack proxy
- Applied in all 4 DTO-to-domain mappers: `MatchDetailMapper`, `MatchPreviewMapper`, `PokemonListMapper`, `ItemListMapper`
- This approach works in both dev (webpack proxy) and production (same-origin), since `window.location.origin` always resolves to the correct host
- Coil's `KtorNetworkFetcherFactory` requires full URLs — relative paths (like `/static/...`) will not work
