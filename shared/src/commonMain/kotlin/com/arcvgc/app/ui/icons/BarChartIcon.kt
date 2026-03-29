package com.arcvgc.app.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val BarChartIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "BarChart",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(4f, 9f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(11f)
            horizontalLineTo(4f)
            close()
            moveTo(16f, 13f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(7f)
            horizontalLineTo(16f)
            close()
            moveTo(10f, 4f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(16f)
            horizontalLineTo(10f)
            close()
        }
    }.build()
}
