package com.arcvgc.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arcvgc.app.ui.tokens.AppTokens.InfoButtonSize
import com.arcvgc.app.ui.tokens.AppTokens.InfoIconSize

@Composable
fun InfoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Info"
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(InfoButtonSize)
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = contentDescription,
            modifier = Modifier.size(InfoIconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
