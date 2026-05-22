package com.arcvgc.app.ui.tutorial

import com.arcvgc.app.shared.Res
import com.arcvgc.app.shared.battles
import com.arcvgc.app.shared.favorites
import com.arcvgc.app.shared.players
import com.arcvgc.app.shared.pokemon
import com.arcvgc.app.shared.search
import com.arcvgc.app.shared.usage
import org.jetbrains.compose.resources.DrawableResource

// NOTE: these are temporary copies of the web screenshots — replace with
// Android-specific assets in a follow-up. See docs/tutorial.md.
actual fun tutorialImageResource(imageId: String?): DrawableResource? =
    sharedTutorialImageResource(imageId) ?: when (imageId) {
        "battles" -> Res.drawable.battles
        "pokemon" -> Res.drawable.pokemon
        "players" -> Res.drawable.players
        "usage" -> Res.drawable.usage
        "search" -> Res.drawable.search
        "favorites" -> Res.drawable.favorites
        else -> null
    }
