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
- Pokemon/Player navigation routed to nav stacks via `onPokemonClick`/`onPlayerClick` callbacks (mobile: `navStack`, desktop: `desktopNavStack`). Fallback to per-instance recursive pattern when callbacks are not provided.

### Web ViewModel persistence (`ViewModelStore`)
- `ProvideViewModelStore` wraps the entire app content inside `Surface`, providing a `ViewModelStore` via `LocalViewModelStore`
- `rememberViewModel(key) { factory }` retrieves or creates ViewModels by key — ViewModels survive tab switches, back navigation, and recomposition because they live in the store, not in the composition tree
- Stable keys match the Android `hiltViewModel(key = ...)` pattern:
  - `"content_list_home"`, `"content_list_favorites_${contentType}"`, `"content_list_search_${params}"`, `"content_list_pokemon_${id}"`, `"content_list_player_${id}_${formatId}"`
  - `"search"` for `SearchViewModel`
  - `"battle_detail_${battleId}"` for `BattleDetailViewModel`
  - `"favorites"` for `FavoritesViewModel` (persists selected sub-tab)
- Key files: `ViewModelStore.kt` (store + composable helpers), all web ViewModels use `rememberViewModel()` instead of bare `remember()`

### Web mobile navigation stack (`MobileLayout`)
- `navStack: List<NavEntry>` is owned by `WebApp()` and passed to `MobileLayout` as a parameter
- `NavEntry` sealed class (shared by mobile and desktop): `BattleDetail(request)`, `Pokemon(id, name, ...)`, `Player(id, name, ...)`
- Supports arbitrary-depth drill-down: `battle → pokemon → battle → player → battle → ...`
- Each stack entry renders full-screen; "back" pops the top entry
- Each `Pokemon`/`Player` entry provides its own `LocalBattleOverlay` that pushes a new `BattleDetail` entry
- `LocalBattleOverlay` from the tab pages and search overlay uses `onReplaceNavStack` to atomically replace the navStack with a single `BattleDetail` entry (avoids async `historyGo` + sync `pushState` race conditions)
- Tab switching clears the entire stack
- Combined with `ViewModelStore`, navigating back preserves loaded data, scroll position, selected battle detail, and favorites sub-tab (no re-fetch)

### Web browser History API integration (`BrowserHistory.kt`)
- `@JsFun` wrappers for `history.pushState()` and `history.go()` in `BrowserHistory.kt`
- Browser back button pops one navigation level: `navStack` entries first (mobile), then `desktopNavStack` (desktop), then `searchOverlayParams`
- `WebApp()` tracks `historyDepth` (entries pushed) and `popStatesToIgnore` (skips popstate events from programmatic `historyGo` calls)
- History entries are pushed imperatively at each navigation site (not reactively via `LaunchedEffect`)
- Navigation callbacks (`handleSearch`, `handlePushEntry`, etc.) manage both app state and browser history together
- State variables use explicit `MutableState` objects so the `DisposableEffect` popstate listener reads current values
- Tab switching calls `historyGo(-historyDepth)` to clear all stale history entries
- `ContentListPage` accepts optional `onPokemonClick`/`onPlayerClick` callbacks — when provided, clicks route to those instead of internal `pokemonNavTarget`/`playerNavTarget` state. Used by both mobile and desktop layouts to push entries onto their respective nav stacks.
- Desktop uses a separate `desktopNavStack: List<NavEntry>` (Pokemon/Player only, no BattleDetail) managed in `WebApp()`. `DesktopLayout` renders the top entry, with callbacks wired to push/pop.
- New search and tab switching clear `desktopNavStack`
- **State restoration on back navigation:** `ContentListViewModel` persists `savedBattleId`, `savedScrollIndex`, `savedScrollOffset` — composition state (`selectedBattleId`, `LazyListState`) is initialized from the ViewModel and synced back via `LaunchedEffect`/`snapshotFlow`. All `remember` blocks are keyed on `viewModel` to re-initialize when different modes render at the same tree position. `FavoritesViewModel` (key `"favorites"`) persists the selected sub-tab index.
- **Not integrated:** tab switching, desktop battle detail selection (inline pane — not a navigation event)

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

## Deep Linking

All three platforms support deep links. Every page in the app is addressable via URL.

### URL Scheme

| Target | URL Pattern | Example |
|---|---|---|
| Home / Top | `/` | `arcvgc.com` |
| Battle detail (legacy) | `/battle/{id}` | `arcvgc.com/battle/42` |
| Pokemon battles | `/pokemon/{id}` | `arcvgc.com/pokemon/150` |
| Player battles | `/player/{name}` | `arcvgc.com/player/Wolfe%20Glick` |
| Favorites sub-tab | `/favorites/{type}` | `arcvgc.com/favorites/pokemon` |
| Search tab | `/search` | `arcvgc.com/search` |
| Search results | `/search?p=...&f=...&order=...` | see search params below |
| Settings | `/settings` | `arcvgc.com/settings` |

**Battle detail as query param:** Any root URL can have `?battle={id}` appended to open a battle detail pane/sheet alongside the root page. On desktop web, the root page renders in the left pane and the battle detail in the right pane. On mobile apps, the root page renders behind the battle detail sheet. On mobile web, the root is ignored (full-screen battle overlay). Examples:
- `/pokemon/150?battle=42` — Pokemon page with battle 42 open
- `/favorites/battles?battle=42` — Favorites with battle 42 open
- `/search?p=150&f=1&order=rating&battle=42` — Search results with battle 42 open
- `/?battle=42` — Home with battle 42 open
- `/battle/42` — Legacy format, equivalent to `/?battle=42`

**Search query parameters:** `p` (Pokemon IDs, comma-separated), `i` (item IDs per slot, `_` for none), `t` (tera type IDs per slot), `f` (format ID), `min`/`max` (rating), `unrated` (flag), `order` (rating/date), `start`/`end` (epoch millis), `player` (URL-encoded name). `encodeSearchPath()` and `parseDeepLink()` handle round-tripping.

### Shared module

- `DeepLink` data class (wraps `DeepLinkTarget` + optional `battleId`) returned by `parseDeepLink(path)` in `shared/.../domain/model/DeepLinkTarget.kt`
- `DeepLinkTarget` sealed class: `Home`, `Pokemon`, `Player`, `Favorites`, `Search`, `SearchTab`, `SettingsTab`
- `appendBattleParam(basePath, battleId)` appends `?battle=X` or `&battle=X` to any path
- `DeepLinkResolver` resolves targets to navigation data (fetches Pokemon/Player display info from API, looks up items/tera types/formats from catalog providers) in `shared/.../data/DeepLinkResolver.kt`

### Web

- `BrowserHistory.kt` includes `pushHistoryStateWithPath()`, `replaceHistoryStateWithPath()`, `getLocationPathAndSearch()` for URL-based navigation
- On page load, `WebApp()` reads `window.location.pathname + search`, parses it via `parseDeepLink()`, resolves via `DeepLinkResolver`, and sets initial navigation state. Shows a loading spinner during resolution.
- `battleId` from `DeepLink` is set generically for all root targets; mobile web pushes `NavEntry.BattleDetail` to `navStack`, desktop passes `initialBattleId` to `ContentListPage`
- URL mirroring: `ContentListPage` mirrors `modePath` + `appendBattleParam()` via `replaceHistoryStateWithPath`. Home + battle uses `/battle/{id}`; other roots use `?battle={id}`. Search URLs mirror via `encodeSearchPath()`. Favorites sub-tabs mirror via their `ContentListPage` `modePath`. Tab switches mirror for Search (`/search`) and Settings (`/settings`).
- Stale `navStack` entries from deep links are cleared on desktop via a `LaunchedEffect` (prevents popstate listener from popping mobile entries on desktop)
- `deepLinkBattleId` is cleared on tab switch and when a new desktop nav entry is pushed
- nginx SPA fallback (`try_files $uri $uri/ /index.html`) serves the app for all deep link paths
- `DependencyContainer` provides catalog repos to `DeepLinkResolver` for search filter display data
- `devServer.js` has `historyApiFallback: true` for local dev; `index.html` has `<base href="/">` for correct asset loading on deep link paths

### Android

- Intent filters in `AndroidManifest.xml` for `https://arcvgc.com/battle/*`, `/pokemon/*`, `/player/*`, `/favorites/*`, `/search`, `/settings`
- `MainActivity` parses `intent.data` (path + query) via `parseDeepLink()` and passes the `DeepLink` to `App(deepLink:)`
- `deepLinkBattleId` initialized synchronously from `deepLink?.battleId`; cleared on manual tab switch (`NavigationBarItem.onClick`)
- `initialBattleId` threaded to all `ContentListPage` instances: Home tab, `FavoritesPage` (with `battleIdForTab` sub-tab scoping), search overlay, and deep link overlay (Pokemon/Player)
- `ContentListPage` applies `initialBattleId` via `LaunchedEffect(initialBattleId)` to handle async arrival
- Pokemon/Player deep links render as overlays via `deepLinkOverlay` state in `App.kt`
- Search deep links set `searchOverlayParams` which renders `ContentListPage` in Search mode
- `DeepLinkResolver` provided via Hilt in `NetworkModule` with catalog providers (item, tera type, format) for search filter resolution
- App Links verified via `.well-known/assetlinks.json` (debug key only; production key to be added after Play Store release)
- **Known limitation**: deep links only processed on cold start (`onCreate`); `onNewIntent` not yet handled for warm-start

### iOS

- Custom URL scheme `arcvgc://` registered in `Info.plist` (`CFBundleURLTypes`)
- Universal Links via Associated Domains entitlement (`applinks:arcvgc.com`) in `iosApp.entitlements` + server-side `apple-app-site-association`
- `iOSApp.swift` handles `.onOpenURL` for both custom scheme and universal links — parses path + query and calls `DependencyContainer.handleDeepLink(deepLink:)`
- `DependencyContainer` resolves the target asynchronously and publishes via `@Published pendingDeepLink` + `@Published pendingBattleId`. Catalog providers wired for search resolution.
- `ContentView` observes `pendingDeepLink` via `.onChange`. Resets previous navigation state before applying new deep link. Pokemon/Player use a single `DeepLinkNavTarget` enum with one `navigationDestination(isPresented:)` to avoid dismiss/present race conditions. `.id(deepLinkNavTarget)` forces view recreation when target changes.
- `deepLinkBattleId` cleared via `DispatchQueue.main.async` after SwiftUI processes the state update, preventing re-trigger on sheet dismissal
- Home `ContentListView` keyed with `.id(deepLinkBattleId ?? 0)` to force recreation on battle deep link arrival
- `initialBattleId` threaded to Home, Pokemon/Player destinations, and `FavoritesView` (with `battleIdForTab` sub-tab scoping)
- Search deep links set `deepLinkSearchParams` passed to `SearchView(initialSearchParams:)`
- `SearchView` keyed with `.id(deepLinkSearchParams?.hashValue)` and `FavoritesView` keyed with `.id(deepLinkFavoritesSubTab)` for re-init on subsequent deep links
