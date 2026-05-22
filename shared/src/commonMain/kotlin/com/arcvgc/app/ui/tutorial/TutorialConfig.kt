package com.arcvgc.app.ui.tutorial

data class TutorialStep(
    val id: String,
    val imageId: String? = null
)

object TutorialConfig {
    val steps: List<TutorialStep> = listOf(
        TutorialStep(id = "welcome", imageId = "info"),
        TutorialStep(id = "battles", imageId = "battles"),
        TutorialStep(id = "pokemon", imageId = "pokemon"),
        TutorialStep(id = "players", imageId = "players"),
        TutorialStep(id = "usage", imageId = "usage"),
        TutorialStep(id = "search", imageId = "search"),
        TutorialStep(id = "favorites", imageId = "favorites"),
        TutorialStep(id = "settings", imageId = null)
    )
}
