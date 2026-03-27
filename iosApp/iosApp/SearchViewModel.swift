import Foundation
import Shared

@MainActor
final class SearchViewModel: ObservableObject {
    @Published private(set) var state = SearchStateReducer.shared.initialState()

    // MARK: - Bridging Convenience Properties

    var minRating: Int32? { state.selectedMinRating?.int32Value }
    var maxRating: Int32? { state.selectedMaxRating?.int32Value }
    var timeStart: Date? { state.timeRangeStart.map { Date(timeIntervalSince1970: Double($0.int64Value)) } }
    var timeEnd: Date? { state.timeRangeEnd.map { Date(timeIntervalSince1970: Double($0.int64Value)) } }

    // MARK: - Filter Slot Management

    func addPokemon(_ pokemon: PokemonPickerUiModel) {
        state = SearchStateReducer.shared.addPokemon(state: state, pokemon: pokemon)
    }

    func removePokemon(at index: Int) {
        state = SearchStateReducer.shared.removePokemon(state: state, index: Int32(index))
    }

    func setItem(at slotIndex: Int, item: ItemUiModel) {
        state = SearchStateReducer.shared.setItem(state: state, slotIndex: Int32(slotIndex), item: item)
    }

    func setTeraType(at slotIndex: Int, teraType: TeraTypeUiModel) {
        state = SearchStateReducer.shared.setTeraType(state: state, slotIndex: Int32(slotIndex), teraType: teraType)
    }

    func addTeam2Pokemon(_ pokemon: PokemonPickerUiModel) {
        state = SearchStateReducer.shared.addTeam2Pokemon(state: state, pokemon: pokemon)
    }

    func removeTeam2Pokemon(at index: Int) {
        state = SearchStateReducer.shared.removeTeam2Pokemon(state: state, index: Int32(index))
    }

    func setTeam2Item(at slotIndex: Int, item: ItemUiModel) {
        state = SearchStateReducer.shared.setTeam2Item(state: state, slotIndex: Int32(slotIndex), item: item)
    }

    func setTeam2TeraType(at slotIndex: Int, teraType: TeraTypeUiModel) {
        state = SearchStateReducer.shared.setTeam2TeraType(state: state, slotIndex: Int32(slotIndex), teraType: teraType)
    }

    func setFormat(_ format: FormatUiModel) {
        state = SearchStateReducer.shared.setFormat(state: state, format: format)
    }

    func setDefaultFormat(_ format: FormatUiModel) {
        state = SearchStateReducer.shared.setDefaultFormat(state: state, format: format)
    }

    func setMinRating(_ rating: Int32?) {
        state = SearchStateReducer.shared.setMinRating(state: state, rating: rating.map { KotlinInt(int: $0) })
    }

    func setMaxRating(_ rating: Int32?) {
        state = SearchStateReducer.shared.setMaxRating(state: state, rating: rating.map { KotlinInt(int: $0) })
    }

    func setUnratedOnly(_ value: Bool) {
        state = SearchStateReducer.shared.setUnratedOnly(state: state, value: value)
    }

    func setTimeRange(start: Date?, end: Date?) {
        state = SearchStateReducer.shared.setTimeRange(
            state: state,
            start: start.map { KotlinLong(value: Int64($0.timeIntervalSince1970)) },
            end: end.map { KotlinLong(value: Int64($0.timeIntervalSince1970)) }
        )
    }

    func setPlayerName(_ name: String) {
        state = SearchStateReducer.shared.setPlayerName(state: state, name: name)
    }

    func setOrderBy(_ orderBy: String) {
        state = SearchStateReducer.shared.setOrderBy(state: state, orderBy: orderBy)
    }
}
