import SwiftUI
import Shared

struct TutorialDialog: View {
    let onDismiss: () -> Void

    @State private var currentPage: Int = 0

    private var steps: [TutorialStep] { TutorialConfig.shared.steps }

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Spacer()
                Button {
                    onDismiss()
                } label: {
                    Image(systemName: "xmark")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(Color(.secondaryLabel))
                        .padding(8)
                }
                .buttonStyle(.plain)
                .accessibilityLabel("Close")
            }
            .padding(.horizontal, 8)
            .padding(.top, 8)

            TabView(selection: $currentPage) {
                ForEach(Array(steps.enumerated()), id: \.offset) { index, step in
                    TutorialPageView(step: step)
                        .tag(index)
                        .padding(.horizontal, 24)
                }
            }
            // Hide the built-in indicator — it overlays page content. We render
            // our own dots in a reserved row below instead.
            .tabViewStyle(.page(indexDisplayMode: .never))

            HStack(spacing: 8) {
                ForEach(0..<steps.count, id: \.self) { index in
                    Circle()
                        .fill(index == currentPage ? Color.accentColor : Color(.tertiaryLabel))
                        .frame(
                            width: index == currentPage ? 9 : 7,
                            height: index == currentPage ? 9 : 7
                        )
                }
            }
            .padding(.top, 12)
            .padding(.bottom, 16)
        }
    }
}

private struct TutorialPageView: View {
    let step: TutorialStep

    var body: some View {
        let strings = TutorialOverrides.resolve(id: step.id)
        let assetName = TutorialImages.assetName(for: step.imageId)

        ScrollView(showsIndicators: false) {
            VStack(spacing: 20) {
                if let assetName {
                    Image(assetName)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(maxWidth: .infinity)
                        .frame(maxHeight: 260)
                }

                Text(strings?.title ?? "")
                    .font(.title2.weight(.semibold))
                    .multilineTextAlignment(.center)

                Text(strings?.body ?? "")
                    .font(.body)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)

                Spacer(minLength: 0)
            }
            .frame(maxWidth: .infinity)
            .padding(.top, 8)
            .padding(.bottom, 8)
        }
    }
}

#Preview {
    TutorialDialog(onDismiss: {})
}
