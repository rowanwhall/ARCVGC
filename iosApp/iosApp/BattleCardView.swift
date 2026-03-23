import SwiftUI
import Shared

private let cardCornerRadius: CGFloat = 12

struct BattleCardView: View {
    let uiModel: BattleCardUiModel
    var showWinnerHighlight: Bool = true
    var onTap: (() -> Void)? = nil

    var body: some View {
        VStack(spacing: 0) {
            // Header row with timestamp and rating
            HStack {
                Text(uiModel.formattedTime)
                    .font(.caption)
                    .foregroundColor(Color(.label).opacity(0.75))
                Spacer()
                Text(uiModel.rating)
                    .font(.caption)
                    .foregroundColor(Color(.label).opacity(0.75))
            }
            .padding(.horizontal, 8)

            Spacer().frame(height: 8)

            VStack(spacing: 8) {
                PlayerTeamSection(player: uiModel.player1, showWinnerHighlight: showWinnerHighlight)
                VsDivider()
                    .padding(.horizontal, 16)
                PlayerTeamSection(player: uiModel.player2, showWinnerHighlight: showWinnerHighlight)
            }

            Spacer().frame(height: 8)

            Text(uiModel.formatName)
                .font(.caption)
                .foregroundColor(Color(.label).opacity(0.75))
                .frame(maxWidth: .infinity, alignment: .center)
        }
        .padding(8)
        .background(Color(.systemBackground))
        .cornerRadius(cardCornerRadius)
        .overlay(
            RoundedRectangle(cornerRadius: cardCornerRadius)
                .stroke(Color(.opaqueSeparator), lineWidth: 1)
        )
        .contentShape(Rectangle())
        .onTapGesture {
            onTap?()
        }
    }
}

struct PlayerTeamSection: View {
    let player: PlayerUiModel
    var showWinnerHighlight: Bool = true
    @Environment(\.themeColor) private var themeColor

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Player name at top-left
            let isWinner = showWinnerHighlight && player.isWinner?.boolValue == true
            let isLoser = showWinnerHighlight && player.isWinner?.boolValue == false
            Text(player.name)
                .font(.caption)
                .fontWeight(.medium)
                .foregroundColor(isWinner ? themeColor : isLoser ? Color(.label).opacity(0.75) : Color(.label))
                .padding(.horizontal, 4)
                .padding(.vertical, 2)

            // Team row
            HStack(spacing: 0) {
                ForEach(Array(player.team.enumerated()), id: \.offset) { _, pokemonSlot in
                    PokemonWithItem(pokemonSlot: pokemonSlot)
                }
            }
        }
        .padding(4)
        .cornerRadius(cardCornerRadius)
        .overlay(
            RoundedRectangle(cornerRadius: cardCornerRadius)
                .stroke(
                    showWinnerHighlight && player.isWinner?.boolValue == true ? themeColor : Color(.separator),
                    lineWidth: showWinnerHighlight && player.isWinner?.boolValue == true ? 2 : 1
                )
        )
    }
}

struct PokemonWithItem: View {
    let pokemonSlot: PokemonSlotUiModel

    var body: some View {
        GeometryReader { geometry in
            let size = geometry.size.width
            let itemSize = size * 0.35
            let teraTypeSize = size * 0.35 * 0.75

            FillPokemonAvatar(imageUrl: pokemonSlot.imageUrl)
                .overlay(alignment: .bottomTrailing) {
                    if pokemonSlot.item != nil {
                        PreviewAsyncImage(url: pokemonSlot.item?.imageUrl, previewAsset: "PreviewItem")
                            .frame(width: itemSize, height: itemSize)
                    }
                }
                .overlay(alignment: .topTrailing) {
                    if pokemonSlot.teraType != nil {
                        PreviewAsyncImage(url: pokemonSlot.teraType?.imageUrl, previewAsset: "PreviewTera")
                            .frame(width: teraTypeSize, height: teraTypeSize)
                            .offset(x: -(itemSize - teraTypeSize) / 2, y: (itemSize - teraTypeSize) / 2)
                    }
                }
        }
        .aspectRatio(1, contentMode: .fit)
    }
}

// MARK: - Preview

#Preview {
    let samplePokemonSlot = PokemonSlotUiModel(
        name: "Dragonite",
        imageUrl: nil,
        item: ItemUiModel(id: 1, name: "Choice Band", imageUrl: nil),
        teraType: TeraTypeUiModel(id: 1, name: "Normal", imageUrl: nil)
    )

    let samplePlayer1 = PlayerUiModel(
        name: "Player1",
        isWinner: true,
        team: (0..<6).map { _ in samplePokemonSlot }
    )

    let samplePlayer2 = PlayerUiModel(
        name: "Opponent",
        isWinner: false,
        team: (0..<6).map { _ in samplePokemonSlot }
    )

    let sampleUiModel = BattleCardUiModel(
        id: 1,
        player1: samplePlayer1,
        player2: samplePlayer2,
        formatName: "VGC 2026 Reg H",
        rating: "1542",
        formattedTime: "Feb 8, 5:03 PM"
    )

    return BattleCardView(uiModel: sampleUiModel)
        .padding(16)
}
