import SwiftUI
import Shared

struct TopPlayerDialogView: View {
    let player: ContentListItem.TopPlayerChipItem
    let onDismiss: () -> Void
    let onViewPlayer: (Int32, String) -> Void
    let onBattleClick: (Int32) -> Void

    private var battles: [ContentListItem.TopPlayerBattleItem] {
        player.battles
    }

    private var ratingSummary: String? {
        var parts: [String] = []
        if player.rankedWinCount > 0 {
            let label = player.rankedWinCount == 1 ? "win" : "wins"
            parts.append("\(player.rankedWinCount) \(label)")
        }
        if let avg = player.avgRating {
            parts.append("avg \(avg.int32Value)")
        }
        if let max = player.maxRating, max.int32Value != player.minRating?.int32Value {
            parts.append("max \(max.int32Value)")
        }
        return parts.isEmpty ? nil : parts.joined(separator: " • ")
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(player.name)
                            .font(.title3)
                            .fontWeight(.semibold)
                            .foregroundColor(Color(.label))
                        if let summary = ratingSummary {
                            Text(summary)
                                .font(.caption)
                                .foregroundColor(Color(.secondaryLabel))
                        }
                    }
                    Spacer()
                    Button {
                        onDismiss()
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .font(.title2)
                            .foregroundColor(Color(.tertiaryLabel))
                    }
                    .buttonStyle(.plain)
                }

                Button {
                    onViewPlayer(player.id, player.name)
                } label: {
                    HStack(spacing: 12) {
                        Text("View player profile")
                            .font(.body)
                            .fontWeight(.medium)
                            .foregroundColor(Color(.label))
                        Spacer()
                        Image(systemName: "chevron.right")
                            .font(.subheadline)
                            .foregroundColor(Color(.secondaryLabel))
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 12)
                    .background(Color(.secondarySystemBackground))
                    .cornerRadius(AppTokens.cardCornerRadius)
                    .overlay(
                        RoundedRectangle(cornerRadius: AppTokens.cardCornerRadius)
                            .stroke(Color(.opaqueSeparator), lineWidth: AppTokens.standardBorderWidth)
                    )
                }
                .buttonStyle(.plain)

                if !battles.isEmpty {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Top battles")
                            .font(.subheadline)
                            .fontWeight(.semibold)
                            .foregroundColor(Color(.secondaryLabel))
                        VStack(spacing: 8) {
                            ForEach(Array(battles.enumerated()), id: \.offset) { _, battle in
                                Button {
                                    onBattleClick(battle.id)
                                } label: {
                                    HStack {
                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(battle.rating.map { "Rating \($0.int32Value)" } ?? "Unrated")
                                                .font(.body)
                                                .fontWeight(.medium)
                                                .foregroundColor(Color(.label))
                                            Text(battle.uploadTimeDisplay)
                                                .font(.caption)
                                                .foregroundColor(Color(.secondaryLabel))
                                        }
                                        Spacer()
                                        Image(systemName: "chevron.right")
                                            .font(.subheadline)
                                            .foregroundColor(Color(.secondaryLabel))
                                    }
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 10)
                                    .background(Color(.secondarySystemBackground))
                                    .cornerRadius(AppTokens.cardCornerRadius)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: AppTokens.cardCornerRadius)
                                            .stroke(Color(.opaqueSeparator), lineWidth: AppTokens.standardBorderWidth)
                                    )
                                }
                                .buttonStyle(.plain)
                            }
                        }
                    }
                }
            }
            .padding(20)
        }
        .background(Color(.systemGray6))
    }
}

#Preview {
    TopPlayerDialogView(
        player: ContentListItem.TopPlayerChipItem(
            id: 1,
            name: "DragonClaw99",
            battles: [
                ContentListItem.TopPlayerBattleItem(
                    id: 100,
                    rating: KotlinInt(int: 1850),
                    uploadTimeDisplay: "May 9, 5:30 PM"
                ),
                ContentListItem.TopPlayerBattleItem(
                    id: 101,
                    rating: nil,
                    uploadTimeDisplay: "May 7, 2:15 PM"
                )
            ],
            rankedWinCount: 5,
            avgRating: KotlinInt(int: 1700),
            maxRating: KotlinInt(int: 1850),
            minRating: KotlinInt(int: 1500)
        ),
        onDismiss: {},
        onViewPlayer: { _, _ in },
        onBattleClick: { _ in }
    )
}
