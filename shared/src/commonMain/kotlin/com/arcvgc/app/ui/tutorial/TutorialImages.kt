package com.arcvgc.app.ui.tutorial

import com.arcvgc.app.shared.Res
import com.arcvgc.app.shared.favorite
import com.arcvgc.app.shared.info
import org.jetbrains.compose.resources.DrawableResource

/**
 * Resolves a tutorial image ID to a Compose Resource drawable.
 *
 * Each platform's actual references drawables from its own composeResources source set,
 * falling back to [sharedTutorialImageResource] for cross-platform icons.
 * - wasmJsMain: web-specific PNGs in `shared/src/wasmJsMain/composeResources/drawable/`.
 * - androidMain: only the shared icons (no Android-specific tutorial assets yet).
 * - iosMain: only the shared icons — iOS UI uses Compose-less SwiftUI and reads its own
 *   asset catalog via `iosApp/iosApp/Tutorial/TutorialImages.swift`.
 *
 * Returning null causes the page to render without an image slot.
 */
expect fun tutorialImageResource(imageId: String?): DrawableResource?

/**
 * Cross-platform icons living in the shared (commonMain) Compose resources, available on
 * every Compose target. Use for general-purpose icons reused outside the tutorial too.
 */
fun sharedTutorialImageResource(imageId: String?): DrawableResource? = when (imageId) {
    "info" -> Res.drawable.info
    "favorite" -> Res.drawable.favorite
    else -> null
}
