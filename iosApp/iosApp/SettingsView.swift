import SwiftUI
import Shared

struct SettingsView: View {
    @ObservedObject var settingsStore: SettingsStore
    @ObservedObject var catalogStore: CatalogStore
    @ObservedObject var favoritesStore: FavoritesStore
    @State private var showThemePicker = false
    @State private var showDarkModePicker = false
    @State private var showFormatPicker = false
    @State private var confirmActionKey: String?

    private var allItems: [SettingItem] {
        settingsStore.settingSections.flatMap { $0.items }
    }

    private var confirmActionItem: SettingItem.Action? {
        guard let key = confirmActionKey else { return nil }
        return allItems
            .compactMap { item -> SettingItem.Action? in
                switch onEnum(of: item) {
                case .action(let action): return action
                default: return nil
                }
            }
            .first { $0.key == key }
    }

    private var currentFormatChoice: SettingItem.FormatChoice? {
        allItems
            .compactMap { item -> SettingItem.FormatChoice? in
                switch onEnum(of: item) {
                case .formatChoice(let choice): return choice
                default: return nil
                }
            }
            .first
    }

    private func preferredFormats(for formatChoice: SettingItem.FormatChoice) -> [FormatUiModel] {
        let filtered = catalogStore.formatItems.filter { !$0.isHistoric || $0.id == formatChoice.selectedFormatId }
        let effectiveId: Int32 = formatChoice.selectedFormatId == 0 ? formatChoice.defaultFormatId : formatChoice.selectedFormatId
        return FormatSorter.shared.sorted(formats: filtered, defaultFormatId: KotlinInt(int: effectiveId))
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                ForEach(settingsStore.settingSections, id: \.title) { section in
                    sectionHeader(section.title)
                    sectionCard(section)
                }
                Text(SettingsRepository.companion.DISCLAIMER_TEXT)
                    .font(.caption2)
                    .italic()
                    .foregroundColor(.secondary)
                    .padding(.horizontal, AppTokens.settingsSectionHorizontalPadding)
                    .padding(.vertical, 24)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .background(Color(.systemBackground))
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
                    formats: preferredFormats(for: formatChoice),
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
                    catalogStore.reload()
                    favoritesStore.syncState()
                }
                confirmActionKey = nil
            }
        } message: {
            Text(confirmActionItem?.confirmationMessage ?? "")
        }
    }

    private func sectionHeader(_ title: String) -> some View {
        Text(title.uppercased())
            .font(.system(size: AppTokens.settingsSectionHeaderFontSize, weight: .semibold))
            .foregroundColor(.secondary)
            .padding(.horizontal, AppTokens.settingsSectionHorizontalPadding)
            .padding(.top, AppTokens.settingsSectionHeaderTopPadding)
            .padding(.bottom, AppTokens.settingsSectionHeaderBottomPadding)
    }

    private func sectionCard(_ section: SettingsSection) -> some View {
        VStack(spacing: 0) {
            ForEach(Array(section.items.enumerated()), id: \.element.key) { index, item in
                if index > 0 {
                    Rectangle()
                        .fill(Color(.opaqueSeparator))
                        .frame(height: 1)
                        .padding(.leading, AppTokens.settingsRowHorizontalPadding)
                }
                rowView(for: item)
            }
        }
        .background(Color(.systemBackground))
        .clipShape(RoundedRectangle(cornerRadius: AppTokens.cardCornerRadius))
        .overlay(
            RoundedRectangle(cornerRadius: AppTokens.cardCornerRadius)
                .stroke(Color(.opaqueSeparator), lineWidth: AppTokens.standardBorderWidth)
        )
        .padding(.horizontal, AppTokens.settingsSectionHorizontalPadding)
    }

    @ViewBuilder
    private func rowView(for item: SettingItem) -> some View {
        switch onEnum(of: item) {
        case .toggle(let toggle):
            HStack {
                titleSubtitle(title: toggle.title, subtitle: toggle.subtitle)
                Spacer(minLength: AppTokens.settingsRowAccessoryGap)
                Toggle("", isOn: Binding(
                    get: { toggle.isEnabled },
                    set: { settingsStore.setBooleanSetting(key: toggle.key, value: $0) }
                ))
                .labelsHidden()
            }
            .padding(.horizontal, AppTokens.settingsRowHorizontalPadding)
            .padding(.vertical, AppTokens.settingsRowVerticalPadding)
        case .darkModeChoice(let darkModeChoice):
            tappableRow(
                title: darkModeChoice.title,
                subtitle: darkModeChoice.subtitle,
                action: { showDarkModePicker = true }
            ) {
                Text(DarkModeOption.companion.fromId(id: darkModeChoice.selectedModeId).displayName)
                    .foregroundColor(.secondary)
            }
        case .colorChoice(let colorChoice):
            tappableRow(
                title: colorChoice.title,
                subtitle: colorChoice.subtitle,
                action: { showThemePicker = true }
            ) {
                RoundedRectangle(cornerRadius: AppTokens.colorSwatchCornerRadius)
                    .fill(SettingsStore.colorForThemeId(colorChoice.selectedThemeId))
                    .frame(width: AppTokens.colorSwatchSize, height: AppTokens.colorSwatchSize)
            }
        case .formatChoice(let formatChoice):
            FormatChoiceSettingRow(
                formatChoice: formatChoice,
                formats: preferredFormats(for: formatChoice),
                catalogLoading: catalogStore.formatLoading,
                onTap: { showFormatPicker = true }
            )
        case .action(let action):
            tappableRow(
                title: action.title,
                subtitle: action.subtitle,
                action: { confirmActionKey = action.key }
            ) {
                EmptyView()
            }
        case .link(let link):
            if let url = URL(string: link.url) {
                SwiftUI.Link(destination: url) {
                    HStack {
                        titleSubtitle(title: link.title, subtitle: link.subtitle)
                        Spacer(minLength: AppTokens.settingsRowAccessoryGap)
                        Image(systemName: "arrow.up.right")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    .padding(.horizontal, AppTokens.settingsRowHorizontalPadding)
                    .padding(.vertical, AppTokens.settingsRowVerticalPadding)
                    .contentShape(Rectangle())
                }
                .tint(.primary)
            }
        }
    }

    private func titleSubtitle(title: String, subtitle: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(title)
                .foregroundColor(.primary)
            Text(subtitle)
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }

    private func tappableRow<Trailing: View>(
        title: String,
        subtitle: String,
        action: @escaping () -> Void,
        @ViewBuilder trailing: () -> Trailing
    ) -> some View {
        HStack {
            titleSubtitle(title: title, subtitle: subtitle)
            Spacer(minLength: AppTokens.settingsRowAccessoryGap)
            trailing()
        }
        .padding(.horizontal, AppTokens.settingsRowHorizontalPadding)
        .padding(.vertical, AppTokens.settingsRowVerticalPadding)
        .contentShape(Rectangle())
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(.isButton)
        .onTapGesture(perform: action)
    }
}

#Preview {
    let container = DependencyContainer()
    return NavigationStack {
        SettingsView(
            settingsStore: container.settingsStore,
            catalogStore: container.catalogStore,
            favoritesStore: container.favoritesStore
        )
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
        HStack(alignment: .top, spacing: AppTokens.settingsRowAccessoryGap) {
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
        .padding(.horizontal, AppTokens.settingsRowHorizontalPadding)
        .padding(.vertical, AppTokens.settingsRowVerticalPadding)
        .contentShape(Rectangle())
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(.isButton)
        .onTapGesture {
            if canInteract { onTap() }
        }
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
