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
shared/src/androidMain/   — Android HTTP engine (OkHttp), platform storage (SharedPreferences)
shared/src/iosMain/       — iOS HTTP engine (Darwin), platform storage (NSUserDefaults)
shared/src/wasmJsMain/    — Web HTTP engine (Js), platform storage (localStorage via @JsFun)
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
- **Domain layer** (`shared/.../domain/model/`): Pure Kotlin data classes — `MatchPreview`, `MatchDetail`, `PlayerDetail`, `PokemonListItem`, `SearchFilterSlot`, `AppConfig`, `Format`, etc.
- **UI layer** (`shared/.../ui/`): Platform-agnostic UI models, mappers (including `TimeFormatter` for shared time formatting), and `SearchStateReducer` (pure state reducer for search filter mutations used by all platforms); platform-specific screens in `composeApp/`, `iosApp/`, `webApp/`
- **Data layer** (`shared/.../data/`): Shared business logic used by all platforms
  - `BattleRepository` — Match data (getMatches, searchMatches, getMatchDetail, getMatchesByIds, getPokemonByIds, getPlayersByNames). Throws exceptions on error. Android wraps in `Result<T>` via a thin adapter; iOS uses directly via SKIE `async throws` bridge.
  - `FavoritesRepository` — Favorites toggle/check with `StateFlow` state, delegates to `FavoritesStorage` (expect/actual)
  - `SettingsRepository` — App settings with `StateFlow` per setting + combined `settingItems` flow. Delegates to `SettingsStorage` (expect/actual)
  - `AppConfigRepository` — Remote config (default format, min app versions, catalog version). Caches to `AppConfigStorage` (expect/actual). Signals `catalogVersionChanged` when catalog cache needs clearing.
  - `CatalogCache` + `CatalogCacheStorage` — TTL-based caching for catalog data (Pokemon, Items, Tera Types, Formats)
  - `PokemonCatalogRepository`, `ItemCatalogRepository`, `TeraTypeCatalogRepository`, `FormatCatalogRepository` — Singleton repos with `StateFlow<CatalogState<T>>` and `reload()`
  - `MatchesResult`, `CatalogState<T>` — Shared data classes
- **Android repositories** (`composeApp/.../data/repository/`): Thin Hilt-injectable wrappers delegating to shared repos
- Error handling uses `NetworkResult<T>` sealed class (`Success`/`Error`) at the API layer. Shared `BattleRepository` converts to thrown exceptions; shared `CatalogLoader` converts to `CatalogResult<T>`.

## API

Base URL: `https://arcvgc.com`

| Endpoint | Purpose |
|---|---|
| `GET /api/v0/config/` | App config (default format, min versions, catalog version) |
| `GET /api/v0/matches/?limit=50&page=1` | Paginated battle list |
| `GET /api/v0/matches/{id}` | Battle detail |
| `GET /api/v0/pokemon/?exclude_illegal=true&limit=50&page=N` | Pokemon catalog |
| `GET /api/v0/items/?limit=50&page=N` | Item catalog |
| `GET /api/v0/types/tera?limit=50&page=N` | Tera type catalog |
| `GET /api/v0/formats?limit=50&page=N` | Format catalog |
| `POST /api/v0/matches/search` | Search matches (body: `SearchRequestDto`) |

All catalog endpoints return `{ success, data, pagination }` using `PaginationDto`. Constants in `shared/.../network/ApiConstants.kt`.

## Key Screens

- **ContentListPage** — Generic paginated list page for Home, Search results, per-Pokemon battles, Favorites sub-tabs. Sort toggle in Search/Pokemon/Player modes.
- **BattleDetailScreen** — Two tabs: "Team Preview" and "Replay"
- **SearchPage** — Pokemon filter UI with format (pre-selected from config), rating, date, player name, sort order
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
```

## Detailed Documentation

Feature-specific architecture docs. **You MUST read the relevant doc(s) before starting any task that touches the corresponding domain area.** These docs contain critical implementation details, invariants, and cross-platform patterns that are not obvious from the code alone.

| Doc | Contents |
|---|---|
| [`docs/content-list.md`](docs/content-list.md) | ContentListPage modes, headers, page 1 vs 2+ content structure, section loading, format selection, favorites auto-refresh, navigation suppression |
| [`docs/config.md`](docs/config.md) | App Config — remote config, default format, forced upgrade, catalog versioning |
| [`docs/search.md`](docs/search.md) | Search/Filter architecture, catalog repos, sort toggle, section loading, pagination guard |
| [`docs/navigation.md`](docs/navigation.md) | Tab navigation, Pokemon/Player drill-down, battle detail presentation |
| [`docs/favorites.md`](docs/favorites.md) | Favorites architecture — shared + per-platform |
| [`docs/settings.md`](docs/settings.md) | Settings architecture — adding new settings |
| [`docs/dark-mode.md`](docs/dark-mode.md) | Theme implementation per platform |
| [`docs/sentry.md`](docs/sentry.md) | Crash reporting setup |
| [`docs/deployment.md`](docs/deployment.md) | Web deployment, nginx, CORS, image URL handling |
| [`docs/legal.md`](docs/legal.md) | Legal documents, key claims, hosted URLs |

Coding conventions and quality rules are in `.claude/rules/` (automatically loaded).

## Documentation Maintenance

After completing any task that modifies source code, review the docs index above and determine whether any referenced docs or CLAUDE.md itself need updating to reflect the changes. Update them if so. The `Stop` hook in `.claude/hooks/check-docs.sh` will remind you when source files have been modified.
