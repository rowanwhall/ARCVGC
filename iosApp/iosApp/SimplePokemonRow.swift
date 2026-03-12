import SwiftUI

struct SimplePokemonRow: View {
    let imageUrl: String?
    let name: String
    let types: [(name: String, imageUrl: String?)]
    let circleSize: CGFloat
    let spriteSize: CGFloat
    var fontWeight: Font.Weight = .regular

    var body: some View {
        HStack(spacing: 12) {
            PokemonAvatar(
                imageUrl: imageUrl,
                circleSize: circleSize,
                spriteSize: spriteSize
            )

            Text(name)
                .font(.body)
                .fontWeight(fontWeight)
                .foregroundColor(Color(.label))

            Spacer()

            TypeIconRow(types: types)
        }
        .contentShape(Rectangle())
    }
}

#Preview {
    SimplePokemonRow(
        imageUrl: nil,
        name: "Dragonite",
        types: [(name: "Dragon", imageUrl: nil), (name: "Flying", imageUrl: nil)],
        circleSize: 46,
        spriteSize: 64
    )
    .padding()
}
