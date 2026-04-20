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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Intent
import android.net.Uri
import com.arcvgc.app.data.repository.AppConfigRepository
import com.arcvgc.app.data.repository.FormatCatalogRepository
import com.arcvgc.app.data.repository.ItemCatalogRepository
import com.arcvgc.app.data.repository.PokemonCatalogRepository
import com.arcvgc.app.data.repository.SettingsRepository
import com.arcvgc.app.data.repository.TeraTypeCatalogRepository
import com.arcvgc.app.ui.model.AppTheme
import com.arcvgc.app.ui.model.DarkModeOption
import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.components.SettingsSectionCard
import com.arcvgc.app.ui.components.SettingsSectionHeader
import com.arcvgc.app.ui.model.SettingItem
import com.arcvgc.app.ui.tokens.AppTokens.ColorSwatchCornerRadius
import com.arcvgc.app.ui.tokens.AppTokens.ColorSwatchSize
import com.arcvgc.app.ui.tokens.AppTokens.SettingsRowHorizontalPadding
import com.arcvgc.app.ui.tokens.AppTokens.SettingsRowVerticalPadding
import com.arcvgc.app.ui.tokens.AppTokens.SettingsSubtitleFontSize
import com.arcvgc.app.ui.tokens.AppTokens.SettingsTitleFontSize
import com.arcvgc.app.ui.tokens.AppTokens.SettingsValueFontSize
import com.arcvgc.app.data.SettingsRepository as SharedSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val settingsRepository: SettingsRepository,
    val appConfigRepository: AppConfigRepository,
    private val pokemonCatalogRepository: PokemonCatalogRepository,
    private val itemCatalogRepository: ItemCatalogRepository,
    private val teraTypeCatalogRepository: TeraTypeCatalogRepository,
    val formatCatalogRepository: FormatCatalogRepository
) : ViewModel() {
    fun performAction(key: String) {
        settingsRepository.performAction(key)
        pokemonCatalogRepository.reload()
        itemCatalogRepository.reload()
        teraTypeCatalogRepository.reload()
        formatCatalogRepository.reload()
    }
}

@Composable
fun SettingsPage(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val sections by viewModel.settingsRepository.settingSections.collectAsStateWithLifecycle()
    val allItems = sections.flatMap { it.items }
    val formatCatalogState by viewModel.formatCatalogRepository.state.collectAsStateWithLifecycle()
    var showThemePicker by remember { mutableStateOf(false) }
    var showDarkModePicker by remember { mutableStateOf(false) }
    var showFormatPicker by remember { mutableStateOf(false) }
    var confirmActionKey by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
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
                                        viewModel.settingsRepository.setBooleanSetting(item.key, enabled)
                                    }
                                )
                                is SettingItem.ColorChoice -> ColorChoiceSettingRow(
                                    item = item,
                                    onClick = { showThemePicker = true }
                                )
                                is SettingItem.DarkModeChoice -> DarkModeChoiceSettingRow(
                                    item = item,
                                    onClick = { showDarkModePicker = true }
                                )
                                is SettingItem.FormatChoice -> FormatChoiceSettingRow(
                                    item = item,
                                    formats = formatCatalogState.items,
                                    catalogLoading = formatCatalogState.isLoading,
                                    onClick = { showFormatPicker = true }
                                )
                                is SettingItem.Action -> ActionSettingRow(
                                    item = item,
                                    onClick = { confirmActionKey = item.key }
                                )
                                is SettingItem.Link -> LinkSettingRow(
                                    item = item,
                                    onClick = {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.url)))
                                    }
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
                        viewModel.performAction(actionItem.key)
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
        ThemePickerSheet(
            selectedThemeId = currentItem?.selectedThemeId ?: AppTheme.Red.id,
            onSelect = { themeId ->
                viewModel.settingsRepository.setIntSetting(currentItem?.key ?: "", themeId)
                showThemePicker = false
            },
            onDismiss = { showThemePicker = false }
        )
    }

    if (showDarkModePicker) {
        val currentItem = allItems.filterIsInstance<SettingItem.DarkModeChoice>().firstOrNull()
        DarkModePickerSheet(
            selectedModeId = currentItem?.selectedModeId ?: DarkModeOption.System.id,
            onSelect = { modeId ->
                viewModel.settingsRepository.setIntSetting(currentItem?.key ?: "", modeId)
                showDarkModePicker = false
            },
            onDismiss = { showDarkModePicker = false }
        )
    }

    if (showFormatPicker) {
        val currentItem = allItems.filterIsInstance<SettingItem.FormatChoice>().firstOrNull()
        if (currentItem != null) {
            PreferredFormatPickerSheet(
                formats = formatCatalogState.items,
                selectedFormatId = currentItem.selectedFormatId,
                defaultFormatId = currentItem.defaultFormatId,
                onSelect = { formatId ->
                    viewModel.settingsRepository.setIntSetting(currentItem.key, formatId)
                    showFormatPicker = false
                },
                onDismiss = { showFormatPicker = false }
            )
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
                .padding(start = 12.dp)
                .size(ColorSwatchSize)
                .clip(RoundedCornerShape(ColorSwatchCornerRadius))
                .background(Color(AppTheme.fromId(item.selectedThemeId).primaryColor))
        )
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
            text = "\u2197",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    .padding(start = 12.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreferredFormatPickerSheet(
    formats: List<FormatUiModel>,
    selectedFormatId: Int,
    defaultFormatId: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val defaultName = formats.firstOrNull { it.id == defaultFormatId }?.displayName
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = "Preferred Format",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DarkModePickerSheet(
    selectedModeId: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = "Dark Mode",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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

@Preview(showBackground = true)
@Composable
private fun ActionSettingRowPreview() {
    MaterialTheme {
        ActionSettingRow(
            item = SettingItem.Action(
                key = "clear_cache",
                title = "Clear Catalog Cache",
                subtitle = "Clears cached Pokémon, items, tera types, and formats. They'll reload from the network on next launch.",
                confirmationMessage = "Are you sure?"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ColorChoiceSettingRowPreview() {
    MaterialTheme {
        ColorChoiceSettingRow(
            item = SettingItem.ColorChoice(
                key = "theme",
                title = "Theme Color",
                subtitle = "Choose the app's accent color. We like red like our mascot, but maybe you're feeling Great, Ultra, or Master.",
                selectedThemeId = 0
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LinkSettingRowPreview() {
    MaterialTheme {
        LinkSettingRow(
            item = SettingItem.Link(
                key = "privacy",
                title = "Privacy Policy",
                subtitle = "How ARC handles your data (spoiler: it doesn't).",
                url = "https://example.com"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ToggleSettingRowPreview() {
    MaterialTheme {
        ToggleSettingRow(
            item = SettingItem.Toggle(
                key = "test",
                title = "Winner Highlight",
                subtitle = "Show green border around the winning player's team. Could save you time, but maybe you don't want spoilers.",
                isEnabled = true
            ),
            onToggle = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FormatChoiceSettingRowLoadedPreview() {
    MaterialTheme {
        FormatChoiceSettingRow(
            item = SettingItem.FormatChoice(
                key = "preferred_format",
                title = "Preferred Format",
                subtitle = "What do you want to see first?",
                selectedFormatId = 1,
                defaultFormatId = 1
            ),
            formats = listOf(FormatUiModel(id = 1, displayName = "[Gen 9 Champions] VGC 2026 Reg M-A (Bo3)")),
            catalogLoading = false,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FormatChoiceSettingRowLoadingPreview() {
    MaterialTheme {
        FormatChoiceSettingRow(
            item = SettingItem.FormatChoice(
                key = "preferred_format",
                title = "Preferred Format",
                subtitle = "What do you want to see first?",
                selectedFormatId = 0,
                defaultFormatId = 0
            ),
            formats = emptyList(),
            catalogLoading = true,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DarkModeChoiceSettingRowPreview() {
    MaterialTheme {
        DarkModeChoiceSettingRow(
            item = SettingItem.DarkModeChoice(
                key = "dark_mode",
                title = "Dark Mode",
                subtitle = "Choose between system, light, or dark appearance.",
                selectedModeId = 0
            ),
            onClick = {}
        )
    }
}
