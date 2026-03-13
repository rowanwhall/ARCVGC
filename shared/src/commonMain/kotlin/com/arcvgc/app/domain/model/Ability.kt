package com.arcvgc.app.domain.model

data class Ability(
    val id: Int,
    val name: String
) {
    /** Formats ability name for display: "SandStream" -> "Sand Stream" */
    val displayName: String
        get() = name.replace(Regex("([a-z])([A-Z])"), "$1 $2")
}
