import SwiftUI
import Shared

struct SearchFilterCard: View {
    let slot: SearchFilterSlotUiModel
    var onRemove: () -> Void
    var onItemTap: () -> Void
    var onTeraTap: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            // Pokemon image — circle background with pokemon overlaying it
            PokemonAvatar(
                imageUrl: slot.pokemonImageUrl,
                circleSize: 40,
                spriteSize: 56
            )

            // Pokemon name - auto-shrinks to fit
            Text(slot.pokemonName)
                .font(.system(size: 16, weight: .bold))
                .lineLimit(1)
                .minimumScaleFactor(0.6)

            Spacer()

            // Item button / image
            if SearchFilterRestrictions.shared.canFilterByItem(pokemonName: slot.pokemonName) {
                if slot.item != nil && (isPreview || slot.item?.imageUrl != nil) {
                    Button { onItemTap() } label: {
                        PreviewAsyncImage(url: slot.item?.imageUrl, previewAsset: "PreviewItem")
                            .frame(width: 32, height: 32)
                    }
                } else {
                    SmallFilterButton(label: slot.item?.name ?? "Item") {
                        onItemTap()
                    }
                }
            }

            // Tera button / image
            if SearchFilterRestrictions.shared.canFilterByTeraType(pokemonName: slot.pokemonName) {
                if slot.teraType != nil && (isPreview || slot.teraType?.imageUrl != nil) {
                    Button { onTeraTap() } label: {
                        PreviewAsyncImage(url: slot.teraType?.imageUrl, previewAsset: "PreviewTera")
                            .frame(width: 32, height: 32)
                    }
                } else {
                    SmallFilterButton(label: slot.teraType?.name ?? "Tera") {
                        onTeraTap()
                    }
                }
            }

            // Remove button
            Button { onRemove() } label: {
                Image(systemName: "xmark")
                    .font(.system(size: 14))
                    .foregroundColor(.secondary)
            }
            .frame(width: 32, height: 32)
        }
        .padding(12)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color(.opaqueSeparator), lineWidth: 1)
        )
    }
}

private struct SmallFilterButton: View {
    let label: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(label)
                .font(.system(size: 12))
                .foregroundColor(.secondary)
                .padding(.horizontal, 10)
                .padding(.vertical, 6)
                .background(Color(.systemGray5))
                .cornerRadius(6)
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
