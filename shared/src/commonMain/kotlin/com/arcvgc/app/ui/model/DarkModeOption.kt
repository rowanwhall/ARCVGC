package com.arcvgc.app.ui.model

enum class DarkModeOption(val id: Int, val displayName: String) {
    System(0, "System"),
    Light(1, "Light"),
    Dark(2, "Dark");

    companion object {
        fun fromId(id: Int): DarkModeOption = entries.firstOrNull { it.id == id } ?: System
    }
}
