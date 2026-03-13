package com.arcvgc.app.ui

import androidx.compose.runtime.compositionLocalOf

enum class WindowSizeClass {
    Compact,
    Expanded
}

val LocalWindowSizeClass = compositionLocalOf { WindowSizeClass.Expanded }

data class BattleOverlayRequest(
    val battleId: Int,
    val player1IsWinner: Boolean?,
    val player2IsWinner: Boolean?
)

val LocalBattleOverlay = compositionLocalOf<((BattleOverlayRequest?) -> Unit)?> { null }
