import Foundation
import Shared

@MainActor
final class SearchViewModel: ObservableObject {
    @Published private(set) var state = SearchState()

    // MARK: - Filter Slot Management

    func addPokemon(_ pokemon: PokemonPickerUiModel) {
        guard state.canAddMore else { return }
        let slot = SearchFilterSlotUiModel(
            pokemonId: pokemon.id,
            pokemonName: pokemon.name,
            pokemonImageUrl: pokemon.imageUrl,
            item: nil,
            teraType: nil
        )
        state.filterSlots.append(slot)
    }

    func removePokemon(at index: Int) {
        state.filterSlots.remove(at: index)
    }

    func setItem(at slotIndex: Int, item: ItemUiModel) {
        let slot = state.filterSlots[slotIndex]
        state.filterSlots[slotIndex] = SearchFilterSlotUiModel(
            pokemonId: slot.pokemonId,
            pokemonName: slot.pokemonName,
            pokemonImageUrl: slot.pokemonImageUrl,
            item: item,
            teraType: slot.teraType
        )
    }

    func setTeraType(at slotIndex: Int, teraType: TeraTypeUiModel) {
        let slot = state.filterSlots[slotIndex]
        state.filterSlots[slotIndex] = SearchFilterSlotUiModel(
            pokemonId: slot.pokemonId,
            pokemonName: slot.pokemonName,
            pokemonImageUrl: slot.pokemonImageUrl,
            item: slot.item,
            teraType: teraType
        )
    }

    func setFormat(_ format: FormatUiModel) {
        state.selectedFormat = format
    }

    func setMinRating(_ rating: Int32?) {
        state.selectedMinRating = rating
    }

    func setMaxRating(_ rating: Int32?) {
        state.selectedMaxRating = rating
    }

    func setUnratedOnly(_ value: Bool) {
        state.unratedOnly = value
        if value {
            state.selectedMinRating = nil
            state.selectedMaxRating = nil
            if state.selectedOrderBy == "rating" {
                state.selectedOrderBy = "time"
            }
        }
    }

    func setTimeRange(start: Date?, end: Date?) {
        state.timeRangeStart = start
        state.timeRangeEnd = end
    }

    func setPlayerName(_ name: String) {
        state.playerName = name
    }

    func setOrderBy(_ orderBy: String) {
        state.selectedOrderBy = orderBy
    }
}
