package com.arcvgc.app.ui.search

/**
 * Returns initials for an ability name: first letter of the first 1-2 words,
 * ignoring parenthetical content. Examples:
 * - "Intimidate" -> "I"
 * - "Sand Stream" -> "SS"
 * - "As One (Glastrier)" -> "AO"
 */
fun abilityInitials(name: String): String {
    val cleaned = name.replace(Regex("\\(.*\\)"), "").trim()
    return cleaned.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
}
