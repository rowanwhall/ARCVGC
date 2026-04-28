package com.arcvgc.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.arcvgc.app.data.SettingsRepository as SharedSettingsRepository
import com.arcvgc.app.di.DependencyContainer
import com.arcvgc.app.ui.model.AppTheme
import com.arcvgc.app.ui.model.DarkModeOption
import com.arcvgc.app.ui.model.FormatSorter
import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.model.excludeHistoric
import com.arcvgc.app.ui.components.SettingsSectionCard
import com.arcvgc.app.ui.components.SettingsSectionHeader
import com.arcvgc.app.ui.model.SettingItem
import com.arcvgc.app.ui.tokens.AppTokens.ColorSwatchCornerRadius
import com.arcvgc.app.ui.tokens.AppTokens.ColorSwatchSize
import com.arcvgc.app.ui.tokens.AppTokens.DialogWidth
import com.arcvgc.app.ui.tokens.AppTokens.SettingsRowAccessoryGap
import com.arcvgc.app.ui.tokens.AppTokens.SettingsRowHorizontalPadding
import com.arcvgc.app.ui.tokens.AppTokens.SettingsRowVerticalPadding
import com.arcvgc.app.ui.tokens.AppTokens.SettingsSubtitleFontSize
import com.arcvgc.app.ui.tokens.AppTokens.SettingsTitleFontSize
import com.arcvgc.app.ui.tokens.AppTokens.SettingsValueFontSize
import kotlinx.browser.window

@Composable
fun SettingsPage(
    modifier: Modifier = Modifier
) {
    val settingsRepository = DependencyContainer.settingsRepository
    val sections by settingsRepository.settingSections.collectAsState()
    val allItems = sections.flatMap { it.items }
    val formatCatalogState by DependencyContainer.formatCatalogRepository.state.collectAsState()
    val currentFormatChoice = allItems.filterIsInstance<SettingItem.FormatChoice>().firstOrNull()
    val preferredFormatChoices = remember(formatCatalogState.items, currentFormatChoice?.selectedFormatId, currentFormatChoice?.defaultFormatId) {
        val effectiveId = currentFormatChoice?.let {
            if (it.selectedFormatId == SharedSettingsRepository.USE_DEFAULT_FORMAT) it.defaultFormatId else it.selectedFormatId
        }
        FormatSorter.sorted(
            formatCatalogState.items.excludeHistoric(keepId = currentFormatChoice?.selectedFormatId),
            effectiveId
        )
    }
    var showThemePicker by remember { mutableStateOf(false) }
    var showDarkModePicker by remember { mutableStateOf(false) }
    var showFormatPicker by remember { mutableStateOf(false) }
    var confirmActionKey by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 600.dp).fillMaxSize()
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 32.dp, bottom = 16.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                sections.forEach { section ->
                    item(key = "header_${section.title}") {
                        SettingsSectionHeader(title = section.title)
                    }
                    item(key = "card_${section.title}") {
                        SettingsSectionCard {
                            section.items.forEachIndexed { index, item ->
                                if (index > 0) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = SettingsRowHorizontalPadding),
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                                when (item) {
                                    is SettingItem.Toggle -> ToggleSettingRow(
                                        item = item,
                                        onToggle = { enabled ->
                                            settingsRepository.setBooleanSetting(key = item.key, value = enabled)
                                        }
                                    )
                                    is SettingItem.DarkModeChoice -> DarkModeChoiceSettingRow(
                                        item = item,
                                        onClick = { showDarkModePicker = true }
                                    )
                                    is SettingItem.ColorChoice -> ColorChoiceSettingRow(
                                        item = item,
                                        onClick = { showThemePicker = true }
                                    )
                                    is SettingItem.FormatChoice -> FormatChoiceSettingRow(
                                        item = item,
                                        formats = preferredFormatChoices,
                                        catalogLoading = formatCatalogState.isLoading,
                                        onClick = { showFormatPicker = true }
                                    )
                                    is SettingItem.Action -> ActionSettingRow(
                                        item = item,
                                        onClick = { confirmActionKey = item.key }
                                    )
                                    is SettingItem.Link -> LinkSettingRow(
                                        item = item,
                                        onClick = { window.open(item.url, "_blank") }
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Text(
                        text = SharedSettingsRepository.DISCLAIMER_TEXT,
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
                    )
                }
            }
        }
    }

    if (confirmActionKey != null) {
        val actionItem = allItems.filterIsInstance<SettingItem.Action>()
            .firstOrNull { it.key == confirmActionKey }
        if (actionItem != null) {
            AlertDialog(
                onDismissRequest = { confirmActionKey = null },
                title = { Text(actionItem.title) },
                text = { Text(actionItem.confirmationMessage) },
                confirmButton = {
                    TextButton(onClick = {
                        settingsRepository.performAction(actionItem.key)
                        DependencyContainer.pokemonCatalogRepository.reload()
                        DependencyContainer.itemCatalogRepository.reload()
                        DependencyContainer.teraTypeCatalogRepository.reload()
                        DependencyContainer.formatCatalogRepository.reload()
                        confirmActionKey = null
                    }) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmActionKey = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    if (showThemePicker) {
        val currentItem = allItems.filterIsInstance<SettingItem.ColorChoice>().firstOrNull()
        ThemePickerDialog(
            selectedThemeId = currentItem?.selectedThemeId ?: AppTheme.Red.id,
            onSelect = { themeId ->
                settingsRepository.setIntSetting(currentItem?.key ?: "", themeId)
                showThemePicker = false
            },
            onDismiss = { showThemePicker = false }
        )
    }

    if (showDarkModePicker) {
        val currentItem = allItems.filterIsInstance<SettingItem.DarkModeChoice>().firstOrNull()
        DarkModePickerDialog(
            selectedModeId = currentItem?.selectedModeId ?: DarkModeOption.System.id,
            onSelect = { modeId ->
                settingsRepository.setIntSetting(currentItem?.key ?: "", modeId)
                showDarkModePicker = false
            },
            onDismiss = { showDarkModePicker = false }
        )
    }

    if (showFormatPicker) {
        val currentItem = allItems.filterIsInstance<SettingItem.FormatChoice>().firstOrNull()
        if (currentItem != null) {
            PreferredFormatPickerDialog(
                formats = preferredFormatChoices,
                selectedFormatId = currentItem.selectedFormatId,
                defaultFormatId = currentItem.defaultFormatId,
                onSelect = { formatId ->
                    settingsRepository.setIntSetting(currentItem.key, formatId)
                    showFormatPicker = false
                },
                onDismiss = { showFormatPicker = false }
            )
        }
    }
}

@Composable
private fun ColorChoiceSettingRow(
    item: SettingItem.ColorChoice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = SettingsRowHorizontalPadding, vertical = SettingsRowVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = SettingsTitleFontSize,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = item.subtitle,
                fontSize = SettingsSubtitleFontSize,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .size(ColorSwatchSize)
                .clip(RoundedCornerShape(ColorSwatchCornerRadius))
                .background(Color(AppTheme.fromId(item.selectedThemeId).primaryColor))
        )
    }
}

@Composable
private fun ThemePickerDialog(
    selectedThemeId: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 0.dp,
            modifier = Modifier.width(DialogWidth)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Theme Color",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                AppTheme.entries.forEach { theme ->
                    ListItem(
                        headlineContent = { Text(theme.displayName) },
                        leadingContent = {
                            RadioButton(
                                selected = selectedThemeId == theme.id,
                                onClick = { onSelect(theme.id) }
                            )
                        },
                        trailingContent = {
                            Box(
                                modifier = Modifier
                                    .size(ColorSwatchSize)
                                    .clip(RoundedCornerShape(ColorSwatchCornerRadius))
                                    .background(Color(theme.primaryColor))
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(theme.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleSettingRow(
    item: SettingItem.Toggle,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SettingsRowHorizontalPadding, vertical = SettingsRowVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = SettingsTitleFontSize,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = item.subtitle,
                fontSize = SettingsSubtitleFontSize,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = item.isEnabled,
            onCheckedChange = onToggle
        )
    }
}

@Composable
private fun FormatChoiceSettingRow(
    item: SettingItem.FormatChoice,
    formats: List<FormatUiModel>,
    catalogLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clickable = !catalogLoading && formats.isNotEmpty()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .let { if (clickable) it.clickable(onClick = onClick) else it }
            .padding(horizontal = SettingsRowHorizontalPadding, vertical = SettingsRowVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = SettingsTitleFontSize,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = item.subtitle,
                fontSize = SettingsSubtitleFontSize,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (clickable) {
            val effectiveId = if (item.selectedFormatId == SharedSettingsRepository.USE_DEFAULT_FORMAT) {
                item.defaultFormatId
            } else item.selectedFormatId
            val name = formats.firstOrNull { it.id == effectiveId }?.displayName ?: ""
            Text(
                text = name,
                fontSize = SettingsValueFontSize,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = SettingsRowAccessoryGap)
            )
        } else {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier.size(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PreferredFormatPickerDialog(
    formats: List<FormatUiModel>,
    selectedFormatId: Int,
    defaultFormatId: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val defaultName = formats.firstOrNull { it.id == defaultFormatId }?.displayName
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 0.dp,
            modifier = Modifier.width(DialogWidth)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Preferred Format",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (defaultName != null) {
                    ListItem(
                        headlineContent = { Text("VGC Default - $defaultName") },
                        leadingContent = {
                            RadioButton(
                                selected = selectedFormatId == SharedSettingsRepository.USE_DEFAULT_FORMAT,
                                onClick = { onSelect(SharedSettingsRepository.USE_DEFAULT_FORMAT) }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(SharedSettingsRepository.USE_DEFAULT_FORMAT) }
                    )
                }
                formats.forEach { format ->
                    ListItem(
                        headlineContent = { Text(format.displayName) },
                        leadingContent = {
                            RadioButton(
                                selected = selectedFormatId == format.id,
                                onClick = { onSelect(format.id) }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(format.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DarkModeChoiceSettingRow(
    item: SettingItem.DarkModeChoice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = SettingsRowHorizontalPadding, vertical = SettingsRowVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = SettingsTitleFontSize,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = item.subtitle,
                fontSize = SettingsSubtitleFontSize,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = DarkModeOption.fromId(item.selectedModeId).displayName,
            fontSize = SettingsValueFontSize,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DarkModePickerDialog(
    selectedModeId: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 0.dp,
            modifier = Modifier.width(DialogWidth)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Dark Mode",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                DarkModeOption.entries.forEach { option ->
                    ListItem(
                        headlineContent = { Text(option.displayName) },
                        leadingContent = {
                            RadioButton(
                                selected = selectedModeId == option.id,
                                onClick = { onSelect(option.id) }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionSettingRow(
    item: SettingItem.Action,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = SettingsRowHorizontalPadding, vertical = SettingsRowVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = SettingsTitleFontSize,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = item.subtitle,
                fontSize = SettingsSubtitleFontSize,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LinkSettingRow(
    item: SettingItem.Link,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = SettingsRowHorizontalPadding, vertical = SettingsRowVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = SettingsTitleFontSize,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = item.subtitle,
                fontSize = SettingsSubtitleFontSize,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "->",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
