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
- `NavigationRail` on the left (Top / Search / Favorites / Settings) instead of bottom `NavigationBar`
- `Row { NavigationRail { ... }; Box { content } }` layout
- `searchOverlayParams: SearchParams?` replaces content area with search results; cleared on tab switch
- Coil `setSingletonImageLoaderFactory` with Ktor network fetcher for image loading
- Pokemon/Player navigation managed per-`ContentListPage` instance (same recursive pattern as other platforms)

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
- Web: `BattleDetailPanel` renders inline in a right pane (`weight(0.6f)`) of a master-detail `Row` layout. List pane takes `weight(0.4f)`. Selected battle highlighted with `primaryContainer` background. No sheet/overlay.
- Sheet is suppressed (not dismissed) during Pokemon navigation: Android gates composition on `pokemonNavTarget == null`; iOS gates the sheet binding getter on `pokemonNavTarget == nil`; Web replaces the entire page with a child `ContentListPage(mode = Pokemon(...))`
- Pokemon/Player tap callbacks are handled locally within each `ContentListPage` / `ContentListView` — they do not bubble up to parent
- **Format threading**: When navigating to a Pokemon from battle detail, the battle's `formatId` is injected at the boundary (Android: `BattleDetailSheetWrapper`, iOS: `BattleDetailSheet`, Web: `BattleDetailPanel`) by wrapping the 4-param `onPokemonClick` into a 5-param version that appends the format. The Pokemon content list then defaults to that format, with a format dropdown in the hero header allowing the user to switch.

## Key file locations — content list
- Android: `ContentListPage.kt`, `ContentListViewModel.kt`, `ContentListUiState.kt` (all in `composeApp/.../ui/contentlist/`)
- iOS: `ContentListView.swift`, `ContentListViewModel.swift`, `ContentListState.swift` (in `iosApp/iosApp/`)
- Web: `ContentListPage.kt`, `ContentListViewModel.kt`, `ContentListUiState.kt` (all in `webApp/.../ui/contentlist/`)

## Key file locations — battle detail
- Android: `BattleDetailScreen.kt` (sheet + tabs), `TeamPreviewTab.kt`, `PokemonDetailCard.kt` (all in `composeApp/.../ui/battledetail/`)
- iOS: `BattleDetailSheet.swift` (sheet + tabs + TeamPreviewTab + PlayerTeamDetailSection), `PokemonDetailCard.swift` (in `iosApp/iosApp/`)
- Web: `BattleDetailPanel.kt` (inline panel + tabs), `TeamPreviewTab.kt`, `PokemonDetailCard.kt`, `ReplayTab.kt` (all in `webApp/.../ui/battledetail/`)
- Shared `BattleRepository`: `shared/.../data/BattleRepository.kt` (used by all platforms)
