package com.arcvgc.app.ui.tutorial

import org.jetbrains.compose.resources.DrawableResource

actual fun tutorialImageResource(imageId: String?): DrawableResource? =
    sharedTutorialImageResource(imageId)
