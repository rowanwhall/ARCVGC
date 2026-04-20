---
description: Coding conventions and guidelines for all platforms (Kotlin, Swift, Compose, SwiftUI)
globs:
  - "**/*.kt"
  - "**/*.swift"
---

# Coding Conventions

## Design Tokens

When adding or modifying UI code, use `AppTokens` for shared dimension, typography, and alpha values instead of hardcoding. This keeps Android, Web, and iOS visually aligned.

- **Kotlin (Android + Web)**: Import individual tokens directly — e.g., `import com.arcvgc.app.ui.tokens.AppTokens.CardCornerRadius` — then use `CardCornerRadius` unqualified. Do **not** import the `AppTokens` object itself and qualify every usage.
- **Swift (iOS)**: Use `AppTokens.cardCornerRadius`, `AppTokens.standardBorderWidth`, etc. from `iosApp/iosApp/AppTokens.swift` (Swift does not support member imports, so qualified access is expected).
- When adding a new token, add it to both `AppTokens.kt` and `AppTokens.swift` with matching names (PascalCase in Kotlin, camelCase in Swift).
- Only tokenize values that are reused across multiple files with the same purpose. One-off values stay inline.
- **`BrandFontFamily`** is a `@Composable` getter (not a plain `val`) because Compose Resources `Font()` requires a composable context. Use it like any other token but only from `@Composable` functions.

## Branding Font (Orbitron)

The app uses **Orbitron** (variable weight, OFL license) as its brand font for "ARC" text.

- **Kotlin (Android + Web)**: Bundled as a Compose Resource at `shared/src/commonMain/composeResources/font/orbitron_bold.ttf`. Access via `AppTokens.BrandFontFamily`.
- **Swift (iOS)**: Bundled at `iosApp/iosApp/Fonts/Orbitron-Bold.ttf`, registered in `Info.plist` as `Orbitron-Bold.ttf` (note: `PBXFileSystemSynchronizedRootGroup` flattens the `Fonts/` subdirectory in the app bundle, so the plist path omits the folder). Use `.custom("Orbitron-Regular", size:)` with `.fontWeight()` modifier for weight variations.
- **Web font weight**: Browser font rendering is thinner than native. Use `FontWeight.SemiBold` on web where Android/iOS use `FontWeight.Medium` to achieve visual parity.

## Compose (Android + Web)

- **`modifier` parameter ordering**: `modifier: Modifier = Modifier` must be the **first optional parameter** — immediately after all required parameters, before any other optional parameters.
- **Previews required**: All `@Composable` functions (Android) must include `@Preview` blocks. Use `PreviewAsyncImage` for any image that loads from a URL at runtime:
  - `PreviewAsyncImage(url, previewDrawable = Res.drawable.preview_xxx, contentDescription, modifier)` — shows the Compose Resource drawable in `@Preview` mode (`LocalInspectionMode`), `AsyncImage` at runtime. Located in `shared/.../ui/components/PreviewAsyncImage.kt`. Takes `DrawableResource` (from Compose Resources), not Android `@DrawableRes Int`.
  - Preview drawables are in `shared/src/commonMain/composeResources/drawable/`. Import via `com.arcvgc.app.shared.Res` and `com.arcvgc.app.shared.preview_*`.
  - Available preview assets: `preview_pokemon`, `preview_item`, `preview_tera`, `preview_type_1`, `preview_type_2`
- **Gradient toolbar**: All Android and Web pages with a back-navigation toolbar must use the gradient toolbar components from `shared/.../ui/components/GradientToolbarScaffold.kt` instead of `Scaffold` + `TopAppBar`. Two composables are available:
  - **`GradientToolbarScaffold`** (wrapper pattern): Wraps content with a solid background + gradient toolbar overlay. Content goes in the lambda and receives `topPadding: Dp` — apply it *inside* the scrollable container. Used by `BattleDetailScreen`/`BattleDetailPanel`.
  - **`GradientToolbar`** (overlay pattern): Just the gradient + TopAppBar, no background or content slot. Render as a sibling *after* content in a `Box` so it draws on top. Content must compute its own toolbar spacing using `GradientToolbarHeight`. Used by `ContentListPage`.
  - On Android, pass `statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()` to `GradientToolbarScaffold` for full-screen pages; on web, use the default (`0.dp`).
- Auto-sizing text: Shared `AutoSizeText` composable in `shared/.../ui/components/AutoSizeText.kt` (Compose Multiplatform 1.10.0 lacks `TextDefaults.AutoSize`). Android `@Preview` in `composeApp/.../ui/components/SharedComponentPreviews.kt`.

## SwiftUI (iOS)

- **Previews required**: All SwiftUI `View` structs must include `#Preview` blocks. Use `PreviewAsyncImage` for URL-loaded images:
  - `PreviewAsyncImage(url: url, previewAsset: "PreviewXxx")` — shows asset catalog image in Xcode previews (`isPreview` flag), `AsyncImage` at runtime. Located in `iosApp/iosApp/PreviewAsyncImage.swift`. The `isPreview` constant is defined in `PokemonAvatar.swift`.
  - Available preview assets: `PreviewPokemon`, `PreviewItem`, `PreviewTera`, `PreviewType1`, `PreviewType2`
- Auto-sizing text: iOS uses native `.minimumScaleFactor()`
- Xcode project uses `PBXFileSystemSynchronizedRootGroup` — new Swift files are auto-discovered, no pbxproj edits needed
- **iPad responsive layouts**: Use `@Environment(\.horizontalSizeClass)` to detect `.regular` (iPad) vs `.compact` (iPhone). Apply layout changes at the component level. Examples: `ContentListView` Pokemon grid (6 cols iPad / 3 cols iPhone), `PlayerTeamDetailSection` in `BattleDetailSheet` (centered grid iPad / horizontal carousel iPhone), `SearchFilterCard` (inline buttons iPad / context menu iPhone). Android and Web use analogous `LocalWindowSizeClass.current == WindowSizeClass.Compact/Expanded`.
- **Android tablet responsive layouts**: Use `LocalWindowSizeClass.current` (`WindowSizeClass.Compact` vs `Expanded`) from `composeApp/.../ui/WindowSizeClass.kt`. Provided at the app root in `App.kt` via `BoxWithConstraints` with a 600dp breakpoint (same as web). Examples: `ContentListItemRow` Pokemon grid (6 cols tablet / 3 cols phone), `PlayerTeamSection` in `BattleDetailScreen` (centered grid tablet / horizontal carousel phone), `SearchFilterCard` (inline buttons tablet / context menu phone).

## Shared Module Patterns

- DTOs in `network/model/`, domain models in `domain/model/`, UI models in `ui/model/`
- Network mappers are extension functions (`Dto.toDomain()`); UI mappers are singleton objects (`UiMapper.map()` / `UiMapper.mapList()`)
- Display names computed via properties (e.g., `Ability.displayName` converts camelCase to Title Case)
- Package: `com.arcvgc.app`
- **Shared Compose components** (`shared/.../ui/components/`): `PreviewAsyncImage`, `PokemonAvatar`/`FillPokemonAvatar`/`PokeballCircle`, `TypeIconRow`/`TypeInfo`, `SimplePokemonRow`, `BattleCard`, `VsDivider`, `EmptyView`, `ErrorView`, `InfoButton`, `AutoSizeText`, `GradientToolbarScaffold`/`GradientToolbar`, `SettingsSectionHeader`/`SettingsSectionCard`. Android files in `composeApp/.../ui/components/` contain only `@Preview` wrappers for these (`SharedComponentPreviews.kt` for most, `BattleCardPreview.kt` for BattleCard). When adding a new shared component, place the composable in `shared/.../ui/components/` and add a `@Preview`-only file in `composeApp/.../ui/components/` (use a distinct filename to avoid JVM class name collisions with the shared module).
- **Shared content list components** (`shared/.../ui/contentlist/`): `SectionHeader`, `SortToggleButton`, `PlayerListRow`, `FormatDropdown`, `PokemonNavTarget`, `PlayerNavTarget`, `PAGINATION_THRESHOLD`, `findBattle()`. Platform-specific content list components (e.g., `PokemonListRow`) remain in `composeApp/` and `webApp/`.
- **Compose Resources**: Preview drawable PNGs in `shared/src/commonMain/composeResources/drawable/`. Generated `Res` class is public at `com.arcvgc.app.shared.Res`. Import accessors via `com.arcvgc.app.shared.preview_*`.

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

## Web ContentListPage File Organization

The web `ContentListPage` is split across five files in `webApp/.../ui/contentlist/`:

| File | Responsibility | When to edit |
|------|---------------|-------------|
| `ContentListPage.kt` | Page orchestration — navigation, state hoisting, master-detail layout, `buildCallbacks()` factory, URL mirroring | Adding navigation targets, changing compact/expanded branching, modifying battle detail pane behavior |
| `ContentListContent.kt` | Grid builder body, section emission, `emitPageHeader`/`emitFormatSelectorItem`/`emitSearchFieldItem` subscopes, layout helpers (`CenteredItem`, `SectionGroupLayout`, `SectionContentAlignedHeader`) | Adding new header types, new section rendering, new grid item types |
| `ContentListContentParams.kt` | `ContentListCallbacks`, `ContentListFormatState`, `ContentListGridConfig` data classes | Adding new callbacks or format/grid state that `ContentListContent` needs |
| `ContentListLayout.kt` | Pure layout math + dp constants — battle card sizing, top pokemon tiles, section group columns, animation specs | Tuning responsive breakpoints, card widths, column counts |
| `ContentListNavigation.kt` | `derivedFormatId()` helper (mode → formatId mapping) | Adding new `ContentListMode` variants |

**Key patterns:**
- **Data class grouping for composable params**: When a composable has >12 parameters, group related params into data classes (callbacks, state, config). Use defaults that encode "disabled" so callers only pass what they need. See `ContentListContentParams.kt`.
- **LazyGridScope subscope extensions**: Extract repeated or large grid `item {}` / `items {}` blocks as `LazyGridScope.emitXxx()` extension functions. Keep them `private` in the file that uses them.
- **`buildCallbacks()` factory**: When Compact and Expanded branches wire identical callbacks except for 1–2 handlers, hoist the common wiring into a factory function that takes only the differing handlers as parameters.

## ContentListItem Rendering

`ContentListPage` / `ContentListView` renders items heterogeneously via sealed class dispatch — `ContentListItem.Battle` -> shared `BattleCard` (Android/Web) / `BattleCardView` (iOS), `ContentListItem.Pokemon` -> `PokemonListRow` / `SimplePokemonRow`, `ContentListItem.Player` -> shared `PlayerListRow` (Android/Web) / player `HStack` (iOS). iOS uses SKIE `onEnum(of:)` for pattern matching.

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
