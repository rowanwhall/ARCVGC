import SwiftUI
import Shared

struct SearchFilterCard: View {
    let slot: SearchFilterSlotUiModel
    var onRemove: () -> Void
    var onItemTap: () -> Void
    var onTeraTap: () -> Void
    var onAbilityTap: () -> Void
    var compact: Bool = false

    var body: some View {
        HStack(spacing: compact ? 4 : 8) {
            PokemonAvatar(imageUrl: slot.pokemonImageUrl, circleSize: 32, spriteSize: 44)

            if !compact {
                Text(slot.pokemonName)
                    .font(.system(size: 16, weight: .bold))
                    .lineLimit(1)
                    .minimumScaleFactor(0.6)
            }

            badgesContent

            Spacer()

            // Context menu
            filterMenu

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

private func abilityInitials(_ name: String) -> String {
    let cleaned = name.replacingOccurrences(of: "\\s*\\(.*?\\)", with: "", options: .regularExpression)
    let words = cleaned.split(separator: " ").prefix(2)
    return words.map { String($0.prefix(1)).uppercased() }.joined()
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
