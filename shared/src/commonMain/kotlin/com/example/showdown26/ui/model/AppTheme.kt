package com.example.showdown26.ui.model

enum class AppTheme(val id: Int, val displayName: String, val primaryColor: Long) {
    Red(0, "Red", 0xFFDC2F35),
    Blue(1, "Blue", 0xFF1A73E8),
    Yellow(2, "Yellow", 0xFFE6A700),
    Purple(3, "Purple", 0xFF7B1FA2);

    companion object {
        fun fromId(id: Int): AppTheme = entries.firstOrNull { it.id == id } ?: Red
    }
}
