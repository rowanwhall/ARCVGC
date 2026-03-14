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

## Gradle
- Plugin: `io.sentry.kotlin.multiplatform.gradle` applied in `shared/build.gradle.kts`
- The plugin auto-installs the `sentry-kotlin-multiplatform` dependency into `commonMain`
- iOS requires the Sentry Cocoa SDK added via Swift Package Manager in Xcode (`https://github.com/getsentry/sentry-cocoa.git`)
