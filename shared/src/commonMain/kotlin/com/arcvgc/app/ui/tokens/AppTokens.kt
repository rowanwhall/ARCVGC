package com.arcvgc.app.ui.tokens

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared design tokens for Android and Web.
 * iOS mirrors these in AppTokens.swift with matching names and native types.
 */
object AppTokens {

    // --- Corner Radii ---
    val CardCornerRadius = 12.dp
    val SearchButtonCornerRadius = 8.dp
    val FilterChipCornerRadius = 4.dp
    val MoveChipCornerRadius = 4.dp
    val PlayerChipCornerRadius = 16.dp
    val SmallFilterButtonCornerRadius = 6.dp
    val ColorSwatchCornerRadius = 6.dp

    // --- Border Widths ---
    val StandardBorderWidth = 1.dp
    val WinnerBorderWidth = 2.dp

    // --- Button & Icon Sizes ---
    val InfoButtonSize = 36.dp
    val InfoIconSize = 20.dp
    val ColorSwatchSize = 24.dp

    // --- Filter & Search ---
    val FilterChipHeight = 44.dp
    val SmallFilterButtonHorizontalPadding = 6.dp
    val SmallFilterButtonVerticalPadding = 4.dp
    val SmallFilterButtonFontSize = 12.sp

    // --- Player Chip ---
    val PlayerChipHorizontalPadding = 12.dp
    val PlayerChipVerticalPadding = 6.dp

    // --- Settings Row ---
    val SettingsRowHorizontalPadding = 16.dp
    val SettingsRowVerticalPadding = 12.dp
    val SettingsTitleFontSize = 16.sp
    val SettingsSubtitleFontSize = 13.sp

    // --- Content List ---
    val ContentListItemSpacing = 12.dp

    // --- Dialog (Web) ---
    val DialogWidth = 480.dp

    // --- Alpha ---
    val SecondaryIconAlpha = 0.5f
}
