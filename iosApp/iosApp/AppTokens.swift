import Foundation

enum AppTokens {
    // Corner Radii
    static let cardCornerRadius: CGFloat = 12
    static let searchButtonCornerRadius: CGFloat = 8
    static let filterChipCornerRadius: CGFloat = 4
    static let moveChipCornerRadius: CGFloat = 4
    static let playerChipCornerRadius: CGFloat = 16
    static let colorSwatchCornerRadius: CGFloat = 6

    // Border Widths
    static let standardBorderWidth: CGFloat = 1
    static let winnerBorderWidth: CGFloat = 2

    // Button & Icon Sizes
    static let infoButtonSize: CGFloat = 36
    static let infoIconSize: CGFloat = 20
    static let colorSwatchSize: CGFloat = 24

    // Filter & Search
    static let filterChipHeight: CGFloat = 44
    static let smallFilterButtonCornerRadius: CGFloat = 6
    static let smallFilterButtonHorizontalPadding: CGFloat = 6
    static let smallFilterButtonVerticalPadding: CGFloat = 4
    static let smallFilterButtonFontSize: CGFloat = 12
    // Player Chip
    static let playerChipHorizontalPadding: CGFloat = 12
    static let playerChipVerticalPadding: CGFloat = 6

    // Settings Row
    static let settingsRowHorizontalPadding: CGFloat = 16
    static let settingsRowVerticalPadding: CGFloat = 12
    static let settingsTitleFontSize: CGFloat = 16
    static let settingsSubtitleFontSize: CGFloat = 13
    static let settingsValueFontSize: CGFloat = 14

    // Content List
    static let contentListItemSpacing: CGFloat = 12
    /// Approximate vertical space reserved at the bottom of the Usage list so
    /// scrollable content doesn't tuck underneath the anchored `UsageBottomBar`.
    /// Mirrors `UsageBottomBarReservedHeight` in shared `UsageBottomBar.kt`.
    static let usageBottomBarReservedHeight: CGFloat = 160

    // Hero / Branding
    static let heroLogoHeight: CGFloat = 96

    // Separators
    static let bulletSeparator = "\u{2022}"

    // Alpha
    static let secondaryIconAlpha: Double = 0.5
}
