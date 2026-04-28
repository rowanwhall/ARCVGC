package com.arcvgc.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.arcvgc.app.ui.tokens.AppTokens.CardCornerRadius
import com.arcvgc.app.ui.tokens.AppTokens.SettingsSectionHeaderBottomPadding
import com.arcvgc.app.ui.tokens.AppTokens.SettingsSectionHeaderFontSize
import com.arcvgc.app.ui.tokens.AppTokens.SettingsSectionHeaderTopPadding
import com.arcvgc.app.ui.tokens.AppTokens.SettingsSectionHorizontalPadding
import com.arcvgc.app.ui.tokens.AppTokens.StandardBorderWidth

@Composable
fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        fontSize = SettingsSectionHeaderFontSize,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(
            start = SettingsSectionHorizontalPadding,
            end = SettingsSectionHorizontalPadding,
            top = SettingsSectionHeaderTopPadding,
            bottom = SettingsSectionHeaderBottomPadding
        )
    )
}

@Composable
fun SettingsSectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SettingsSectionHorizontalPadding),
        shape = RoundedCornerShape(CardCornerRadius),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(StandardBorderWidth, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column { content() }
    }
}
