package com.arcvgc.app.ui

import androidx.compose.runtime.compositionLocalOf

enum class WindowSizeClass {
    Compact,
    Expanded
}

val LocalWindowSizeClass = compositionLocalOf { WindowSizeClass.Compact }
