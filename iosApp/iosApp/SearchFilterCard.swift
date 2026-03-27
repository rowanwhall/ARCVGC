import SwiftUI
import Shared

struct SearchFilterCard: View {
    let slot: SearchFilterSlotUiModel
    var onRemove: () -> Void
    var onItemTap: () -> Void
    var onTeraTap: () -> Void
    var compact: Bool = false

    var body: some View {
        HStack(spacing: compact ? 4 : 12) {
            if compact {
                // Compact mode: avatar + stacked badges + menu + remove
                compactContent
            } else {
                // Full mode: avatar + name + inline item/tera + remove
                fullContent
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

    @ViewBuilder
    private var fullContent: some View {
        PokemonAvatar(imageUrl: slot.pokemonImageUrl, circleSize: 40, spriteSize: 56)
        Text(slot.pokemonName)
            .font(.system(size: 16, weight: .bold))
            .lineLimit(1)
            .minimumScaleFactor(0.6)
        Spacer()
        if SearchFilterRestrictions.shared.canFilterByItem(pokemonName: slot.pokemonName) {
            if slot.item != nil && (isPreview || slot.item?.imageUrl != nil) {
                Button { onItemTap() } label: {
                    PreviewAsyncImage(url: slot.item?.imageUrl, previewAsset: "PreviewItem")
                        .frame(width: 32, height: 32)
                }
            } else {
                SmallFilterButton(label: slot.item?.name ?? "Item") { onItemTap() }
            }
        }
        if SearchFilterRestrictions.shared.canFilterByTeraType(pokemonName: slot.pokemonName) {
            if slot.teraType != nil && (isPreview || slot.teraType?.imageUrl != nil) {
                Button { onTeraTap() } label: {
                    PreviewAsyncImage(url: slot.teraType?.imageUrl, previewAsset: "PreviewTera")
                        .frame(width: 32, height: 32)
                }
            } else {
                SmallFilterButton(label: slot.teraType?.name ?? "Tera") { onTeraTap() }
            }
        }
    }

    @ViewBuilder
    private var compactContent: some View {
        let canItem = SearchFilterRestrictions.shared.canFilterByItem(pokemonName: slot.pokemonName)
        let canTera = SearchFilterRestrictions.shared.canFilterByTeraType(pokemonName: slot.pokemonName)
        let hasItemIcon = slot.item != nil && (isPreview || slot.item?.imageUrl != nil)
        let hasTeraIcon = slot.teraType != nil && (isPreview || slot.teraType?.imageUrl != nil)

        // Avatar + stacked sub-filter badges
        HStack(spacing: -8) {
            PokemonAvatar(imageUrl: slot.pokemonImageUrl, circleSize: 32, spriteSize: 44)
                .zIndex(3)
            if hasItemIcon {
                PreviewAsyncImage(url: slot.item?.imageUrl, previewAsset: "PreviewItem")
                    .frame(width: 18, height: 18)
                    .frame(width: 24, height: 24)
                    .background(Color(.systemGray5))
                    .clipShape(Circle())
                    .overlay(Circle().stroke(Color(.opaqueSeparator), lineWidth: 1))
                    .zIndex(2)
            }
            if hasTeraIcon {
                PreviewAsyncImage(url: slot.teraType?.imageUrl, previewAsset: "PreviewTera")
                    .frame(width: 18, height: 18)
                    .frame(width: 24, height: 24)
                    .background(Color(.systemGray5))
                    .clipShape(Circle())
                    .overlay(Circle().stroke(Color(.opaqueSeparator), lineWidth: 1))
                    .zIndex(1)
            }
        }

        Spacer()

        // MoreVert menu
        if canItem || canTera {
            Menu {
                if canItem {
                    let itemLabel = slot.item.map { "Item: \($0.name)" } ?? "Item"
                    Button(itemLabel) { onItemTap() }
                }
                if canTera {
                    let teraLabel = slot.teraType.map { "Tera: \($0.name)" } ?? "Tera"
                    Button(teraLabel) { onTeraTap() }
                }
            } label: {
                Image(systemName: "ellipsis")
                    .font(.system(size: 14))
                    .foregroundColor(.secondary)
                    .frame(width: 32, height: 32)
            }
        }
    }
}

private struct SmallFilterButton: View {
    let label: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(label)
                .font(.system(size: AppTokens.smallFilterButtonFontSize))
                .foregroundColor(.secondary)
                .padding(.horizontal, AppTokens.smallFilterButtonHorizontalPadding)
                .padding(.vertical, AppTokens.smallFilterButtonVerticalPadding)
                .background(Color(.systemGray5))
                .cornerRadius(AppTokens.smallFilterButtonCornerRadius)
        }
    }
}

#Preview("Empty Filters") {
    SearchFilterCard(
        slot: SearchFilterSlotUiModel(
            pokemonId: 149,
            pokemonName: "Dragonite",
            pokemonImageUrl: nil,
            item: nil,
            teraType: nil
        ),
        onRemove: {},
        onItemTap: {},
        onTeraTap: {}
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
            teraType: TeraTypeUiModel(id: 1, name: "Normal", imageUrl: nil)
        ),
        onRemove: {},
        onItemTap: {},
        onTeraTap: {}
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
            teraType: TeraTypeUiModel(id: 1, name: "Normal", imageUrl: nil)
        ),
        onRemove: {},
        onItemTap: {},
        onTeraTap: {},
        compact: true
    )
    .padding()
}
