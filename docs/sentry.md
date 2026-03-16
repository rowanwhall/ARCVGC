# Crash Reporting (Sentry)

Sentry is used for crash and error reporting across all three platforms. Each platform has its own Sentry project and DSN.

## Architecture
- **Shared layer**: `expect fun initializeSentry()` in `shared/.../data/SentryInit.kt` with platform `actual` implementations
- **Android** (`SentryInit.android.kt`): Uses Sentry KMP SDK (`io.sentry.kotlin.multiplatform.Sentry.init {}`)
- **iOS** (`SentryInit.ios.kt`): Uses Sentry KMP SDK (bridges to Sentry Cocoa SDK). Requires Sentry Cocoa framework via SPM in Xcode
- **Web** (`SentryInit.wasmJs.kt`): The KMP SDK's wasmJs target is a no-op stub. Real error tracking uses the Sentry JavaScript Browser SDK loaded via `<script>` tag in `webApp/.../resources/index.html`, bridged from Kotlin via `@JsFun`

## Initialization
Called once at app startup, before any other initialization:
- **Android**: `ShowdownApplication.onCreate()` calls `initializeSentry()`
- **iOS**: `iOSApp.init()` calls `SentryInit_iosKt.initializeSentry()`
- **Web**: `main()` in `Main.kt` calls `initializeSentry()` before `ComposeViewport`

## Non-Fatal Error Capture
`expect fun captureException(throwable: Throwable)` in `shared/.../data/SentryInit.kt` manually reports caught exceptions to Sentry as non-fatal events.

- **Android/iOS**: Delegates to `io.sentry.kotlin.multiplatform.Sentry.captureException(throwable)`
- **Web**: Bridges to JS `Sentry.captureException(new Error(message))` via `@JsFun`
- **Usage**: Called at three levels:
  1. `ApiService` catch blocks — reports all network/parsing errors
  2. `BattleRepository` throw paths — reports all repository-level errors before re-throwing
  3. `CatalogLoader.safeCatalogLoad` — wraps all iOS-facing catalog loaders
  4. `CoroutineExceptionHandler` — safety net on all shared coroutine scopes

## Error Handling at the K/N Boundary
Kotlin exceptions thrown from suspend functions may not be caught by Swift's `do/catch` (see coding conventions for details). All Kotlin suspend functions called from iOS handle errors internally:

- **`BattleRepository.getMatchDetailOrNull()`**: Non-throwing wrapper for iOS; returns null on error (Sentry capture happens in `getMatchDetail` before throwing)
- **Catalog loaders** (`loadPokemonCatalog`, etc.): Wrapped in `safeCatalogLoad` which catches exceptions and returns `CatalogResult(error = ...)` instead of throwing
- **`ContentListLogic`**: Runs entirely in Kotlin coroutine scopes with try/catch; iOS never calls these methods directly
- **Swift side**: Always uses `try?` (not `do/catch`) when calling Kotlin suspend functions

## Coroutine Exception Safety
All shared `CoroutineScope` instances include a `CoroutineExceptionHandler` that captures exceptions to Sentry:
- **`CoroutineScopeFactory.createMainScope()`**: Used by iOS ViewModels (`SupervisorJob + Dispatchers.Main + handler`)
- **`createSafeScope()`**: Used by catalog repositories and `AppConfigRepository` (`SupervisorJob + Dispatchers.Default + handler`)

This ensures uncaught exceptions in coroutines are reported to Sentry rather than crashing the app on Kotlin/Native.

## Gradle
- Plugin: `io.sentry.kotlin.multiplatform.gradle` applied in `shared/build.gradle.kts`
- The plugin auto-installs the `sentry-kotlin-multiplatform` dependency into `commonMain`
- iOS requires the Sentry Cocoa SDK added via Swift Package Manager in Xcode (`https://github.com/getsentry/sentry-cocoa.git`)
