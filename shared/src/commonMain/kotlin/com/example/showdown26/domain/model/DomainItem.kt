package com.example.showdown26.domain.model

data class DomainItem(
    val id: Int,
    val name: String,
    val imageUrl: String? = null
) {
    /** Formats item name for display: "BoosterEnergy" -> "Booster Energy" */
    val displayName: String
        get() = name.replace(Regex("([a-z])([A-Z])"), "$1 $2")

    /**
     * Derives the sprite icon name for use with Serebii's item sprites.
     * Lowercase with all non-alphanumeric characters removed.
     * Example: "BoosterEnergy" -> "boosterenergy"
     * Example: "Hearthflame Mask" -> "hearthflamemask"
     */
    val spriteIconName: String
        get() = name.lowercase().replace(Regex("[^a-z0-9]"), "")
}
