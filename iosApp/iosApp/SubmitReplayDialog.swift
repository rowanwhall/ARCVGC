import SwiftUI
import UIKit
import Shared

struct SubmitReplayDialog: View {
    @StateObject private var viewModel: SubmitReplayViewModel
    @State private var url: String = ""
    @State private var hasClipboardText: Bool = false
    @FocusState private var isFieldFocused: Bool

    private let onDismiss: () -> Void

    init(repository: BattleRepository, onDismiss: @escaping () -> Void) {
        _viewModel = StateObject(wrappedValue: SubmitReplayViewModel(repository: repository))
        self.onDismiss = onDismiss
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Submit replay to ARC")
                .font(.headline)

            Text("Have a battle you'd like to share with the world? Paste the replay link here!")
                .font(.subheadline)
                .foregroundColor(.secondary)
                .fixedSize(horizontal: false, vertical: true)

            TextField("https://replay.pokemonshowdown.com/...", text: $url)
                .textFieldStyle(.roundedBorder)
                .keyboardType(.URL)
                .textContentType(.URL)
                .autocorrectionDisabled(true)
                .textInputAutocapitalization(.never)
                .disabled(viewModel.isSubmitting)
                .focused($isFieldFocused)

            if let error = viewModel.error {
                Text(error)
                    .font(.footnote)
                    .foregroundColor(.red)
            }

            if hasClipboardText {
                Button {
                    if let pasted = UIPasteboard.general.string {
                        url = pasted
                    }
                } label: {
                    Label("Paste from clipboard", systemImage: "doc.on.clipboard")
                }
                .disabled(viewModel.isSubmitting)
            }

            Spacer(minLength: 0)

            HStack {
                Spacer()
                Button("Cancel") {
                    viewModel.reset()
                    onDismiss()
                }
                .disabled(viewModel.isSubmitting)

                Button {
                    viewModel.submit(
                        replayUrl: url.trimmingCharacters(in: .whitespacesAndNewlines),
                        onSuccess: { onDismiss() }
                    )
                } label: {
                    if viewModel.isSubmitting {
                        ProgressView().tint(.white)
                    } else {
                        Text("Submit")
                    }
                }
                .buttonStyle(.borderedProminent)
                .disabled(viewModel.isSubmitting || url.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
            }
        }
        .padding(20)
        .task {
            hasClipboardText = UIPasteboard.general.hasStrings
            try? await Task.sleep(nanoseconds: 300_000_000)
            isFieldFocused = true
        }
    }
}

#Preview("Initial") {
    SubmitReplayDialog(
        repository: BattleRepository(apiService: ApiService()),
        onDismiss: {}
    )
}
