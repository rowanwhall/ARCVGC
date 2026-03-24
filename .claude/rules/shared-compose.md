---
description: Rules for sharing Compose UI components between Android and Web
globs:
  - "composeApp/src/androidMain/**/ui/**/*.kt"
  - "webApp/src/wasmJsMain/**/ui/**/*.kt"
  - "shared/src/commonMain/**/ui/**/*.kt"
---

# Shared Compose Components

## Default: share between Android and Web

When creating a new `@Composable` function that will be used on both Android and Web, place it in the **shared module** (`shared/src/commonMain/kotlin/com/arcvgc/app/ui/components/`) by default. Only keep a component platform-specific when there is a concrete reason (different layout strategy, platform-only APIs, responsive behavior that differs between mobile and desktop).

## How to add a shared component

1. **Create the composable** in `shared/.../ui/components/` (or `shared/.../ui/contentlist/` for content list helpers).
2. **Use `PreviewAsyncImage`** for any image that loads from a URL — pass `Res.drawable.preview_*` for the preview drawable. Import via `com.arcvgc.app.shared.Res` and `com.arcvgc.app.shared.preview_*`.
3. **Add a `@Preview` function** in `composeApp/.../ui/components/`. Use a **distinct filename** from the shared file to avoid JVM class name collisions (e.g., `FooPreview.kt` for shared `Foo.kt`, or add to `SharedComponentPreviews.kt`).
4. **Do not create a web copy** — the web module inherits from shared automatically.
5. **Do not use Android-only APIs** in shared composables (`WindowInsets`, `@DrawableRes`, `LocalConfiguration`, `tooling.preview`). Pass platform values as parameters with sensible defaults (e.g., `statusBarPadding: Dp = 0.dp`).

## JVM class name collisions

Two `.kt` files with the same name in the same package — one in `shared/` and one in `composeApp/` — generate the same `*Kt` JVM class. At runtime, only one wins, causing `NoSuchMethodError`. Always use distinct filenames between modules for the same package.

## When to keep platform-specific

Keep separate implementations when:
- The component has fundamentally different **layout strategy** (e.g., responsive grid columns on web vs fixed on mobile)
- It uses **platform-only APIs** that can't be parameterized (e.g., `SimpleDateFormat` on Android vs custom parsing on wasmJs)
- The component's **interaction model** differs (e.g., `ModalBottomSheet` on Android vs `Dialog` on web)

Even in these cases, look for sub-components that *can* be shared (e.g., a row layout used inside both platform-specific screens).
