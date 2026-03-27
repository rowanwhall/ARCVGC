import SwiftUI
import Shared

let filterChipHeight: CGFloat = AppTokens.filterChipHeight

struct SearchFilterChipsView: View {
    let filters: SearchFiltersData
    let searchParams: SearchParams?
    let onSearchParamsChanged: ((SearchParams) -> Void)?

    var body: some View {
        WrappingHStack(horizontalSpacing: 4, verticalSpacing: 4) {
            if let formatName = filters.formatName {
                Text(formatName)
                    .font(.system(size: 14))
                    .foregroundColor(Color(.label))
                    .padding(.horizontal, 6)
                    .frame(height: filterChipHeight)
                    .background(Color(.systemGray5))
                    .cornerRadius(AppTokens.filterChipCornerRadius)
            }

            ForEach(Array(filters.team1Chips.enumerated()), id: \.offset) { _, chip in
                PokemonFilterChipView(
                    chip: chip,
                    canRemove: searchParams?.canRemovePokemonAt(index: Int32(chip.index)) ?? false,
                    onRemove: {
                        if let params = searchParams, let callback = onSearchParamsChanged {
                            callback(params.removePokemonAt(index: Int32(chip.index)))
                        }
                    }
                )
            }

            if !filters.team2Chips.isEmpty {
                Text("vs")
                    .font(.system(size: 24))
                    .italic()
                    .foregroundColor(Color(.secondaryLabel))
                    .frame(height: filterChipHeight)

                ForEach(Array(filters.team2Chips.enumerated()), id: \.offset) { _, chip in
                    PokemonFilterChipView(
                        chip: chip,
                        canRemove: searchParams?.canRemoveTeam2PokemonAt(index: Int32(chip.index)) ?? false,
                        onRemove: {
                            if let params = searchParams, let callback = onSearchParamsChanged {
                                callback(params.removeTeam2PokemonAt(index: Int32(chip.index)))
                            }
                        }
                    )
                }
            }

            if let rating = filters.minimumRating {
                let canRemove = searchParams?.canRemoveMinRating() ?? false
                HStack(spacing: 0) {
                    Text("\(String(rating))+")
                        .font(.system(size: 14))
                        .foregroundColor(Color(.label))
                    if canRemove, let params = searchParams, let callback = onSearchParamsChanged {
                        FilterChipCloseButton {
                            callback(params.removeMinRating())
                        }
                    }
                }
                .padding(.leading, 6)
                .padding(.trailing, canRemove ? 0 : 6)
                .frame(height: filterChipHeight)
                .background(Color(.systemGray5))
                .cornerRadius(AppTokens.filterChipCornerRadius)
            }

            if let rating = filters.maximumRating {
                let canRemove = searchParams?.canRemoveMaxRating() ?? false
                HStack(spacing: 0) {
                    Text("\(String(rating))-")
                        .font(.system(size: 14))
                        .foregroundColor(Color(.label))
                    if canRemove, let params = searchParams, let callback = onSearchParamsChanged {
                        FilterChipCloseButton {
                            callback(params.removeMaxRating())
                        }
                    }
                }
                .padding(.leading, 6)
                .padding(.trailing, canRemove ? 0 : 6)
                .frame(height: filterChipHeight)
                .background(Color(.systemGray5))
                .cornerRadius(AppTokens.filterChipCornerRadius)
            }

            if filters.unratedOnly {
                let canRemove = searchParams?.canRemoveUnrated() ?? false
                HStack(spacing: 0) {
                    Text("Unrated")
                        .font(.system(size: 14))
                        .foregroundColor(Color(.label))
                    if canRemove, let params = searchParams, let callback = onSearchParamsChanged {
                        FilterChipCloseButton {
                            callback(params.removeUnrated())
                        }
                    }
                }
                .padding(.leading, 6)
                .padding(.trailing, canRemove ? 0 : 6)
                .frame(height: filterChipHeight)
                .background(Color(.systemGray5))
                .cornerRadius(AppTokens.filterChipCornerRadius)
            }

            if let name = filters.playerName {
                let canRemove = searchParams?.canRemovePlayerName() ?? false
                HStack(spacing: 0) {
                    Text(name)
                        .font(.system(size: 14))
                        .foregroundColor(Color(.label))
                    if canRemove, let params = searchParams, let callback = onSearchParamsChanged {
                        FilterChipCloseButton {
                            callback(params.removePlayerName())
                        }
                    }
                }
                .padding(.leading, 6)
                .padding(.trailing, canRemove ? 0 : 6)
                .frame(height: filterChipHeight)
                .background(Color(.systemGray5))
                .cornerRadius(AppTokens.filterChipCornerRadius)
            }

            if filters.timeRangeStart != nil || filters.timeRangeEnd != nil {
                let formatter = DateFormatter()
                let _ = formatter.dateFormat = "MM/dd/yy"
                let startStr = filters.timeRangeStart.map { formatter.string(from: Date(timeIntervalSince1970: Double($0))) } ?? "..."
                let endStr = filters.timeRangeEnd.map { formatter.string(from: Date(timeIntervalSince1970: Double($0))) } ?? "..."
                let canRemove = searchParams?.canRemoveTimeRange() ?? false
                HStack(spacing: 0) {
                    Text("\(startStr) – \(endStr)")
                        .font(.system(size: 14))
                        .foregroundColor(Color(.label))
                    if canRemove, let params = searchParams, let callback = onSearchParamsChanged {
                        FilterChipCloseButton {
                            callback(params.removeTimeRange())
                        }
                    }
                }
                .padding(.leading, 6)
                .padding(.trailing, canRemove ? 0 : 6)
                .frame(height: filterChipHeight)
                .background(Color(.systemGray5))
                .cornerRadius(AppTokens.filterChipCornerRadius)
            }

        }
    }
}

private struct PokemonFilterChipView: View {
    let chip: SearchFilterChipData
    let canRemove: Bool
    let onRemove: () -> Void

    var body: some View {
        let label = {
            var text = chip.name
            if let itemName = chip.itemName { text += " @ \(itemName)" }
            return text
        }()
        HStack(spacing: 2) {
            if let url = chip.teraTypeImageUrl {
                AsyncImage(url: URL(string: url)) { phase in
                    switch phase {
                    case .success(let image):
                        image.resizable().aspectRatio(contentMode: .fit)
                    default:
                        Color.clear
                    }
                }
                .frame(width: 27, height: 27)
            }
            if let url = chip.imageUrl {
                AsyncImage(url: URL(string: url)) { phase in
                    switch phase {
                    case .success(let image):
                        image.resizable().aspectRatio(contentMode: .fit)
                    default:
                        Color.clear
                    }
                }
                .frame(width: 40, height: 40)
            }
            Text(label)
                .font(.system(size: 14))
                .foregroundColor(Color(.label))
            if canRemove {
                FilterChipCloseButton(action: onRemove)
            }
        }
        .padding(.leading, 4)
        .padding(.trailing, canRemove ? 0 : 4)
        .frame(height: filterChipHeight)
        .background(Color(.systemGray5))
        .cornerRadius(AppTokens.filterChipCornerRadius)
    }
}

struct FilterChipCloseButton: View {
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: "xmark")
                .font(.system(size: 10, weight: .semibold))
                .foregroundColor(Color(.secondaryLabel))
                .frame(width: 28, height: 28)
        }
    }
}

struct WrappingHStack: Layout {
    var horizontalSpacing: CGFloat
    var verticalSpacing: CGFloat

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let result = arrangeSubviews(proposal: proposal, subviews: subviews)
        return result.size
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let result = arrangeSubviews(proposal: proposal, subviews: subviews)
        for (index, position) in result.positions.enumerated() {
            subviews[index].place(
                at: CGPoint(x: bounds.minX + position.x, y: bounds.minY + position.y),
                proposal: .unspecified
            )
        }
    }

    private struct ArrangementResult {
        var size: CGSize
        var positions: [CGPoint]
    }

    private func arrangeSubviews(proposal: ProposedViewSize, subviews: Subviews) -> ArrangementResult {
        let maxWidth = proposal.width ?? .infinity
        var positions: [CGPoint] = []
        var x: CGFloat = 0
        var y: CGFloat = 0
        var rowHeight: CGFloat = 0
        var totalHeight: CGFloat = 0
        var totalWidth: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x + size.width > maxWidth && x > 0 {
                y += rowHeight + verticalSpacing
                x = 0
                rowHeight = 0
            }
            positions.append(CGPoint(x: x, y: y))
            rowHeight = max(rowHeight, size.height)
            x += size.width + horizontalSpacing
            totalWidth = max(totalWidth, x - horizontalSpacing)
            totalHeight = y + rowHeight
        }

        return ArrangementResult(
            size: CGSize(width: totalWidth, height: totalHeight),
            positions: positions
        )
    }
}
