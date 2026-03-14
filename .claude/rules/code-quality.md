---
description: Code quality rules — dark mode colors, spelling, warnings, legal review triggers
globs:
  - "**/*.kt"
  - "**/*.swift"
---

# Code Quality Rules

## Dark Mode Colors

**NEVER use hardcoded `Color.Black` or `Color.White` for text or backgrounds.** Use theme-aware colors:
- **Android/Web (Compose)**: `MaterialTheme.colorScheme.onSurface` (text), `MaterialTheme.colorScheme.surface` (backgrounds), `MaterialTheme.colorScheme.surfaceContainer` (page backgrounds), `MaterialTheme.colorScheme.onSurfaceVariant` (secondary text)
- **iOS (SwiftUI)**: `Color(.label)` (text), `Color(.secondarySystemBackground)` (page backgrounds), `Color(.systemBackground)` (card backgrounds), `Color(.systemGray6)` (inner section backgrounds), `Color(.secondaryLabel)` (secondary text)
- **Exception**: `Color.Black`/`Color.White` in `PokemonAvatar.kt` / `PokemonAvatar.swift` are intentional Pokeball design colors.

**Surface hierarchy** (dark mode contrast, outermost to innermost):
- **Android/Web**: `surfaceContainer` (page) -> `surface` (card) -> `surfaceContainer` (inner section)
- **iOS**: `secondarySystemBackground` (page) -> `systemBackground` (card) -> `systemGray6` (inner section)
- **iOS detail sheet cards/chips**: Use `Color(.secondarySystemBackground)` (not `systemBackground`) for elements inside `systemGray6` sections.

**Web-specific**: `WebApp.kt` wraps all content in a `Surface` composable inside `MaterialTheme`. This is **required** because `MaterialTheme` alone does NOT set `LocalContentColor`. **Do not remove the `Surface` wrapper in `WebApp.kt`.**

## Spelling

All user-facing instances of the word "Pokemon" must use the accented spelling **"Pokemon"** (with e, U+00E9). Internal identifiers (class names, variable names, asset names, package names) remain unaccented.

## Warnings and Unused Imports

After completing any code changes, review all modified and newly created files for compiler warnings and unused imports before considering the task done:

- **Kotlin**: No unused imports, no redundant `!!` (use `?.let {}`, local val smart casts, or `.orEmpty()` instead), no `println()` debug statements left in code, all `CoroutineScope.launch {}` blocks must have try/catch on Kotlin/Native to prevent crashes
- **Swift**: No unused imports — but note that `import Foundation` is **required** in any Swift file that `import Shared` (the Kotlin/Native framework depends on Foundation types via NSObject). Only remove `import Foundation` from files that also `import SwiftUI` (which re-exports Foundation). No force unwraps (`!`) where safe alternatives exist (use `if let`, `guard let`, `.map {}`)
- Build all affected platforms after changes to verify no warnings or errors were introduced

## README.md

Keep `README.md` up to date when making changes that affect the public-facing project description. Specifically, update the README when: adding or removing a feature, changing the tech stack, modifying the project structure, changing build commands or setup steps, or adding new prerequisites. Do not update the README for internal refactors or bug fixes.

## Legal Review Triggers

After implementing features that affect privacy or terms, review and update `legal/privacy-policy.html` and `legal/terms-of-service.html`:
- Adding analytics or ad SDKs -> update Privacy Policy sections 4 and 5
- Adding user accounts, authentication, or remote data storage -> update Privacy Policy sections 1, 2, 7, 8 and Terms of Service section 1
- Adding monetization or paid features -> review both documents for necessary additions
- Collecting any new category of data -> update Privacy Policy section 1
- Changing data sources or adding new third-party API integrations -> update Terms of Service section 4
