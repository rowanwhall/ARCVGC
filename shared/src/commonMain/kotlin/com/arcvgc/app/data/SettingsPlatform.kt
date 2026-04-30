package com.arcvgc.app.data

/**
 * Identifies the host platform for [SettingsRepository] so the shared
 * `buildSettingSections()` can include or omit platform-specific entries
 * (e.g. desktop-web-only animation toggle, future Apple/Google sign-in).
 */
enum class SettingsPlatform {
    Android,
    Ios,
    Web
}
