import SwiftUI
import Shared

struct SettingsView: View {
    @ObservedObject var settingsStore: SettingsStore
    var catalogStore: CatalogStore?
    @ObservedObject var favoritesStore: FavoritesStore
    @State private var showThemePicker = false
    @State private var showDarkModePicker = false
    @State private var showFormatPicker = false
    @State private var confirmActionKey: String?

    private var confirmActionItem: SettingItem.Action? {
        guard let key = confirmActionKey else { return nil }
        return settingsStore.settingItems
            .compactMap { item -> SettingItem.Action? in
                switch onEnum(of: item) {
                case .action(let action): return action
                default: return nil
                }
            }
            .first { $0.key == key }
    }

    private var currentFormatChoice: SettingItem.FormatChoice? {
        settingsStore.settingItems
            .compactMap { item -> SettingItem.FormatChoice? in
                switch onEnum(of: item) {
                case .formatChoice(let choice): return choice
                default: return nil
                }
            }
            .first
    }

    var body: some View {
        List {
            Section {
                ForEach(settingsStore.settingItems, id: \.key) { item in
                    switch onEnum(of: item) {
                    case .toggle(let toggle):
                        Toggle(isOn: Binding(
                            get: { toggle.isEnabled },
                            set: { settingsStore.setBooleanSetting(key: toggle.key, value: $0) }
                        )) {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(toggle.title)
                                Text(toggle.subtitle)
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                        }
                    case .darkModeChoice(let darkModeChoice):
                        Button {
                            showDarkModePicker = true
                        } label: {
                            HStack {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(darkModeChoice.title)
                                        .foregroundColor(.primary)
                                    Text(darkModeChoice.subtitle)
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                }
                                Spacer()
                                Text(DarkModeOption.companion.fromId(id: darkModeChoice.selectedModeId).displayName)
                                    .foregroundColor(.secondary)
                            }
                        }
                    case .colorChoice(let colorChoice):
                        Button {
                            showThemePicker = true
                        } label: {
                            HStack {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(colorChoice.title)
                                        .foregroundColor(.primary)
                                    Text(colorChoice.subtitle)
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                }
                                Spacer()
                                RoundedRectangle(cornerRadius: AppTokens.colorSwatchCornerRadius)
                                    .fill(SettingsStore.colorForThemeId(colorChoice.selectedThemeId))
                                    .frame(width: AppTokens.colorSwatchSize, height: AppTokens.colorSwatchSize)
                            }
                        }
                    case .formatChoice(let formatChoice):
                        FormatChoiceSettingRow(
                            formatChoice: formatChoice,
                            formats: catalogStore?.formatItems ?? [],
                            catalogLoading: catalogStore?.formatLoading ?? true,
                            onTap: { showFormatPicker = true }
                        )
                    case .action(let action):
                        Button {
                            confirmActionKey = action.key
                        } label: {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(action.title)
                                    .foregroundColor(.primary)
                                Text(action.subtitle)
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                        }
                    case .link(let link):
                        if let url = URL(string: link.url) {
                            SwiftUI.Link(destination: url) {
                                HStack {
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(link.title)
                                            .foregroundColor(.primary)
                                        Text(link.subtitle)
                                            .font(.caption)
                                            .foregroundColor(.secondary)
                                    }
                                    Spacer()
                                    Image(systemName: "arrow.up.right")
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                }
                            }
                        }
                    }
                }
            }
            Section {
                Text(SettingsRepository.companion.DISCLAIMER_TEXT)
                    .font(.caption2)
                    .italic()
                    .foregroundColor(.secondary)
                    .listRowBackground(Color.clear)
            }
        }
        .navigationTitle("Settings")
        .sheet(isPresented: $showThemePicker) {
            ThemePickerSheet(
                selectedThemeId: settingsStore.selectedThemeId,
                onSelect: { themeId in
                    settingsStore.setIntSetting(key: "selected_theme", value: themeId)
                }
            )
        }
        .sheet(isPresented: $showDarkModePicker) {
            DarkModePickerSheet(
                selectedModeId: settingsStore.darkModeId,
                onSelect: { modeId in
                    settingsStore.setIntSetting(key: "dark_mode", value: modeId)
                }
            )
        }
        .sheet(isPresented: $showFormatPicker) {
            if let formatChoice = currentFormatChoice {
                PreferredFormatPickerSheet(
                    formats: catalogStore?.formatItems ?? [],
                    selectedFormatId: formatChoice.selectedFormatId,
                    defaultFormatId: formatChoice.defaultFormatId,
                    onSelect: { formatId in
                        settingsStore.setIntSetting(key: formatChoice.key, value: formatId)
                    }
                )
            }
        }
        .alert(
            confirmActionItem?.title ?? "",
            isPresented: Binding(
                get: { confirmActionKey != nil },
                set: { if !$0 { confirmActionKey = nil } }
            )
        ) {
            Button("Cancel", role: .cancel) {
                confirmActionKey = nil
            }
            Button("Confirm", role: .destructive) {
                if let key = confirmActionKey {
                    settingsStore.performAction(key: key)
                    catalogStore?.reload()
                    favoritesStore.syncState()
                }
                confirmActionKey = nil
            }
        } message: {
            Text(confirmActionItem?.confirmationMessage ?? "")
        }
    }
}

#Preview {
    NavigationStack {
        SettingsView(settingsStore: SettingsStore(), catalogStore: nil, favoritesStore: FavoritesStore())
    }
}

private struct FormatChoiceSettingRow: View {
    let formatChoice: SettingItem.FormatChoice
    let formats: [FormatUiModel]
    let catalogLoading: Bool
    let onTap: () -> Void

    private var canInteract: Bool { !catalogLoading && !formats.isEmpty }

    private var selectedName: String {
        let effectiveId = formatChoice.selectedFormatId == 0
            ? formatChoice.defaultFormatId
            : formatChoice.selectedFormatId
        return formats.first(where: { $0.id == effectiveId })?.displayName ?? ""
    }

    var body: some View {
        Button {
            if canInteract { onTap() }
        } label: {
            HStack(alignment: .top, spacing: 12) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(formatChoice.title)
                        .foregroundColor(.primary)
                    Text(formatChoice.subtitle)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                .layoutPriority(1)
                .frame(maxWidth: .infinity, alignment: .leading)
                if canInteract {
                    Text(selectedName)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.trailing)
                        .frame(maxWidth: .infinity, alignment: .trailing)
                        .fixedSize(horizontal: false, vertical: true)
                } else {
                    ProgressView()
                        .scaleEffect(0.8)
                }
            }
        }
        .disabled(!canInteract)
    }
}

private struct PreferredFormatPickerSheet: View {
    let formats: [FormatUiModel]
    let selectedFormatId: Int32
    let defaultFormatId: Int32
    let onSelect: (Int32) -> Void

    @Environment(\.dismiss) private var dismiss

    private var defaultName: String? {
        formats.first(where: { $0.id == defaultFormatId })?.displayName
    }

    var body: some View {
        NavigationStack {
            List {
                if let defaultName {
                    Button {
                        onSelect(0)
                        dismiss()
                    } label: {
                        HStack {
                            Text("VGC Default - \(defaultName)")
                                .foregroundColor(.primary)
                            Spacer()
                            if selectedFormatId == 0 {
                                Image(systemName: "checkmark")
                                    .foregroundColor(.accentColor)
                            }
                        }
                    }
                }
                ForEach(formats, id: \.id) { format in
                    Button {
                        onSelect(format.id)
                        dismiss()
                    } label: {
                        HStack {
                            Text(format.displayName)
                                .foregroundColor(.primary)
                            Spacer()
                            if selectedFormatId == format.id {
                                Image(systemName: "checkmark")
                                    .foregroundColor(.accentColor)
                            }
                        }
                    }
                }
            }
            .navigationTitle("Preferred Format")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
    }
}
