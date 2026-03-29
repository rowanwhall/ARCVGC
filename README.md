# ARC — Automatic Replay Compiler

A Kotlin Multiplatform app for browsing [Pokémon Showdown](https://pokemonshowdown.com/) VGC battle replays. Browse recent matches, search by Pokémon/items/formats, view detailed team previews, and save favorites — all from a single shared codebase.

**Live at [arcvgc.com](https://arcvgc.com)**

## Platforms

| Platform | UI Framework | Status |
|----------|-------------|--------|
| Android | Jetpack Compose | ✅ |
| iOS | SwiftUI | ✅ |
| Web | Compose for wasmJs | ✅ |

## Features

- **Browse** — Paginated list of recent VGC battles with pull-to-refresh
- **Usage** — Top Pokémon usage rankings by format
- **Search** — Filter by up to 6 Pokémon (each with optional held item and tera type), format, minimum rating, and sort order
- **Battle Detail** — Team preview with full Pokémon details (moves, abilities, items, EVs, tera type) and replay links
- **Favorites** — Save battles, Pokémon, and players locally across three sub-tabs
- **Player & Pokémon Pages** — Tap any player or Pokémon to see all their battles
- **Settings** — Dark mode (system/light/dark), accent color themes, winner highlight toggle, catalog cache management
- **Deep Linking** — Shareable URLs for every page; battle detail as `?battle=ID` on any root URL (web URL bar mirrors navigation, Android App Links, iOS Universal Links + custom URL scheme)
- **Crash Reporting** — Sentry integration across all three platforms

## Tech Stack

- **Kotlin** 2.3.0, **Compose Multiplatform** 1.10.0
- **Ktor** 3.4.0 for networking (OkHttp/Darwin/Js engines per platform)
- **kotlinx-serialization** for JSON, **kotlinx-coroutines** for async
- **Coil 3** for image loading (Android & Web)
- **Hilt** for Android DI; manual `DependencyContainer` for iOS & Web
- **SKIE** for Kotlin/Swift sealed class interop
- **Sentry KMP** for crash reporting

## Project Structure

```
shared/                     — Shared Kotlin code (network, domain, data, UI models, mappers)
  src/commonMain/           — Platform-agnostic shared code
  src/androidMain/          — Android-specific implementations (OkHttp, SharedPreferences)
  src/iosMain/              — iOS-specific implementations (Darwin, NSUserDefaults)
  src/wasmJsMain/           — Web-specific implementations (Ktor JS, localStorage via @JsFun)
composeApp/src/androidMain/ — Android app (Compose UI, Hilt DI, ViewModels)
iosApp/                     — iOS app (SwiftUI views, ViewModels)
webApp/src/wasmJsMain/      — Web app (Compose for wasmJs, desktop-optimized layouts)
deploy/                     — Deployment scripts and server config
```

## Architecture

Clean architecture with three layers and explicit mappers between them:

```
Network DTOs  →  Domain Models  →  UI Models  →  Screen
  (Ktor)        (toDomain())      (toUiModel())   (Compose/SwiftUI)
```

## Setup

### Prerequisites

- Android Studio or IntelliJ IDEA with KMP plugin
- Xcode (for iOS builds)
- JDK 11+

### Secrets

Copy the example secrets file and fill in your values:

```bash
cp secrets.properties.example secrets.properties
```

This file is gitignored. Sentry crash reporting is disabled when DSNs are left empty.

### Build & Run

```bash
# Android
./gradlew :composeApp:assembleDebug

# iOS — build shared framework, then open in Xcode
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
open iosApp/iosApp.xcodeproj

# Web — dev server with hot reload
./gradlew :webApp:wasmJsBrowserDevelopmentRun

# Web — production build
./gradlew :webApp:wasmJsBrowserDistribution
```

## Legal

ARC is not affiliated with Nintendo, The Pokémon Company, Game Freak, Creatures Inc., or Pokémon Showdown/Smogon.

- [Privacy Policy](https://arcvgc.com/privacy-policy)
- [Terms of Service](https://arcvgc.com/terms-of-service)
