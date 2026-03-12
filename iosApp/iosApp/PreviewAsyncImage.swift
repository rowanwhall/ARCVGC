import SwiftUI

/// Renders a local asset image in Xcode preview mode, or an `AsyncImage` at runtime.
///
/// In preview mode the `previewAsset` is always shown (regardless of `url`).
/// At runtime, the network image is loaded when `url` is non-nil; nothing is
/// rendered when `url` is nil.
struct PreviewAsyncImage: View {
    let url: String?
    let previewAsset: String

    var body: some View {
        if isPreview {
            Image(previewAsset)
                .resizable()
                .aspectRatio(contentMode: .fit)
        } else if let url = url {
            AsyncImage(url: URL(string: url)) { phase in
                switch phase {
                case .success(let image):
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .transition(.opacity.animation(.easeIn(duration: 0.2)))
                default:
                    Color.clear
                }
            }
        }
    }
}
