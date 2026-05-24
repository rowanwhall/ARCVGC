# Info Dialogs

Reusable info icon + dialog system for displaying contextual help across the app.

## Architecture

Content is centralized in the shared module; each platform implements its own dialog UI component.

```
InfoContentProvider (shared)  →  InfoButton + InfoSheet (Android)
        ↓ key lookup           →  InfoButton + InfoDialog (Web)
                               →  InfoButton + InfoSheet (iOS)
```

### Shared: `InfoContent` + `InfoContentProvider`

- **File**: `shared/.../ui/model/InfoContent.kt`
- `InfoContent(title, body, imageUrl?)` — data class for dialog content. `imageUrl` is nullable, reserved for future image support.
- `InfoContentProvider` — singleton object with a `Map<String, InfoContent>`. Lookup via `get(key)` returns `InfoContent?`.
- Current keys: `"replay"` (replay/set explanation), `"unrated"` (unrated battle explanation), `"trending_lookback"` (home Trending lookback-window explanation), `"top_players"` (Pokemon-profile Top Players explanation)

### Adding new info content

1. Add a new entry to the `content` map in `InfoContentProvider` with a descriptive string key
2. At the call site on each platform, add `showXxxInfo` state + `InfoButton` + conditional dialog/sheet rendering with `InfoContentProvider.get("your_key")`

### Content-list info hooks (lookback selector + section headers)

The ContentListPage exposes two reusable info-button hooks so a single `infoKeyToShow` state (hosted per platform in the content composable / view) drives any number of info dialogs:

- **Section headers**: `ContentListItem.Section` carries an optional `infoKey`. The shared `SectionHeader` (Android/Web) and iOS `SectionHeaderView` take an `onInfoClick` callback that renders an `InfoButton` after the title. Each platform's section-rendering path passes `onInfoClick = section.infoKey?.let { … }`. Set `infoKey` on a section in `ContentListLogic` to attach a dialog (e.g. `"top_players"` on the Pokemon profile's Top Players section).
- **Lookback selector**: the shared `LookbackSegmentedSelector` (Android/Web) and iOS `LookbackSegmentedSelector` take an `onInfoClick` callback that appends an `InfoButton` to the row. The Home page passes it (gated by `showLookbackInfo` / `isHomeMode`); other modes leave it `null` so no button appears.

State lives in: Android `ContentListContent` (`infoKeyToShow` + `InfoSheet`), Web `ContentListContent` (`infoKeyToShow` + `InfoDialog`), iOS `ContentListView` (`@State infoKeyToShow` + `.sheet`).

### Platform components

| Platform | Icon | Dialog | Pattern |
|---|---|---|---|
| Android | `InfoButton.kt` (`Icons.Outlined.Info`, 20dp icon / 36dp touch) | `InfoSheet.kt` (`ModalBottomSheet`, no drag handle, X close button) | Bottom sheet |
| Web | `InfoButton.kt` (same icon/sizing) | `InfoDialog.kt` (`Dialog` + `Surface`, 480dp width, X close button) | Dialog (matches picker pattern) |
| iOS | `InfoButton.swift` (`info.circle` SF Symbol, 16pt) | `InfoSheet.swift` (`.sheet` with `.presentationDetents([.medium])`, hidden drag indicator, X close button) | Sheet |

All dialogs have:
- A centered brand "info" mascot image at the top (`info` drawable / iOS `Info` asset), sized via the shared `InfoDialogImageSize` (72) token. This is a fixed decorative element on every info dialog, distinct from the per-content `imageUrl` field (which remains unused).
- Title (headline/titleMedium)
- Body text (body/bodyMedium, secondary color)
- X close button in top-right corner (no drag handle)
- Content column has extra end padding (40dp / 12pt) to avoid title/close-button overlap
- Future: optional per-content image via the `imageUrl` field (not yet rendered by any platform)

### Key files

- `shared/.../ui/model/InfoContent.kt` — Content registry
- `composeApp/.../ui/components/InfoButton.kt`, `InfoSheet.kt` — Android components
- `webApp/.../ui/components/InfoButton.kt`, `InfoDialog.kt` — Web components
- `iosApp/iosApp/InfoButton.swift`, `InfoSheet.swift` — iOS components

### iOS bridging

`InfoContentProvider` is a Kotlin `object`, accessed from Swift as `InfoContentProvider.shared.get(key: "replay")`. The `InfoContent` data class bridges cleanly since it only has `String` and `String?` fields.

### wasmJs caveat

Do **not** declare `data class` inside a composable function body on wasmJs — it causes infinite recomposition loops. The web module hoists `GameButton` and similar classes to file-level `private` scope. This is a known Kotlin/Wasm issue.
