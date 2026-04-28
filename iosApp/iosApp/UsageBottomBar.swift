import SwiftUI
import Shared

struct UsageBottomBar: View {
    let formats: [FormatUiModel]
    let selectedFormatId: Int32
    let onFormatSelected: (Int32) -> Void
    let isLoadingFormat: Bool
    @Binding var searchQuery: String
    @FocusState private var isSearchFocused: Bool

    var body: some View {
        VStack(spacing: 8) {
            HStack(spacing: 8) {
                Spacer()
                FormatDropdown(
                    formats: formats,
                    selectedFormatId: selectedFormatId,
                    onFormatSelected: onFormatSelected
                )
                if isLoadingFormat {
                    ProgressView().scaleEffect(0.7)
                }
                Spacer()
            }

            TextField("", text: $searchQuery, prompt: Text("Search Pok\u{00E9}mon").foregroundColor(Color(.secondaryLabel)))
                .focused($isSearchFocused)
                .outlinedTextFieldStyle(isFocused: isSearchFocused)
                .overlay(alignment: .trailing) {
                    if !searchQuery.isEmpty {
                        Button {
                            searchQuery = ""
                        } label: {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundColor(Color(.tertiaryLabel))
                        }
                        .padding(.trailing, 8)
                    }
                }
        }
        .padding(12)
        .background(
            RoundedRectangle(cornerRadius: AppTokens.cardCornerRadius)
                .fill(Color(.systemBackground))
        )
        .overlay(
            RoundedRectangle(cornerRadius: AppTokens.cardCornerRadius)
                .stroke(Color(.opaqueSeparator), lineWidth: AppTokens.standardBorderWidth)
        )
    }
}

#Preview {
    struct UsageBottomBarPreviewHost: View {
        @State private var query: String = ""
        var body: some View {
            UsageBottomBar(
                formats: [
                    FormatUiModel(id: 1, displayName: "[Gen 9] VGC 2026 Reg A", isHistoric: false, isOpenTeamsheet: true, isOfficial: true, hasSeries: true),
                    FormatUiModel(id: 2, displayName: "[Gen 9] VGC 2026 Reg M-A (Bo3)", isHistoric: false, isOpenTeamsheet: true, isOfficial: true, hasSeries: true)
                ],
                selectedFormatId: 2,
                onFormatSelected: { _ in },
                isLoadingFormat: false,
                searchQuery: $query
            )
            .padding()
        }
    }
    return UsageBottomBarPreviewHost()
}
