package com.arcvgc.app.ui.tutorial

import com.arcvgc.app.shared.Res
import com.arcvgc.app.shared.battles_web
import com.arcvgc.app.shared.favorites_web
import com.arcvgc.app.shared.players_web
import com.arcvgc.app.shared.pokemon_web
import com.arcvgc.app.shared.search_web
import com.arcvgc.app.shared.usage_web
import org.jetbrains.compose.resources.DrawableResource

actual fun tutorialImageResource(imageId: String?): DrawableResource? =
    sharedTutorialImageResource(imageId) ?: when (imageId) {
        "battles" -> Res.drawable.battles_web
        "pokemon" -> Res.drawable.pokemon_web
        "players" -> Res.drawable.players_web
        "usage" -> Res.drawable.usage_web
        "search" -> Res.drawable.search_web
        "favorites" -> Res.drawable.favorites_web
        else -> null
    }
