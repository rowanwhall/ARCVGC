import SwiftUI
import Shared

struct SettingsView: View {
    @ObservedObject var settingsStore: SettingsStore
    var catalogStore: CatalogStore?
    @ObservedObject var favoritesStore: FavoritesStore
    @State private var showThemePicker = false
    @State private var showDarkModePicker = false
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
                                RoundedRectangle(cornerRadius: 6)
                                    .fill(SettingsStore.colorForThemeId(colorChoice.selectedThemeId))
                                    .frame(width: 24, height: 24)
                            }
                        }
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
