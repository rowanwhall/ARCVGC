# Showdown26

Kotlin Multiplatform (KMP) app for browsing Pokemon Showdown battle replays. Targets Android (Jetpack Compose), iOS (SwiftUI), and Web (Compose for wasmJs) with a shared core.

## Tech Stack

- **Kotlin** 2.3.0, **Compose Multiplatform** 1.10.0 (shared module has Compose deps for cross-platform UI components)
- **Ktor** 3.4.0 (OkHttp on Android, Darwin on iOS, Js on Web)
- **kotlinx-serialization** 1.10.0, **kotlinx-coroutines** 1.10.2
- **Coil 3** for image loading (shared module for Android/Web components; native `AsyncImage` on iOS)
- **Hilt** 2.59.1 for Android DI
- **Sentry KMP** 0.24.0 for crash reporting (Android/iOS via KMP SDK, Web via JS Browser SDK + `@JsFun` bridge)
- **SKIE** 0.10.9 for Kotlin/Swift sealed class interop
- AGP 9.0.0, minSdk 24, targetSdk 36

## Project Structure

```
shared/src/commonMain/    — Shared Kotlin code (network, domain, data, UI models, mappers, ViewModel logic, shared Compose components)
shared/src/androidMain/   — Android HTTP engine (OkHttp), platform storage (SharedPreferences)
shared/src/iosMain/       — iOS HTTP engine (Darwin), platform storage (NSUserDefaults)
shared/src/wasmJsMain/    — Web HTTP engine (Js), platform storage (localStorage via @JsFun)
composeApp/src/androidMain/ — Android app (Compose UI, Hilt DI, ViewModels)
iosApp/                   — iOS app (SwiftUI views, ViewModels)
webApp/src/wasmJsMain/    — Web app (Compose for wasmJs, manual DI, desktop-optimized layouts, browser History API)
```

## Architecture

Clean architecture with three layers and explicit mappers between them:

```
Network DTOs  →  Domain Models  →  UI Models  →  Screen
  (Ktor)        (toDomain())      (toUiModel())   (Compose/SwiftUI)
```

- **Network layer** (`shared/.../network/`): `ApiService` with Ktor, DTO models, `ApiConstants` for base URL/endpoints, `CatalogLoader` (generic pagination), `SearchRequestMapper` (maps team1/team2 filters to `SearchTeamDto`; legacy top-level `pokemon` field is deprecated and sent empty)
- **Domain layer** (`shared/.../domain/model/`): Pure Kotlin data classes — `MatchPreview`, `MatchDetail`, `PlayerDetail`, `PokemonListItem`, `SearchFilterSlot`, `AppConfig`, `Format`, `MatchSet`, `SetDetail`, `SetMatchInfo`, `SetPlayer`, `SetPlayerDetail`, etc.
- **UI layer** (`shared/.../ui/`): Platform-agnostic UI models, mappers (including `TimeFormatter` for shared time formatting), `SearchLogic` (scope-injected shared ViewModel logic for search — owns `MutableStateFlow<SearchUiState>`, delegates mutations to `SearchStateReducer`), `SearchStateReducer` (pure state reducer for search filter mutations), `ContentListLogic` (scope-injected shared ViewModel logic for content list — handles data fetching, pagination, sort/format toggling, favorites observation), `InfoContentProvider` (key-based registry for info dialog content), design tokens (`shared/.../ui/tokens/AppTokens.kt` — shared dimension, typography, and alpha constants used by Android and Web; iOS mirrors in `iosApp/iosApp/AppTokens.swift`), and shared Compose components (`shared/.../ui/components/` — `PreviewAsyncImage`, `PokemonAvatar`/`FillPokemonAvatar`, `TypeIconRow`, `SimplePokemonRow`, `BattleCard`, `VsDivider`, `EmptyView`, `ErrorView`, `InfoButton`, `AutoSizeText`, `GradientToolbarScaffold`; `shared/.../ui/contentlist/` — `SectionHeader`, `SortToggleButton`, `PlayerListRow`, `FormatDropdown`, `PokemonNavTarget`, `PlayerNavTarget`); platform-specific screens in `composeApp/`, `iosApp/`, `webApp/`
- **Data layer** (`shared/.../data/`): Shared business logic used by all platforms
  - `BattleRepository` (implements `BattleRepositoryApi`) — Match data (getMatches, searchMatches, getMatchDetail, getMatchesByIds, getPokemonProfile, getPlayerProfile, getPlayersByNames, getFormatDetail). Throws exceptions on error. Android wraps in `Result<T>` via a thin adapter; iOS uses directly via SKIE `async throws` bridge. `ContentListLogic` depends on the `BattleRepositoryApi` interface for testability.
  - `FavoritesRepository` — Favorites toggle/check with `StateFlow` state, delegates to `FavoritesStorage` (expect/actual)
  - `SettingsRepository` — App settings with `StateFlow` per setting + combined `settingItems` flow. Delegates to `SettingsStorage` (expect/actual)
  - `AppConfigRepository` — Remote config (default format, min app versions, catalog version). Caches to `AppConfigStorage` (expect/actual). Signals `catalogVersionChanged` when catalog cache needs clearing.
  - `CatalogCache` + `CatalogCacheStorage` — TTL-based caching for catalog data (Pokemon, Items, Tera Types, Formats, Abilities)
  - `PokemonCatalogRepository`, `ItemCatalogRepository`, `TeraTypeCatalogRepository`, `FormatCatalogRepository`, `AbilityCatalogRepository` — Singleton repos with `StateFlow<CatalogState<T>>` and `reload()`
  - `MatchesResult`, `CatalogState<T>` — Shared data classes
- **Android repositories** (`composeApp/.../data/repository/`): Thin Hilt-injectable wrappers delegating to shared repos
- Error handling uses `NetworkResult<T>` sealed class (`Success`/`Error`) at the API layer. Shared `BattleRepository` converts to thrown exceptions; shared `CatalogLoader` converts to `CatalogResult<T>`.

## API

Base URL: `https://arcvgc.com`

| Endpoint | Purpose |
|---|---|
| `GET /api/v1/config/` | App config (default format, min versions, catalog version) |
| `GET /api/v1/matches/?limit=50&page=1` | Paginated battle list (supports `order_by`, `rated_only`, `format_id`) |
| `GET /api/v1/matches/{id}` | Battle detail |
| `GET /api/v1/pokemon/?exclude_illegal=true&limit=50&page=N` | Pokemon catalog |
| `GET /api/v1/pokemon/{id}?format_id=N` | Pokemon profile (stats, top teammates/items/moves/abilities/tera types) |
| `GET /api/v1/items/?limit=50&page=N` | Item catalog |
| `GET /api/v1/abilities/?limit=50&page=N` | Ability catalog |
| `GET /api/v1/types/tera?limit=50&page=N` | Tera type catalog |
| `GET /api/v1/formats?limit=50&page=N` | Format catalog |
| `GET /api/v1/formats/{id}?top_pokemon_count=N` | Format detail with top usage Pokemon |
| `GET /api/v1/players/{id}?format_id=N` | Player profile (optionally scoped to format) |
| `GET /api/v1/sets/?limit=20&page=1` | Paginated set (Bo3) list (supports `order_by`, `complete_only`, `rated_only`, `format_id`) |
| `GET /api/v1/sets/{id}` | Set detail with player teams |
| `POST /api/v1/matches/search` | Search matches (body: `SearchRequestDto` — supports `ability_id`, `player_id`, `set_id`, `team1`/`team2`) |

All catalog endpoints return `{ success, data, pagination }` using `PaginationDto` (`page`, `items_per_page`, `has_next`). Constants in `shared/.../network/ApiConstants.kt`.

## Key Screens

- **ContentListPage** — Generic paginated list page for Home, Search results, per-Pokemon battles, Favorites sub-tabs. Sort toggle in Search/Pokemon/Player modes.
- **BattleDetailScreen** — Full-screen battle detail page with format name + rating header (shows "Unrated" with info icon when null), "Game N" replay buttons with info icon, and team preview cards
- **SearchPage** — Team-based Pokemon filter UI with team1/team2 support. Single-team mode shows full-width cards with inline Item/Tera buttons; two-team mode shows compact side-by-side cards with stacked sub-filter badge icons and a MoreVert dropdown menu. Includes format (pre-selected from config), rating, date, player name, sort order. Search results show "vs" divider between team1 and team2 Pokemon chips in the header
- **FavoritesPage** — Three sub-tabs: Battles, Pokemon, Players
- **SettingsPage** — Dark Mode, Theme Color, Winner Highlight, Clear Favorites, Invalidate Cache, Privacy Policy, Terms of Service
- **ForceUpgradeOverlay** (Android) / **ForceUpgradeView** (iOS) — Blocks app when `minAppVersion > currentVersion`

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

# Web — clean build (required when wasmJs caching causes runtime errors)
# If you see "WebAssembly.instantiate(): Import ... function import requires a callable"
# or similar LinkError at runtime, the browser/webpack is loading stale .wasm artifacts.
# Fix: clean the webApp module before rebuilding.
./gradlew :webApp:clean :webApp:wasmJsBrowserDevelopmentRun
```

## Detailed Documentation

Feature-specific architecture docs. **You MUST read the relevant doc(s) before starting any task that touches the corresponding domain area.** These docs contain critical implementation details, invariants, and cross-platform patterns that are not obvious from the code alone.

| Doc | Contents |
|---|---|
| [`docs/content-list.md`](docs/content-list.md) | ContentListPage modes, headers, page 1 vs 2+ content structure, section loading, format selection, favorites auto-refresh, navigation suppression |
| [`docs/config.md`](docs/config.md) | App Config — remote config, default format, forced upgrade, catalog versioning |
| [`docs/search.md`](docs/search.md) | Search/Filter architecture, catalog repos, sort toggle, section loading, pagination guard |
| [`docs/navigation.md`](docs/navigation.md) | Tab navigation, Pokemon/Player drill-down, battle detail presentation, deep linking |
| [`docs/favorites.md`](docs/favorites.md) | Favorites architecture — shared + per-platform |
| [`docs/settings.md`](docs/settings.md) | Settings architecture — adding new settings |
| [`docs/dark-mode.md`](docs/dark-mode.md) | Theme implementation per platform |
| [`docs/sentry.md`](docs/sentry.md) | Crash reporting setup |
| [`docs/deployment.md`](docs/deployment.md) | Web deployment, nginx, CORS, image URL handling |
| [`docs/info-dialogs.md`](docs/info-dialogs.md) | Info dialog system — shared content registry, per-platform components, adding new info content |
| [`docs/legal.md`](docs/legal.md) | Legal documents, key claims, hosted URLs |

Coding conventions and quality rules are in `.claude/rules/` (automatically loaded).

## Documentation Maintenance

After completing any task that modifies source code, review the docs index above and determine whether any referenced docs or CLAUDE.md itself need updating to reflect the changes. Update them if so. The `Stop` hook in `.claude/hooks/check-docs.sh` will remind you when source files have been modified.
