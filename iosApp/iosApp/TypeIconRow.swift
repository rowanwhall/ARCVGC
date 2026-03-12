import SwiftUI

struct TypeIconRow: View {
    let types: [(name: String, imageUrl: String?)]
    var iconSize: CGFloat = 24

    private let previewAssets = ["PreviewType1", "PreviewType2"]

    var body: some View {
        HStack(spacing: 2) {
            ForEach(Array(types.enumerated()), id: \.offset) { index, type in
                PreviewAsyncImage(
                    url: type.imageUrl,
                    previewAsset: previewAssets[min(index, previewAssets.count - 1)]
                )
                .frame(width: iconSize, height: iconSize)
            }
        }
    }
}

#Preview {
    TypeIconRow(types: [
        (name: "Dragon", imageUrl: nil),
        (name: "Flying", imageUrl: nil)
    ])
    .padding()
}
