# Navigation

## Android (`App.kt`)
- Tab bar with Top, Search, Favorites, and Settings tabs, managed by `selectedTab` state
- `searchOverlayParams: SearchParams?` controls a full-screen `ContentListPage` overlay for search results (hides tab bar)
- `onBack` on the search overlay sets `searchOverlayParams = null` to return to tabs
- Pokemon/Player navigation is **not** handled in `App.kt` — each `ContentListPage` manages its own (see below)

## iOS (`ContentView.swift`, `SearchView.swift`)
- `TabView` with Top, Search, Favorites, and Settings tabs, each with its own `NavigationStack`
- Search results pushed via `navigationDestination(isPresented:)` bound to `searchParams` state
- Pokemon pages pushed via `navigationDestination(isPresented:)` bound to `pokemonNavTarget` state in `ContentListView`
- Player pages pushed via `navigationDestination(isPresented:)` bound to `playerNavTarget` state in `ContentListView`
- Back navigation handled automatically by SwiftUI's NavigationStack

## Web (`WebApp.kt`)
- Two layouts: `DesktopLayout` (≥600dp, `NavigationRail`) and `MobileLayout` (<600dp, bottom `NavigationBar`)
- `searchOverlayParams: SearchParams?` replaces content area with search results; cleared on tab switch
- Coil `setSingletonImageLoaderFactory` with Ktor network fetcher for image loading
- Pokemon/Player navigation managed per-`ContentListPage` instance (same recursive pattern as other platforms)

### Web ViewModel persistence (`ViewModelStore`)
- `ProvideViewModelStore` wraps the entire app content inside `Surface`, providing a `ViewModelStore` via `LocalViewModelStore`
- `rememberViewModel(key) { factory }` retrieves or creates ViewModels by key — ViewModels survive tab switches, back navigation, and recomposition because they live in the store, not in the composition tree
- Stable keys match the Android `hiltViewModel(key = ...)` pattern:
  - `"content_list_home"`, `"content_list_favorites_${contentType}"`, `"content_list_search_${params}"`, `"content_list_pokemon_${id}"`, `"content_list_player_${id}_${formatId}"`
  - `"search"` for `SearchViewModel`
  - `"battle_detail_${battleId}"` for `BattleDetailViewModel`
- Key files: `ViewModelStore.kt` (store + composable helpers), all web ViewModels use `rememberViewModel()` instead of bare `remember()`

### Web mobile navigation stack (`MobileLayout`)
- Uses a `List<MobileNavEntry>` stack instead of flat overlay variables
- `MobileNavEntry` sealed class: `BattleDetail(request)`, `Pokemon(id, name, ...)`, `Player(id, name, ...)`
- Supports arbitrary-depth drill-down: `battle → pokemon → battle → player → battle → ...`
- Each stack entry renders full-screen; "back" pops the top entry
- Each `Pokemon`/`Player` entry provides its own `LocalBattleOverlay` that pushes a new `BattleDetail` entry
- Tab switching clears the entire stack
- Combined with `ViewModelStore`, navigating back preserves loaded data (no re-fetch)

## Per-instance Pokemon/Player navigation (all platforms)
Each `ContentListPage` / `ContentListView` manages its own `pokemonNavTarget` and `playerNavTarget` state locally. When a Pokemon or Player is tapped in a detail sheet or list:
1. `pokemonNavTarget`/`playerNavTarget` is set (detail sheet is **suppressed**, not dismissed — `selectedBattleId` stays set)
2. A child `ContentListPage` / `ContentListView` renders on top with `mode = Pokemon` or `mode = Player`
3. On back, the nav target clears -> detail sheet reappears with the battle still loaded
4. This is recursive — each child has its own independent state, creating a natural navigation stack
- Android: child renders in a `Box` overlay; `BackHandler` at each level intercepts system back
- iOS: child pushed via `navigationDestination(isPresented:)`; NavigationStack handles back automatically

## Battle detail presentation
- Android: `ModalBottomSheet` in `ContentListPage.kt` via `BattleDetailSheetWrapper`, keyed by `selectedBattleId`
- iOS: `.sheet(isPresented:)` in `ContentListView.swift`, bound to `selectedBattleId`
- Web (desktop): `BattleDetailPanel` renders inline in a right pane (`weight(0.6f)`) of a master-detail `Row` layout. List pane takes `weight(0.4f)`. Selected battle highlighted with `primaryContainer` background. No sheet/overlay.
- Web (mobile): `BattleDetailPanel` renders as a full-screen overlay via the `MobileNavEntry.BattleDetail` nav stack entry. Pokemon/Player drill-down pushes additional entries onto the stack.
- Sheet is suppressed (not dismissed) during Pokemon navigation: Android gates composition on `pokemonNavTarget == null`; iOS gates the sheet binding getter on `pokemonNavTarget == nil`; Web replaces the entire page with a child `ContentListPage(mode = Pokemon(...))`
- Pokemon/Player tap callbacks are handled locally within each `ContentListPage` / `ContentListView` — they do not bubble up to parent
- **Format threading**: When navigating to a Pokemon from battle detail, the battle's `formatId` is injected at the boundary (Android: `BattleDetailSheetWrapper`, iOS: `BattleDetailSheet`, Web: `BattleDetailPanel`) by wrapping the 4-param `onPokemonClick` into a 5-param version that appends the format. The Pokemon content list then defaults to that format, with a format dropdown in the hero header allowing the user to switch.

## Key file locations — content list
- Android: `ContentListPage.kt`, `ContentListViewModel.kt`, `ContentListUiState.kt` (all in `composeApp/.../ui/contentlist/`)
- iOS: `ContentListView.swift`, `ContentListViewModel.swift`, `ContentListState.swift` (in `iosApp/iosApp/`)
- Web: `ContentListPage.kt`, `ContentListViewModel.kt`, `ContentListUiState.kt` (all in `webApp/.../ui/contentlist/`)

## Key file locations — web ViewModel store
- `webApp/.../ui/ViewModelStore.kt` — `ViewModelStore` class, `LocalViewModelStore`, `ProvideViewModelStore`, `rememberViewModel()`

## Key file locations — battle detail
- Android: `BattleDetailScreen.kt` (sheet + tabs), `TeamPreviewTab.kt`, `PokemonDetailCard.kt` (all in `composeApp/.../ui/battledetail/`)
- iOS: `BattleDetailSheet.swift` (sheet + tabs + TeamPreviewTab + PlayerTeamDetailSection), `PokemonDetailCard.swift` (in `iosApp/iosApp/`)
- Web: `BattleDetailPanel.kt` (inline panel + tabs), `TeamPreviewTab.kt`, `PokemonDetailCard.kt`, `ReplayTab.kt` (all in `webApp/.../ui/battledetail/`)
- Shared `BattleRepository`: `shared/.../data/BattleRepository.kt` (used by all platforms)
