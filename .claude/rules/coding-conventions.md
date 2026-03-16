---
description: Coding conventions and guidelines for all platforms (Kotlin, Swift, Compose, SwiftUI)
globs:
  - "**/*.kt"
  - "**/*.swift"
---

# Coding Conventions

## Compose (Android + Web)

- **`modifier` parameter ordering**: `modifier: Modifier = Modifier` must be the **first optional parameter** — immediately after all required parameters, before any other optional parameters.
- **Previews required**: All `@Composable` functions (Android) must include `@Preview` blocks. Use `PreviewAsyncImage` for any image that loads from a URL at runtime:
  - `PreviewAsyncImage(url, previewDrawable = R.drawable.preview_xxx, contentDescription, modifier)` — shows the drawable in `@Preview` mode (`LocalInspectionMode`), `AsyncImage` at runtime. Located in `composeApp/.../ui/components/PreviewAsyncImage.kt`.
  - Available preview assets: `preview_pokemon`, `preview_item`, `preview_tera`, `preview_type_1`, `preview_type_2`
- Auto-sizing text: Android uses custom `AutoSizeText` composable (Compose Multiplatform 1.10.0 lacks `TextDefaults.AutoSize`)

## SwiftUI (iOS)

- **Previews required**: All SwiftUI `View` structs must include `#Preview` blocks. Use `PreviewAsyncImage` for URL-loaded images:
  - `PreviewAsyncImage(url: url, previewAsset: "PreviewXxx")` — shows asset catalog image in Xcode previews (`isPreview` flag), `AsyncImage` at runtime. Located in `iosApp/iosApp/PreviewAsyncImage.swift`. The `isPreview` constant is defined in `PokemonAvatar.swift`.
  - Available preview assets: `PreviewPokemon`, `PreviewItem`, `PreviewTera`, `PreviewType1`, `PreviewType2`
- Auto-sizing text: iOS uses native `.minimumScaleFactor()`
- Xcode project uses `PBXFileSystemSynchronizedRootGroup` — new Swift files are auto-discovered, no pbxproj edits needed

## Shared Module Patterns

- DTOs in `network/model/`, domain models in `domain/model/`, UI models in `ui/model/`
- Network mappers are extension functions (`Dto.toDomain()`); UI mappers are singleton objects (`UiMapper.map()` / `UiMapper.mapList()`)
- Display names computed via properties (e.g., `Ability.displayName` converts camelCase to Title Case)
- Package: `com.arcvgc.app`

## DI Patterns

- **Android**: Hilt `@Inject` + `@HiltViewModel`. Thin Hilt-injectable wrappers in `composeApp/.../data/repository/` delegate to shared repos.
- **iOS**: Manual `DependencyContainer` with `@StateObject` ViewModels. Swift stores (`FavoritesStore`, `SettingsStore`, `AppConfigStore`) wrap shared repos as `@MainActor ObservableObject`.
- **Web**: Manual `DependencyContainer` singleton with `rememberViewModel(key) { ViewModel(deps...) }`. ViewModels are cached in a `ViewModelStore` (provided via `ProvideViewModelStore` at the app root) so they survive tab switches and back navigation. Uses shared repos directly with try/catch (no Result<T> wrapper).

## Pagination

- Pagination triggers when user is within 5 items of the list end.
- **`paginate()` must guard against `loadingSections.isNotEmpty()`** — see `docs/search.md` "Sort Toggle & Section Loading" section.

## Kotlin/Native Bridging Caveats (iOS)

- Kotlin `Int` becomes `Int32` in Swift via SKIE bridge; use `onEnum(of:)` for sealed class pattern matching
- `List<Int>` bridges to `[KotlinInt]` (not `[Int32]`), requiring `.map { KotlinInt(int:) }` / `.map { $0.int32Value }` conversions
- Generic Kotlin classes lose type parameters in Swift (e.g., `CatalogResult<T>.items` becomes `[Any]?`, requiring casts)
- Kotlin default parameter values do NOT bridge to Swift — all params must be passed explicitly
- **Exception bridging is unreliable**: Kotlin exceptions thrown from suspend functions may NOT be caught by Swift `do/catch`. **Never rely on Swift catching Kotlin exceptions.** Instead:
  - Handle errors on the Kotlin side before they cross the K/N boundary
  - Return nullable types or result types (e.g., `CatalogResult`) instead of throwing
  - For throwing methods that Android needs, add a non-throwing `OrNull` variant for iOS (e.g., `getMatchDetailOrNull`)
  - On the Swift side, always use `try?` (not `try` with `do/catch`) when calling Kotlin suspend functions
  - Report errors to Sentry via `captureException()` on the Kotlin side before returning

## Kotlin/Wasm Caveats (Web)

- `kotlinx.browser.localStorage` is NOT available in the shared module's wasmJsMain — use `@JsFun` external functions for JS interop instead
- `Dispatchers.IO` is not available on wasmJs — use `Dispatchers.Default` or inherit caller's dispatcher
- Web ViewModels use `collectAsState()` instead of `collectAsStateWithLifecycle()`
- Browser History API (`BrowserHistory.kt`): `@JsFun` wrappers for `pushState`/`history.go`. Navigation state (`navStack`, `searchOverlayParams`) is managed in `WebApp()` with `historyDepth` tracking and a `DisposableEffect` popstate listener. State variables use explicit `MutableState` objects so the listener closure reads current values.

## Web UI Differences from Android

- `NavigationRail` instead of `NavigationBar`; `BattleDetailPanel` inline instead of `ModalBottomSheet`
- `Dialog` composables instead of `ModalBottomSheet` for pickers
- `IconButton` refresh instead of `PullToRefreshBox`; no `BackHandler` — back buttons are visible UI elements
- `TeamPreviewTab` uses fixed 280.dp card width instead of `LocalConfiguration.current.screenWidthDp`

## ContentListItem Rendering

`ContentListPage` / `ContentListView` renders items heterogeneously via sealed class dispatch — `ContentListItem.Battle` -> `BattleCard`/`BattleCardView`, `ContentListItem.Pokemon` -> `PokemonListRow`/`SimplePokemonRow`, `ContentListItem.Player` -> `PlayerListRow`/player `HStack`. iOS uses SKIE `onEnum(of:)` for pattern matching.

## Pokemon Image Styling

Pokemon images use a consistent pattern: a smaller gray circle background (`surfaceVariant` / `systemGray5`) with the Pokemon sprite rendered larger and overlaying (not clipped by) the circle. Sizes vary by context:
- **BattleCard**: 70% circle / 100% sprite (relative to slot)
- **BattleDetail**: 100dp circle / 144dp sprite
- **SearchFilterCard**: 40dp circle / 56dp sprite
- **PokemonPickerSheet**: 46dp circle / 64dp sprite
- **PokemonHero**: 158dp circle / 227dp sprite
