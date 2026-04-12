import SwiftUI
import Shared

struct PokemonDetailCard: View {
    let pokemon: PokemonDetailUiModel
    var onPokemonClick: ((Int32, String, String?, [String]) -> Void)? = nil

    var body: some View {
        Button {
            onPokemonClick?(pokemon.id, pokemon.name, pokemon.imageUrl, pokemon.types.compactMap { $0.imageUrl })
        } label: {
        ZStack(alignment: .topLeading) {
            // Main centered content
            VStack(spacing: 0) {
                // Image area — pokemon overflows the circle background
                ZStack {
                    PokemonAvatar(
                        imageUrl: pokemon.imageUrl,
                        circleSize: 100,
                        spriteSize: 144
                    )

                    // Item circle background + image — over pokemon image
                    if pokemon.item != nil && isPreview {
                        ZStack {
                            Circle()
                                .fill(Color(.systemGray5))
                            Image("PreviewItem")
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .frame(width: 28, height: 28)
                        }
                        .frame(width: 36, height: 36)
                        .offset(x: 44, y: 44)
                    } else if let item = pokemon.item, let itemImageUrl = item.imageUrl {
                        ItemOverlay(imageUrl: itemImageUrl, name: item.name ?? "")
                            .frame(width: 36, height: 36)
                            .offset(x: 44, y: 44)
                    }
                }
                .frame(width: 144, height: 144)

                Spacer().frame(height: 2)

                // Pokemon name
                Text(pokemon.name)
                    .font(.system(size: 16, weight: .bold))
                    .lineLimit(1)

                Spacer().frame(height: 2)

                // Ability · Item
                if pokemon.abilityName != nil || pokemon.item != nil {
                    HStack(spacing: 0) {
                        if let abilityName = pokemon.abilityName {
                            Text(abilityName)
                                .font(.system(size: 12))
                                .foregroundColor(.secondary)
                        }
                        if let item = pokemon.item {
                            if pokemon.abilityName != nil {
                                Text(" \(AppTokens.bulletSeparator) ")
                                    .font(.system(size: 12))
                                    .foregroundColor(.secondary)
                            }
                            Text(item.name)
                                .font(.system(size: 12))
                                .foregroundColor(.secondary)
                        }
                    }
                    .lineLimit(1)
                }

                // Moves — full-width rows with centered text
                let moves = Array(pokemon.moves.prefix(4))
                if !moves.isEmpty {
                    Spacer().frame(height: 6)
                    VStack(spacing: 2) {
                        HStack(spacing: 4) {
                            if moves.count > 0 {
                                MoveChip(moveName: moves[0])
                            }
                            if moves.count > 1 {
                                MoveChip(moveName: moves[1])
                            }
                        }
                        HStack(spacing: 4) {
                            if moves.count > 2 {
                                MoveChip(moveName: moves[2])
                            }
                            if moves.count > 3 {
                                MoveChip(moveName: moves[3])
                            }
                        }
                    }
                }
            }
            .padding(12)
            .frame(maxWidth: .infinity)

            // Type icons — top start
            TypeIconRow(types: pokemon.types.map { (name: $0.name, imageUrl: $0.imageUrl) })
                .padding(8)

            // Tera type icon — top end
            if pokemon.teraType != nil {
                HStack {
                    Spacer()
                    PreviewAsyncImage(url: pokemon.teraType?.imageUrl, previewAsset: "PreviewTera")
                        .frame(width: 26, height: 26)
                        .padding(8)
                }
            }
        }
        .background(Color(.systemBackground))
        .cornerRadius(8)
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(Color(.opaqueSeparator), lineWidth: AppTokens.standardBorderWidth)
        )
        }
        .buttonStyle(PressableButtonStyle())
    }
}

private struct ItemOverlay: View {
    let imageUrl: String
    let name: String
    @State private var imageLoaded = false

    var body: some View {
        ZStack {
            if imageLoaded {
                Circle()
                    .fill(Color(.systemGray5))
            }
            AsyncImage(url: URL(string: imageUrl)) { phase in
                switch phase {
                case .success(let image):
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .transition(.opacity.animation(.easeIn(duration: 0.2)))
                        .onAppear { imageLoaded = true }
                default:
                    Color.clear
                }
            }
            .frame(width: 28, height: 28)
        }
    }
}

struct MoveChip: View {
    let moveName: String

    var body: some View {
        Text(moveName)
            .font(.system(size: 11))
            .foregroundColor(Color(.label))
            .lineLimit(1)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 6)
            .background(Color(.secondarySystemBackground))
            .cornerRadius(AppTokens.moveChipCornerRadius)
    }
}

#Preview {
    let samplePokemon = PokemonDetailUiModel(
        id: 149,
        name: "Dragonite",
        imageUrl: nil,
        item: ItemUiModel(id: 1, name: "Choice Band", imageUrl: nil),
        abilityName: "Multiscale",
        moves: ["Dragon Claw", "Extreme Speed", "Earthquake", "Ice Punch"],
        types: [
            TypeUiModel(name: "Dragon", imageUrl: nil),
            TypeUiModel(name: "Flying", imageUrl: nil)
        ],
        teraType: TeraTypeUiModel(id: 1, name: "Normal", imageUrl: nil)
    )

    PokemonDetailCard(pokemon: samplePokemon)
        .padding()
}

#Preview("Closed Teamsheet") {
    let closedPokemon = PokemonDetailUiModel(
        id: 149,
        name: "Dragonite",
        imageUrl: nil,
        item: nil,
        abilityName: nil,
        moves: [],
        types: [
            TypeUiModel(name: "Dragon", imageUrl: nil),
            TypeUiModel(name: "Flying", imageUrl: nil)
        ],
        teraType: nil
    )

    PokemonDetailCard(pokemon: closedPokemon)
        .padding()
}
