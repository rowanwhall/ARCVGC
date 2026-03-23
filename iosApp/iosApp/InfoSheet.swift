import SwiftUI
import Shared

struct InfoSheet: View {
    let content: InfoContent
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(content.title)
                .font(.headline)

            Text(content.body)
                .font(.body)
                .foregroundColor(Color(.secondaryLabel))

            Spacer()
        }
        .padding()
        .overlay(alignment: .topTrailing) {
            Button {
                dismiss()
            } label: {
                Image(systemName: "xmark")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(Color(.secondaryLabel))
                    .frame(width: 28, height: 28)
                    .background(Color(.systemGray5))
                    .clipShape(Circle())
            }
            .buttonStyle(.plain)
            .padding(12)
        }
        .presentationDetents([.medium])
        .presentationDragIndicator(.hidden)
    }
}

#Preview {
    InfoSheet(content: InfoContent(title: "About Replays", body: "Replays open on Pokémon Showdown's website.", imageUrl: nil))
}
