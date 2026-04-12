import SwiftUI

let isPreview = ProcessInfo.processInfo.environment["XCODE_RUNNING_FOR_PREVIEWS"] == "1"

// MARK: - Silhouette Modifier

extension View {
    /// When `useSilhouettes` is enabled, renders the view as a solid black shape.
    @ViewBuilder
    func ifSilhouette() -> some View {
        if useSilhouettes {
            self
                .foregroundStyle(.black)
                .colorMultiply(.black)
        } else {
            self
        }
    }
}

// MARK: - Theme Color Environment Key

private struct ThemeColorKey: EnvironmentKey {
    static let defaultValue: Color = Color(red: 0.863, green: 0.184, blue: 0.208) // Red
}

extension EnvironmentValues {
    var themeColor: Color {
        get { self[ThemeColorKey.self] }
        set { self[ThemeColorKey.self] = newValue }
    }
}

// MARK: - Pokeball Circle

struct PokeballCircle: View {
    @Environment(\.themeColor) var themeColor

    var body: some View {
        Canvas { context, size in
            let strokeWidth = min(size.width, size.height) * 0.04
            let inset = strokeWidth / 2
            let drawRect = CGRect(x: inset, y: inset, width: size.width - strokeWidth, height: size.height - strokeWidth)
            let center = CGPoint(x: size.width / 2, y: size.height / 2)
            let radius = min(drawRect.width, drawRect.height) / 2

            // Top half (theme color)
            var topPath = Path()
            topPath.addArc(center: center, radius: radius, startAngle: .degrees(180), endAngle: .degrees(0), clockwise: false)
            topPath.closeSubpath()
            context.fill(topPath, with: .color(themeColor))

            // Bottom half (white)
            var bottomPath = Path()
            bottomPath.addArc(center: center, radius: radius, startAngle: .degrees(0), endAngle: .degrees(180), clockwise: false)
            bottomPath.closeSubpath()
            context.fill(bottomPath, with: .color(.white))

            // Horizontal line
            var linePath = Path()
            linePath.move(to: CGPoint(x: center.x - radius, y: center.y))
            linePath.addLine(to: CGPoint(x: center.x + radius, y: center.y))
            context.stroke(linePath, with: .color(.black), lineWidth: strokeWidth)

            // Circle outline
            context.stroke(Path(ellipseIn: drawRect), with: .color(.black), lineWidth: strokeWidth)

            // Center button
            let buttonRadius = radius * 0.24
            let buttonRect = CGRect(x: center.x - buttonRadius, y: center.y - buttonRadius, width: buttonRadius * 2, height: buttonRadius * 2)
            context.fill(Path(ellipseIn: buttonRect), with: .color(.white))
            context.stroke(Path(ellipseIn: buttonRect), with: .color(.black), lineWidth: strokeWidth)
        }
    }
}

// MARK: - Pokemon Avatar

struct PokemonAvatar: View {
    let imageUrl: String?
    let circleSize: CGFloat
    let spriteSize: CGFloat

    var body: some View {
        ZStack {
            if useSilhouettes {
                Circle()
                    .fill(Color(.systemGray5))
                    .frame(width: circleSize, height: circleSize)
            } else {
                PokeballCircle()
                    .frame(width: circleSize, height: circleSize)
            }
            PreviewAsyncImage(url: imageUrl, previewAsset: "PreviewPokemon")
                .frame(width: spriteSize, height: spriteSize)
                .ifSilhouette()
        }
        .frame(width: spriteSize, height: spriteSize)
    }
}

struct FillPokemonAvatar: View {
    let imageUrl: String?
    var circleFraction: CGFloat = 0.7

    var body: some View {
        GeometryReader { geometry in
            let size = geometry.size.width
            let circleSize = size * circleFraction

            ZStack {
                if useSilhouettes {
                    Circle()
                        .fill(Color(.systemGray5))
                        .frame(width: circleSize, height: circleSize)
                } else {
                    PokeballCircle()
                        .frame(width: circleSize, height: circleSize)
                }
                PreviewAsyncImage(url: imageUrl, previewAsset: "PreviewPokemon")
                    .frame(width: size, height: size)
                    .ifSilhouette()
            }
            .frame(width: size, height: size)
        }
        .aspectRatio(1, contentMode: .fit)
    }
}

#Preview("PokemonAvatar") {
    PokemonAvatar(imageUrl: nil, circleSize: 100, spriteSize: 144)
}

#Preview("FillPokemonAvatar") {
    FillPokemonAvatar(imageUrl: nil)
        .frame(width: 100, height: 100)
}
