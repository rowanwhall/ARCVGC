import SwiftUI
import Shared

struct PokemonNavTarget: Hashable {
    let id: Int32
    let name: String
    let imageUrl: String?
    let typeImageUrl1: String?
    let typeImageUrl2: String?
    let formatId: Int32?

    init(id: Int32, name: String, imageUrl: String?, typeImageUrl1: String?, typeImageUrl2: String?, formatId: Int32? = nil) {
        self.id = id
        self.name = name
        self.imageUrl = imageUrl
        self.typeImageUrl1 = typeImageUrl1
        self.typeImageUrl2 = typeImageUrl2
        self.formatId = formatId
    }
}

struct PlayerNavTarget: Hashable {
    let id: Int32
    let name: String
    let formatId: Int32?

    init(id: Int32, name: String, formatId: Int32? = nil) {
        self.id = id
        self.name = name
        self.formatId = formatId
    }
}

let paginationThreshold = 5

struct SearchFilterChipData {
    let index: Int
    let name: String
    let imageUrl: String?
    let itemName: String?
    let teraTypeImageUrl: String?
    let abilityName: String?
    let itemImageUrl: String?
}

struct SearchFiltersData {
    let team1Chips: [SearchFilterChipData]
    let team2Chips: [SearchFilterChipData]
    let formatName: String?
    let minimumRating: Int32?
    let maximumRating: Int32?
    let unratedOnly: Bool
    let playerName: String?
    let timeRangeStart: Int64?
    let timeRangeEnd: Int64?
}

enum ContentListHeader {
    case none
    case homeHero
    case topPokemonHero
    case favoritesHero
    case pokemonHero(name: String, imageUrl: String?, typeImageUrls: [String])
    case playerHero(name: String)
    case searchFilters(SearchFiltersData)

    init(mode: ContentListMode) {
        switch mode {
        case .home:
            self = .homeHero
        case .favorites:
            self = .favoritesHero
        case .pokemon(_, let name, let imageUrl, let typeImageUrl1, let typeImageUrl2, _):
            let typeUrls = [typeImageUrl1, typeImageUrl2].compactMap { $0 }
            self = .pokemonHero(name: name, imageUrl: imageUrl, typeImageUrls: typeUrls)
        case .player(_, let name, _):
            self = .playerHero(name: name)
        case .topPokemon:
            self = .topPokemonHero
        case .search(let params):
            let team1Chips = params.filters.enumerated().map { index, slot in
                SearchFilterChipData(
                    index: index,
                    name: slot.pokemonName,
                    imageUrl: slot.pokemonImageUrl,
                    itemName: slot.itemName,
                    teraTypeImageUrl: slot.teraTypeImageUrl,
                    abilityName: slot.abilityName,
                    itemImageUrl: slot.itemImageUrl
                )
            }
            let team2Chips = params.team2Filters.enumerated().map { index, slot in
                SearchFilterChipData(
                    index: index,
                    name: slot.pokemonName,
                    imageUrl: slot.pokemonImageUrl,
                    itemName: slot.itemName,
                    teraTypeImageUrl: slot.teraTypeImageUrl,
                    abilityName: slot.abilityName,
                    itemImageUrl: slot.itemImageUrl
                )
            }
            self = .searchFilters(SearchFiltersData(
                team1Chips: team1Chips,
                team2Chips: team2Chips,
                formatName: params.formatName,
                minimumRating: params.minimumRating.map { $0.int32Value > 0 ? $0.int32Value : nil } ?? nil,
                maximumRating: params.maximumRating.map { $0.int32Value > 0 ? $0.int32Value : nil } ?? nil,
                unratedOnly: params.unratedOnly,
                playerName: params.playerName,
                timeRangeStart: params.timeRangeStart?.int64Value,
                timeRangeEnd: params.timeRangeEnd?.int64Value
            ))
        }
    }

    var hasPokemonHero: Bool {
        if case .pokemonHero = self { return true }
        return false
    }

    var hasSearchFilters: Bool {
        if case .searchFilters = self { return true }
        return false
    }
}

struct SectionHeaderView: View {
    let title: String
    var isLoading: Bool = false
    var sortOrder: String? = nil
    var onToggleSortOrder: (() -> Void)? = nil
    var onSeeMore: (() -> Void)? = nil

    var body: some View {
        HStack {
            Text(title)
                .font(.subheadline)
                .fontWeight(.semibold)
                .foregroundColor(Color(.label))
            Spacer()
            if let sortOrder = sortOrder, let toggle = onToggleSortOrder {
                SortToggleButton(sortOrder: sortOrder, isLoading: isLoading, action: toggle)
            } else if let seeMore = onSeeMore {
                Button(action: seeMore) {
                    HStack(spacing: 2) {
                        Text("See More")
                            .font(.caption)
                        Image(systemName: "chevron.right")
                            .font(.system(size: 10, weight: .semibold))
                    }
                    .foregroundColor(Color(.label).opacity(0.75))
                    .padding(.horizontal, 8)
                    .frame(height: 28)
                    .contentShape(Rectangle())
                }
            }
        }
    }
}

struct SortToggleButton: View {
    let sortOrder: String
    var isLoading: Bool = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 4) {
                if isLoading {
                    ProgressView()
                        .scaleEffect(0.6)
                        .frame(width: 14, height: 14)
                } else {
                    Image(systemName: "arrow.up.arrow.down")
                        .font(.system(size: 10, weight: .semibold))
                }
                Text(sortOrder == "rating" ? "Rating" : "Time")
                    .font(.system(size: 12))
            }
            .foregroundColor(Color(.label).opacity(0.75))
            .padding(.horizontal, 8)
            .frame(height: 28)
            .cornerRadius(4)
            .overlay(
                RoundedRectangle(cornerRadius: 4)
                    .stroke(Color(.opaqueSeparator), lineWidth: 1)
            )
        }
        .disabled(isLoading)
    }
}

struct FormatDropdown: View {
    let formats: [FormatUiModel]
    let selectedFormatId: Int32
    let onFormatSelected: (Int32) -> Void

    var body: some View {
        let selectedFormat = formats.first { $0.id == selectedFormatId }
        Menu {
            ForEach(Array(formats.enumerated()), id: \.element.id) { _, format in
                Button {
                    onFormatSelected(format.id)
                } label: {
                    if format.id == selectedFormatId {
                        Label(format.displayName, systemImage: "checkmark")
                    } else {
                        Text(format.displayName)
                    }
                }
            }
        } label: {
            HStack(spacing: 4) {
                Text(selectedFormat?.displayName ?? "Format")
                    .font(.system(size: 14))
                    .foregroundColor(Color(.label).opacity(0.75))
                Image(systemName: "chevron.up.chevron.down")
                    .font(.system(size: 10))
                    .foregroundColor(Color(.label).opacity(0.75))
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .cornerRadius(8)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(Color(.opaqueSeparator), lineWidth: 1)
            )
        }
    }
}

func findBattle(in items: [ContentListItem], id: Int32) -> BattleCardUiModel? {
    for item in items {
        if let battleItem = item as? ContentListItem.Battle, battleItem.uiModel.id == id {
            return battleItem.uiModel
        }
        if let section = item as? ContentListItem.Section,
           let found = findBattle(in: section.items as! [ContentListItem], id: id) {
            return found
        }
    }
    return nil
}
