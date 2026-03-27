import Foundation
import Shared

@MainActor
final class SearchViewModel: ObservableObject {
    @Published private(set) var state = SearchStateReducer.shared.initialState()

    private let logic: SearchLogic
    private let logicScope: Kotlinx_coroutines_coreCoroutineScope
    private var observationTask: Task<Void, Never>?

    // MARK: - Bridging Convenience Properties

    var minRating: Int32? { state.selectedMinRating?.int32Value }
    var maxRating: Int32? { state.selectedMaxRating?.int32Value }
    var timeStart: Date? { state.timeRangeStart.map { Date(timeIntervalSince1970: Double($0.int64Value)) } }
    var timeEnd: Date? { state.timeRangeEnd.map { Date(timeIntervalSince1970: Double($0.int64Value)) } }

    init(appConfigRepository: AppConfigRepository? = nil) {
        let scope = CoroutineScopeFactory.shared.createMainScope()
        self.logicScope = scope
        self.logic = SearchLogic(
            scope: scope,
            appConfigFlow: appConfigRepository?.config
        )

        observationTask = Task { [weak self] in
            guard let logic = self?.logic else { return }
            for await uiState in logic.uiState {
                self?.state = uiState
            }
        }
    }

    deinit {
        observationTask?.cancel()
        CoroutineScopeFactory.shared.cancel(scope: logicScope)
    }

    // MARK: - Filter Slot Management

    func addPokemon(_ pokemon: PokemonPickerUiModel) {
        logic.addPokemon(pokemon: pokemon)
    }

    func removePokemon(at index: Int) {
        logic.removePokemon(index: Int32(index))
    }

    func setItem(at slotIndex: Int, item: ItemUiModel?) {
        logic.setItem(slotIndex: Int32(slotIndex), item: item)
    }

    func setTeraType(at slotIndex: Int, teraType: TeraTypeUiModel?) {
        logic.setTeraType(slotIndex: Int32(slotIndex), teraType: teraType)
    }

    func addTeam2Pokemon(_ pokemon: PokemonPickerUiModel) {
        logic.addTeam2Pokemon(pokemon: pokemon)
    }

    func removeTeam2Pokemon(at index: Int) {
        logic.removeTeam2Pokemon(index: Int32(index))
    }

    func setTeam2Item(at slotIndex: Int, item: ItemUiModel?) {
        logic.setTeam2Item(slotIndex: Int32(slotIndex), item: item)
    }

    func setTeam2TeraType(at slotIndex: Int, teraType: TeraTypeUiModel?) {
        logic.setTeam2TeraType(slotIndex: Int32(slotIndex), teraType: teraType)
    }

    func setAbility(at slotIndex: Int, ability: AbilityUiModel?) {
        logic.setAbility(slotIndex: Int32(slotIndex), ability: ability)
    }

    func setTeam2Ability(at slotIndex: Int, ability: AbilityUiModel?) {
        logic.setTeam2Ability(slotIndex: Int32(slotIndex), ability: ability)
    }

    func setFormat(_ format: FormatUiModel) {
        logic.setFormat(format: format)
    }

    func setMinRating(_ rating: Int32?) {
        logic.setMinRating(rating: rating.map { KotlinInt(int: $0) })
    }

    func setMaxRating(_ rating: Int32?) {
        logic.setMaxRating(rating: rating.map { KotlinInt(int: $0) })
    }

    func setUnratedOnly(_ value: Bool) {
        logic.setUnratedOnly(value: value)
    }

    func setTimeRange(start: Date?, end: Date?) {
        logic.setTimeRange(
            start: start.map { KotlinLong(value: Int64($0.timeIntervalSince1970)) },
            end: end.map { KotlinLong(value: Int64($0.timeIntervalSince1970)) }
        )
    }

    func setPlayerName(_ name: String) {
        logic.setPlayerName(name: name)
    }

    func setOrderBy(_ orderBy: String) {
        logic.setOrderBy(orderBy: orderBy)
    }
}
