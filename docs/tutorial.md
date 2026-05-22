# Tutorial Dialog

Multi-page tutorial overlay launched from the `?` icon in the Home toolbar (right of the Submit Replay action).

## Architecture

```
TutorialConfig.steps  →  TutorialContentProvider.get(id) (shared strings)
       (shared)       →  tutorialOverride(id) / TutorialOverrides[id] (per-platform strings)
                      →  tutorialImageResource(imageId) / TutorialImages.assetName (per-platform images)
                      →  TutorialDialog composable / TutorialDialog SwiftUI view
```

Pages are declared once in `TutorialConfig.steps` (shared). Each `TutorialStep` carries an `id` (used to look up title/body) and an optional `imageId` (used to look up a platform-specific drawable/asset). Editing this list is the only thing required to add, remove, or reorder pages — the dialog re-renders without code changes.

## Files

### Shared

- `shared/.../ui/tutorial/TutorialConfig.kt` — ordered list of `TutorialStep(id, imageId?)`.
- `shared/.../ui/tutorial/TutorialContent.kt` — `TutorialString(title, body)` data class + `TutorialContentProvider` map. Default strings live here.
- `shared/.../ui/tutorial/PlatformTutorialOverrides.kt` — `expect fun tutorialOverride(id)` returning a per-platform override or null. Android, iOS, and wasmJs each have an `actual` returning null by default.
- `shared/.../ui/tutorial/TutorialImages.kt` — `expect fun tutorialImageResource(imageId)` → `DrawableResource?`, plus `sharedTutorialImageResource(imageId)` for cross-platform icons. Each platform actual falls back to the shared resolver:
  - `sharedTutorialImageResource` maps `info` → `Res.drawable.info` and `favorite` → `Res.drawable.favorite`. These PNGs live in `shared/src/commonMain/composeResources/drawable/` (alongside `logo`/`error`) and are general-purpose icons reused across the app, not tutorial-only.
  - `wasmJsMain` actual: shared resolver first, then web-specific PNGs `Res.drawable.battles_web` / `pokemon_web` / `players_web` / `usage_web` / `search_web` / `favorites_web` (in `shared/src/wasmJsMain/composeResources/drawable/`, scoped to web only).
  - `androidMain` actual: shared resolver only — no Android-specific tutorial screenshots yet.
  - `iosMain` actual: shared resolver only — iOS UI doesn't call this; it uses its own Swift mapper (`TutorialImages.swift`) and asset catalog.
- `shared/.../ui/components/TutorialDialog.kt` — shared composable used by Android and Web. `HorizontalPager` + dot indicator. `showArrows` parameter renders prev/next icon buttons (used by Expanded-width web).

### Android

Wired in `composeApp/.../ui/contentlist/ContentListPage.kt`:
- `Icons.Default.HelpOutline` `IconButton` added to the Home toolbar `actions` slot, right after the Submit Replay button.
- `showTutorialDialog` state hoisted alongside `showSubmitReplayDialog`.
- `TutorialDialog(onDismiss = …)` rendered inline. No `showArrows` (compact device).

### Web

Wired in `webApp/.../ui/contentlist/ContentListPage.kt`. Same `?` icon at both Home-toolbar call sites (Compact and Expanded branches). The dialog host passes `showArrows = !isCompact` so desktop gets prev/next arrows while mobile web stays swipe-only.

### iOS

- `iosApp/iosApp/Tutorial/TutorialDialog.swift` — `TabView` with `.tabViewStyle(.page(indexDisplayMode: .always))` for swipe + dot indicator. Wrapped by `.sheet(isPresented:)` with `.presentationDetents([.medium, .large])` at the call site.
- `iosApp/iosApp/Tutorial/TutorialImages.swift` — maps `imageId` → asset-catalog name. Initial mapping: `"logo" -> "Logo"`.
- `iosApp/iosApp/Tutorial/TutorialOverrides.swift` — empty `[String: TutorialString]` dict. Resolution order is Swift override → shared `TutorialContentProvider.get(id)`. (The Kotlin `tutorialOverride` actual on iOS always returns null; the Swift-side dict is the actual iOS override mechanism.)
- `iosApp/iosApp/ContentListView.swift` — `Image(systemName: "questionmark.circle")` toolbar button + `.sheet`.

## Adding or removing a tutorial page

1. Edit `TutorialConfig.steps`: add (or remove) a `TutorialStep(id = "newId", imageId = "logo" /* or null */)`.
2. Add a matching entry to the `content` map in `TutorialContentProvider` (title + body).
3. If you used a new `imageId`, wire it up:
   - Shared/cross-platform icon (same image everywhere): drop the PNG into `shared/src/commonMain/composeResources/drawable/` and add a `when` branch to `sharedTutorialImageResource()`. Renders on Android + Web automatically.
   - Web-specific image: drop the PNG into `shared/src/wasmJsMain/composeResources/drawable/` and add a `when` branch to the wasmJs actual of `tutorialImageResource()`.
   - Android-specific image: drop the PNG into `shared/src/androidMain/composeResources/drawable/` (create the dir if needed) and add a `when` branch to the androidMain actual.
   - iOS: add the asset to the iOS asset catalog and add a `case` to `TutorialImages.assetName(for:)` in `iosApp/iosApp/Tutorial/TutorialImages.swift`.
   Returning null/nil on a platform causes that page to render without an image slot.
4. (Optional) If a platform needs different copy, add an entry to the per-platform overrides:
   - Kotlin: implement `tutorialOverride(id)` in the appropriate `actual` source set.
   - Swift: add an entry to `TutorialOverrides.entries`.

The `TutorialContentProviderTest` test in `shared/src/commonTest/.../tutorial/` enforces "every configured step has a non-blank shared title/body" — adding a step without strings fails the test.

## Pager UX summary

| Platform | Swipe | Arrows | Dots |
|---|---|---|---|
| Android phone | yes | no | yes |
| Android tablet | yes | no | yes |
| iOS (iPhone + iPad) | yes | no | yes |
| Web compact | yes | no | yes |
| Web expanded (desktop) | yes | yes (disabled at endpoints) | yes |
