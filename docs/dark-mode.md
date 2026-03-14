# Dark Mode

The app supports three dark mode options: **System** (default, follows OS), **Light**, and **Dark**. Accent/primary colors stay the same across light and dark modes.

## Theme implementation
- **Android** (`App.kt`): 4 light + 4 dark `ColorScheme` definitions (Red/Blue/Yellow/Purple). `colorSchemeForTheme(themeId, isDark)` selects the right one. `isDark` derived from `darkModeId` + `isSystemInDarkTheme()`.
- **iOS** (`ContentView.swift`): `.preferredColorScheme(settingsStore.colorSchemeOverride)` on TabView. `nil` = system, `.light` / `.dark` = forced. SwiftUI handles dark mode colors natively.
- **Web** (`WebApp.kt`): Same 4+4 color scheme pattern as Android. Content wrapped in `Surface` (critical — see color rules in `.claude/rules/code-quality.md`).

For color rules and surface hierarchy, see `.claude/rules/code-quality.md`.
