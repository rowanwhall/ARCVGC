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
- **Gradient toolbar**: All Android pages with a back-navigation toolbar must use `GradientToolbarScaffold` (`composeApp/.../ui/components/GradientToolbarScaffold.kt`) instead of `Scaffold` + `TopAppBar`. It renders a transparent toolbar with a vertical gradient overlay so content scrolls underneath. Callers receive a `topPadding: Dp` in the content lambda — apply it as content padding *inside* the scrollable container (after `.verticalScroll()` / in `LazyColumn` `contentPadding`) so the content starts below the toolbar but scrolls under it.
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

## Deep Linking

Every page must have a deep link URL. `parseDeepLink()` returns `DeepLink(target, battleId?)` — the target identifies the root page, and `battleId` is an optional `?battle=ID` query param that opens a battle detail pane/sheet alongside the root. When adding a new page or a new parameter to an existing deep-linked page:

1. **Shared parser**: Add a `DeepLinkTarget` case in `shared/.../domain/model/DeepLinkTarget.kt` and update `parseDeepLink()`. If the page has parameters, add an encoder function (like `encodeSearchPath()`). The `battle` query param is extracted automatically for all URLs.
2. **Shared resolver**: Add a `ResolvedLink` case in `shared/.../data/DeepLinkResolver.kt` and handle it in `resolve()`. If the page needs display data resolved from IDs, add the resolution logic.
3. **Web URL mirroring**: Ensure navigating to the page updates the browser URL bar. For `ContentListPage` modes, add the path to `modePath` in `webApp/.../ui/contentlist/ContentListPage.kt`. For tab-level pages, update `handleTabSelected` in `WebApp.kt`. For overlay pages, update the handler that opens the overlay. Battle detail selection is mirrored via `appendBattleParam(modePath, selectedBattleId)`.
4. **Web deep link loading**: Handle the new target in the `LaunchedEffect(Unit)` deep link handler in `WebApp.kt`. Pass `initialBattleId` to any new `ContentListPage` or `FavoritesPage` in DesktopLayout.
5. **Android**: Handle the new resolved link in `App.kt`'s `LaunchedEffect(deepLink)`. Thread `initialBattleId` to any new `ContentListPage` or `FavoritesPage`. Add path prefix to `AndroidManifest.xml` intent filters if needed.
6. **iOS**: Handle the new case in `DependencyContainer.handleDeepLink()` and `ContentView.swift`'s `onChange(of: pendingDeepLink)`. Thread `initialBattleId` to any new `ContentListView` or `FavoritesView`. If the new page uses `@State`, add `.id()` keyed on the deep link param to ensure re-init on subsequent deep links.
7. **Tests**: Add parser tests in `DeepLinkTargetTest.kt`.
8. **Docs**: Update the URL scheme table in `docs/navigation.md`.

Key files: `DeepLinkTarget.kt` (parser + `DeepLink` wrapper + `appendBattleParam()`), `DeepLinkResolver.kt` (resolver), `WebApp.kt` (web handling + URL mirroring), `ContentListPage.kt` web (`modePath`), `App.kt` (Android), `DependencyContainer.swift` + `ContentView.swift` (iOS).

## Pokemon Image Styling

Pokemon images use a consistent pattern: a smaller gray circle background (`surfaceVariant` / `systemGray5`) with the Pokemon sprite rendered larger and overlaying (not clipped by) the circle. Sizes vary by context:
- **BattleCard**: 70% circle / 100% sprite (relative to slot)
- **BattleDetail**: 100dp circle / 144dp sprite
- **SearchFilterCard**: 40dp circle / 56dp sprite
- **PokemonPickerSheet**: 46dp circle / 64dp sprite
- **PokemonHero**: 132dp circle / 184dp sprite
