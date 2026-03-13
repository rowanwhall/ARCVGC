package com.arcvgc.app.domain.model

data class Move(
    val id: Int,
    val name: String
) {
    /** Formats move name for display: "ShadowBall" -> "Shadow Ball" */
    val displayName: String
        get() = name.replace(Regex("([a-z])([A-Z])"), "$1 $2")
}
