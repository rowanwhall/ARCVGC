import SwiftUI

struct MinRatingPickerSheet: View {
    let selectedRating: Int32?
    var disabledAbove: Int32? = nil
    let onSelect: (Int32?) -> Void

    @Environment(\.dismiss) private var dismiss

    private let ratingOptions: [Int32] = [1000, 1100, 1200, 1300, 1400, 1500, 1600, 1700]

    var body: some View {
        NavigationStack {
            List {
                Button {
                    onSelect(nil)
                    dismiss()
                } label: {
                    HStack {
                        Text("None")
                            .foregroundColor(.primary)
                        Spacer()
                        if selectedRating == nil {
                            Image(systemName: "checkmark")
                                .foregroundColor(.accentColor)
                        }
                    }
                }
                ForEach(ratingOptions, id: \.self) { rating in
                    let isDisabled = disabledAbove.map { rating >= $0 } ?? false
                    Button {
                        onSelect(rating)
                        dismiss()
                    } label: {
                        HStack {
                            Text("\(rating)")
                                .foregroundColor(isDisabled ? .secondary : .primary)
                            Spacer()
                            if selectedRating == rating {
                                Image(systemName: "checkmark")
                                    .foregroundColor(.accentColor)
                            }
                        }
                    }
                    .disabled(isDisabled)
                }
            }
            .listStyle(.plain)
            .navigationTitle("Select Minimum Rating")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
        .presentationDetents([.medium])
    }
}

#Preview {
    MinRatingPickerSheet(
        selectedRating: 1500,
        onSelect: { _ in }
    )
}
