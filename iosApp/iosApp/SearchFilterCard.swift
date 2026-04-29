import SwiftUI
import Shared

struct SearchFilterCard: View {
    let slot: SearchFilterSlotUiModel
    var onRemove: () -> Void
    var onItemTap: () -> Void
    var onTeraTap: () -> Void
    var onAbilityTap: () -> Void
    var compact: Bool = false
    var selectedFormat: FormatUiModel? = nil

    @Environment(\.horizontalSizeClass) private var horizontalSizeClass

    private var isExpanded: Bool {
        horizontalSizeClass == .regular
    }

    private var showSetFilters: Bool {
        guard let format = selectedFormat else { return true }
        return format.isOpenTeamsheet
    }

    var body: some View {
        if isExpanded {
            expandedCard
        } else {
            compactCard
        }
    }

    // MARK: - Compact (iPhone + two-team mode)

    private var compactCard: some View {
        HStack(spacing: compact ? 4 : 8) {
            PokemonAvatar(imageUrl: slot.pokemonImageUrl, circleSize: 32, spriteSize: 44)

            if !compact {
                Text(slot.pokemonName)
                    .font(.system(size: 16, weight: .bold))
                    .lineLimit(1)
                    .minimumScaleFactor(0.6)
            }

            if showSetFilters {
                badgesContent
            }

            Spacer()

            // Context menu
            if showSetFilters {
                filterMenu
            }

            // Remove button
            Button { onRemove() } label: {
                Image(systemName: "xmark")
                    .font(.system(size: 14))
                    .foregroundColor(.secondary)
            }
            .frame(width: 32, height: 32)
        }
        .padding(compact ? 8 : 12)
        .background(Color(.systemBackground))
        .cornerRadius(AppTokens.cardCornerRadius)
        .overlay(
            RoundedRectangle(cornerRadius: AppTokens.cardCornerRadius)
                .stroke(Color(.opaqueSeparator), lineWidth: AppTokens.standardBorderWidth)
        )
    }

    // MARK: - Expanded (iPad single-team)

    private var expandedCard: some View {
        HStack(spacing: 12) {
            PokemonAvatar(imageUrl: slot.pokemonImageUrl, circleSize: 40, spriteSize: 56)

            Text(slot.pokemonName)
                .font(.system(size: 16, weight: .bold))
                .lineLimit(1)
                .minimumScaleFactor(0.6)
                .frame(maxWidth: .infinity, alignment: .leading)

            if showSetFilters {
                HStack(spacing: 8) {
                    InlineItemButton(slot: slot, onTap: onItemTap)
                    InlineTeraButton(slot: slot, onTap: onTeraTap)
                    InlineAbilityButton(slot: slot, onTap: onAbilityTap)
                }
            }

            Button { onRemove() } label: {
                Image(systemName: "xmark")
                    .font(.system(size: 14))
                    .foregroundColor(.secondary)
            }
            .frame(width: 32, height: 32)
        }
        .padding(12)
        .background(Color(.systemBackground))
        .cornerRadius(AppTokens.cardCornerRadius)
        .overlay(
            RoundedRectangle(cornerRadius: AppTokens.cardCornerRadius)
                .stroke(Color(.opaqueSeparator), lineWidth: AppTokens.standardBorderWidth)
        )
    }

    // MARK: - Compact helpers (badges + menu)

    @ViewBuilder
    private var badgesContent: some View {
        let hasItemIcon = slot.item != nil && (isPreview || slot.item?.imageUrl != nil)
        let hasTeraIcon = slot.teraType != nil && (isPreview || slot.teraType?.imageUrl != nil)
        let hasAbility = slot.ability != nil

        if hasItemIcon || hasTeraIcon || hasAbility {
            HStack(spacing: -8) {
                if hasItemIcon {
                    PreviewAsyncImage(url: slot.item?.imageUrl, previewAsset: "PreviewItem")
                        .frame(width: 18, height: 18)
                        .frame(width: 24, height: 24)
                        .background(Color(.systemGray5))
                        .clipShape(Circle())
                        .overlay(Circle().stroke(Color(.opaqueSeparator), lineWidth: 1))
                        .zIndex(1)
                }
                if hasTeraIcon {
                    PreviewAsyncImage(url: slot.teraType?.imageUrl, previewAsset: "PreviewTera")
                        .frame(width: 18, height: 18)
                        .frame(width: 24, height: 24)
                        .background(Color(.systemGray5))
                        .clipShape(Circle())
                        .overlay(Circle().stroke(Color(.opaqueSeparator), lineWidth: 1))
                        .zIndex(2)
                }
                if hasAbility {
                    Text(abilityInitials(slot.ability!.name))
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(Color(.label))
                        .frame(width: 24, height: 24)
                        .background(Color(.systemGray5))
                        .clipShape(Circle())
                        .overlay(Circle().stroke(Color(.opaqueSeparator), lineWidth: 1))
                        .zIndex(3)
                }
            }
        }
    }

    @ViewBuilder
    private var filterMenu: some View {
        let canItem = SearchFilterRestrictions.shared.canFilterByItem(pokemonName: slot.pokemonName)
        let canTera = SearchFilterRestrictions.shared.canFilterByTeraType(pokemonName: slot.pokemonName)

        Menu {
            if canItem {
                let itemLabel = slot.item.map { "Item: \($0.name)" } ?? "Item"
                Button(itemLabel) { onItemTap() }
            }
            if canTera {
                let teraLabel = slot.teraType.map { "Tera: \($0.name)" } ?? "Tera"
                Button(teraLabel) { onTeraTap() }
            }
            let abilityLabel = slot.ability.map { "Ability: \($0.name)" } ?? "Ability"
            Button(abilityLabel) { onAbilityTap() }
        } label: {
            Image(systemName: "ellipsis")
                .font(.system(size: 14))
                .foregroundColor(.secondary)
                .frame(width: 32, height: 32)
        }
    }
}

// MARK: - Inline buttons (iPad expanded)

private struct InlineItemButton: View {
    let slot: SearchFilterSlotUiModel
    var onTap: () -> Void

    var body: some View {
        if !SearchFilterRestrictions.shared.canFilterByItem(pokemonName: slot.pokemonName) {
            EmptyView()
        } else if let item = slot.item, let imageUrl = item.imageUrl, let url = URL(string: imageUrl) {
            Button { onTap() } label: {
                AsyncImage(url: url) { image in
                    image.resizable().scaledToFit()
                } placeholder: {
                    Color.clear
                }
                .frame(width: 32, height: 32)
            }
            .buttonStyle(.plain)
        } else {
            SmallFilterButton(label: slot.item?.name ?? "Item", onTap: onTap)
        }
    }
}

private struct InlineTeraButton: View {
    let slot: SearchFilterSlotUiModel
    var onTap: () -> Void

    var body: some View {
        if !SearchFilterRestrictions.shared.canFilterByTeraType(pokemonName: slot.pokemonName) {
            EmptyView()
        } else if let teraType = slot.teraType, let imageUrl = teraType.imageUrl, let url = URL(string: imageUrl) {
            Button { onTap() } label: {
                AsyncImage(url: url) { image in
                    image.resizable().scaledToFit()
                } placeholder: {
                    Color.clear
                }
                .frame(width: 32, height: 32)
            }
            .buttonStyle(.plain)
        } else {
            SmallFilterButton(label: slot.teraType?.name ?? "Tera", onTap: onTap)
        }
    }
}

private struct InlineAbilityButton: View {
    let slot: SearchFilterSlotUiModel
    var onTap: () -> Void

    var body: some View {
        if let ability = slot.ability {
            Button { onTap() } label: {
                Text(abilityInitials(ability.name))
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(Color(.secondaryLabel))
                    .frame(width: 32, height: 32)
                    .background(Color(.systemGray5))
                    .clipShape(Circle())
                    .overlay(Circle().stroke(Color(.opaqueSeparator), lineWidth: 1))
            }
            .buttonStyle(.plain)
        } else {
            SmallFilterButton(label: "Ability", onTap: onTap)
        }
    }
}

private struct SmallFilterButton: View {
    let label: String
    var onTap: () -> Void

    var body: some View {
        Button { onTap() } label: {
            Text(label)
                .font(.system(size: AppTokens.smallFilterButtonFontSize))
                .foregroundColor(Color(.secondaryLabel))
                .padding(.horizontal, AppTokens.smallFilterButtonHorizontalPadding)
                .padding(.vertical, AppTokens.smallFilterButtonVerticalPadding)
                .background(Color(.systemGray5))
                .cornerRadius(AppTokens.smallFilterButtonCornerRadius)
        }
        .buttonStyle(.plain)
    }
}

private func abilityInitials(_ name: String) -> String {
    AbilityInitialsKt.abilityInitials(name: name)
}

#Preview("Empty Filters") {
    SearchFilterCard(
        slot: SearchFilterSlotUiModel(
            pokemonId: 149,
            pokemonName: "Dragonite",
            pokemonImageUrl: nil,
            item: nil,
            teraType: nil,
            ability: nil
        ),
        onRemove: {},
        onItemTap: {},
        onTeraTap: {},
        onAbilityTap: {}
    )
    .padding()
}

#Preview("With Filters") {
    SearchFilterCard(
        slot: SearchFilterSlotUiModel(
            pokemonId: 149,
            pokemonName: "Dragonite",
            pokemonImageUrl: nil,
            item: ItemUiModel(id: 1, name: "Choice Band", imageUrl: nil),
            teraType: TeraTypeUiModel(id: 1, name: "Normal", imageUrl: nil),
            ability: AbilityUiModel(id: 1, name: "Inner Focus")
        ),
        onRemove: {},
        onItemTap: {},
        onTeraTap: {},
        onAbilityTap: {}
    )
    .padding()
}

#Preview("Compact") {
    SearchFilterCard(
        slot: SearchFilterSlotUiModel(
            pokemonId: 149,
            pokemonName: "Dragonite",
            pokemonImageUrl: nil,
            item: ItemUiModel(id: 1, name: "Choice Band", imageUrl: nil),
            teraType: TeraTypeUiModel(id: 1, name: "Normal", imageUrl: nil),
            ability: AbilityUiModel(id: 1, name: "Inner Focus")
        ),
        onRemove: {},
        onItemTap: {},
        onTeraTap: {},
        onAbilityTap: {},
        compact: true
    )
    .padding()
}

#Preview("Closed Teamsheet") {
    SearchFilterCard(
        slot: SearchFilterSlotUiModel(
            pokemonId: 149,
            pokemonName: "Dragonite",
            pokemonImageUrl: nil,
            item: ItemUiModel(id: 1, name: "Choice Band", imageUrl: nil),
            teraType: TeraTypeUiModel(id: 1, name: "Normal", imageUrl: nil),
            ability: AbilityUiModel(id: 1, name: "Inner Focus")
        ),
        onRemove: {},
        onItemTap: {},
        onTeraTap: {},
        onAbilityTap: {},
        selectedFormat: FormatUiModel(
            id: 1,
            displayName: "Reg G (Closed Sheets)",
            isHistoric: false,
            isOpenTeamsheet: false,
            isOfficial: false,
            hasSeries: false
        )
    )
    .padding()
}
