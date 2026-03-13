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

    data class Link(
        override val key: String,
        override val title: String,
        override val subtitle: String,
        val url: String
    ) : SettingItem()
}
