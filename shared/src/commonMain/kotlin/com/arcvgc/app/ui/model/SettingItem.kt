package com.arcvgc.app.ui.model

sealed class SettingItem {
    abstract val key: String
    abstract val title: String
    abstract val subtitle: String

    data class Toggle(
        override val key: String,
        override val title: String,
        override val subtitle: String,
        val isEnabled: Boolean
    ) : SettingItem()

    data class ColorChoice(
        override val key: String,
        override val title: String,
        override val subtitle: String,
        val selectedThemeId: Int
    ) : SettingItem()

    data class Action(
        override val key: String,
        override val title: String,
        override val subtitle: String,
        val confirmationMessage: String
    ) : SettingItem()

    data class DarkModeChoice(
        override val key: String,
        override val title: String,
        override val subtitle: String,
        val selectedModeId: Int
    ) : SettingItem()

    /**
     * Lets the user pick a preferred format.
     *
     * @property selectedFormatId Raw user selection. `0` means "follow [defaultFormatId]".
     * @property defaultFormatId The config's current default format (what "VGC Default" points to).
     *   `0` means config hasn't loaded yet — UI should disable interaction.
     */
    data class FormatChoice(
        override val key: String,
        override val title: String,
        override val subtitle: String,
        val selectedFormatId: Int,
        val defaultFormatId: Int
    ) : SettingItem()

    data class Link(
        override val key: String,
        override val title: String,
        override val subtitle: String,
        val url: String
    ) : SettingItem()
}
