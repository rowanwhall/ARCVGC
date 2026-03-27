import SwiftUI
import Shared

enum SearchSheet: Identifiable {
    case pokemon(team: Int)
    case item(team: Int, slotIndex: Int)
    case teraType(team: Int, slotIndex: Int)
    case ability(team: Int, slotIndex: Int)
    case format
    case playerName
    case minRating
    case maxRating
    case sortOrder
    case startDate
    case endDate

    var id: String {
        switch self {
        case .pokemon(let t): return "pokemon_t\(t)"
        case .item(let t, let i): return "item_t\(t)_\(i)"
        case .teraType(let t, let i): return "tera_t\(t)_\(i)"
        case .ability(let t, let i): return "ability_t\(t)_\(i)"
        case .format: return "format"
        case .playerName: return "playerName"
        case .minRating: return "minRating"
        case .maxRating: return "maxRating"
        case .sortOrder: return "sortOrder"
        case .startDate: return "startDate"
        case .endDate: return "endDate"
        }
    }
}

struct SearchView: View {
    @StateObject private var viewModel: SearchViewModel
    @ObservedObject var catalogStore: CatalogStore
    @ObservedObject var appConfigStore: AppConfigStore
    @EnvironmentObject var container: DependencyContainer
    @State private var activeSheet: SearchSheet? = nil
    @State private var searchParams: SearchParams?

    init(catalogStore: CatalogStore, appConfigStore: AppConfigStore, initialSearchParams: SearchParams? = nil) {
        self._viewModel = StateObject(wrappedValue: SearchViewModel(appConfigRepository: appConfigStore.repo))
        self._catalogStore = ObservedObject(wrappedValue: catalogStore)
        self._appConfigStore = ObservedObject(wrappedValue: appConfigStore)
        _searchParams = State(initialValue: initialSearchParams)
    }

    private var sortedFormatItems: [FormatUiModel] {
        let defaultId = appConfigStore.config?.defaultFormat.id
        return FormatSorter.shared.sorted(
            formats: catalogStore.formatItems,
            defaultFormatId: defaultId.map { KotlinInt(int: $0) }
        )
    }

    private var searchEnabled: Bool {
        !viewModel.state.filterSlots.isEmpty
            || viewModel.state.hasTeam2
            || viewModel.minRating != nil
            || viewModel.maxRating != nil
            || viewModel.state.unratedOnly
            || !viewModel.state.playerName.isEmpty
            || (viewModel.timeStart != nil && viewModel.timeEnd != nil)
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                LazyVStack(spacing: 12) {
                    // Format button (first — sets context for all other filters)
                    if catalogStore.formatLoading {
                        SearchOptionButton(text: "") {}
                            .overlay { ProgressView() }
                            .disabled(true)
                    } else if catalogStore.formatError == nil {
                        let displayName = viewModel.state.selectedFormat?.displayName
                            ?? sortedFormatItems.first?.displayName
                        SearchOptionButton(text: "Format: \(displayName ?? "Select")") {
                            activeSheet = .format
                        }
                    }

                    let isTeam2Mode = viewModel.state.hasTeam2
                    let team1Slots = viewModel.state.filterSlots
                    let team2Slots = viewModel.state.team2FilterSlots

                    if isTeam2Mode {
                        // Two-team mode: side-by-side compact cards
                        let maxRows = max(team1Slots.count, team2Slots.count)
                        ForEach(Array(0..<maxRows), id: \.self) { rowIndex in
                            HStack(spacing: 8) {
                                if rowIndex < team1Slots.count {
                                    SearchFilterCard(
                                        slot: team1Slots[rowIndex],
                                        onRemove: { viewModel.removePokemon(at: rowIndex) },
                                        onItemTap: { activeSheet = .item(team: 1, slotIndex: rowIndex) },
                                        onTeraTap: { activeSheet = .teraType(team: 1, slotIndex: rowIndex) },
                                        onAbilityTap: { activeSheet = .ability(team: 1, slotIndex: rowIndex) },
                                        compact: true
                                    )
                                } else {
                                    Spacer().frame(maxWidth: .infinity)
                                }
                                if rowIndex < team2Slots.count {
                                    SearchFilterCard(
                                        slot: team2Slots[rowIndex],
                                        onRemove: { viewModel.removeTeam2Pokemon(at: rowIndex) },
                                        onItemTap: { activeSheet = .item(team: 2, slotIndex: rowIndex) },
                                        onTeraTap: { activeSheet = .teraType(team: 2, slotIndex: rowIndex) },
                                        onAbilityTap: { activeSheet = .ability(team: 2, slotIndex: rowIndex) },
                                        compact: true
                                    )
                                } else {
                                    Spacer().frame(maxWidth: .infinity)
                                }
                            }
                            .id("team_row_\(rowIndex)_\(team1Slots.count)_\(team2Slots.count)")
                        }
                    } else {
                        // Single-team mode: full-width cards
                        ForEach(Array(team1Slots.enumerated()), id: \.offset) { index, slot in
                            SearchFilterCard(
                                slot: slot,
                                onRemove: { viewModel.removePokemon(at: index) },
                                onItemTap: { activeSheet = .item(team: 1, slotIndex: index) },
                                onTeraTap: { activeSheet = .teraType(team: 1, slotIndex: index) },
                                onAbilityTap: { activeSheet = .ability(team: 1, slotIndex: index) }
                            )
                            .id("single_\(index)_\(team1Slots.count)")
                        }
                    }

                    // Add buttons — unified for both modes
                    HStack(spacing: 8) {
                        if viewModel.state.canAddMoreTeam1 {
                            let addText = isTeam2Mode ? "Add Pokémon"
                                : (team1Slots.isEmpty ? "Add Pokémon Filter" : "Add Pokémon")
                            SearchOptionButton(text: addText) { activeSheet = .pokemon(team: 1) }
                        } else if isTeam2Mode {
                            Spacer().frame(maxWidth: .infinity)
                        }
                        if isTeam2Mode || !team1Slots.isEmpty {
                            if viewModel.state.canAddMoreTeam2 {
                                let text = isTeam2Mode ? "Add Opposing" : "Add Opposing Pokémon"
                                SearchOptionButton(text: text) { activeSheet = .pokemon(team: 2) }
                            } else if isTeam2Mode {
                                Spacer().frame(maxWidth: .infinity)
                            }
                        }
                    }

                    // Player Name button
                    DateOptionButton(
                        text: viewModel.state.playerName.isEmpty
                            ? "Showdown Username"
                            : "Showdown Username: \(viewModel.state.playerName)",
                        onClear: !viewModel.state.playerName.isEmpty ? {
                            viewModel.setPlayerName("")
                        } : nil
                    ) {
                        activeSheet = .playerName
                    }

                    // Min/Max Rating buttons
                    HStack(spacing: 8) {
                        SearchOptionButton(
                            text: viewModel.minRating.map { "Min Rating: \(String($0))" }
                                ?? "Min Rating: None"
                        ) {
                            activeSheet = .minRating
                        }
                        SearchOptionButton(
                            text: viewModel.maxRating.map { "Max Rating: \(String($0))" }
                                ?? "Max Rating: None"
                        ) {
                            activeSheet = .maxRating
                        }
                    }
                    .disabled(viewModel.state.unratedOnly)
                    .opacity(viewModel.state.unratedOnly ? AppTokens.secondaryIconAlpha : 1.0)

                    // Unrated Only toggle button
                    Button {
                        viewModel.setUnratedOnly(!viewModel.state.unratedOnly)
                    } label: {
                        Text("Unrated Only")
                            .font(.system(size: 14))
                            .foregroundColor(viewModel.state.unratedOnly ? .white : Color(.label))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                    }
                    .background(viewModel.state.unratedOnly ? Color.accentColor : Color(.systemGray5))
                    .cornerRadius(AppTokens.searchButtonCornerRadius)

                    // Date range buttons
                    HStack(spacing: 8) {
                        DateOptionButton(
                            text: viewModel.timeStart.map {
                                "Start: \(formatDate($0))"
                            } ?? "Start Date",
                            onClear: viewModel.timeStart != nil ? {
                                viewModel.setTimeRange(start: nil, end: viewModel.timeEnd)
                            } : nil
                        ) {
                            activeSheet = .startDate
                        }
                        DateOptionButton(
                            text: viewModel.timeEnd.map {
                                "End: \(formatDate($0))"
                            } ?? "End Date",
                            onClear: viewModel.timeEnd != nil ? {
                                viewModel.setTimeRange(start: viewModel.timeStart, end: nil)
                            } : nil
                        ) {
                            activeSheet = .endDate
                        }
                    }

                    // Sort Order button
                    SearchOptionButton(
                        text: "Sort by: \(viewModel.state.selectedOrderBy == "time" ? "Time" : "Rating")"
                    ) {
                        activeSheet = .sortOrder
                    }
                    .disabled(viewModel.state.unratedOnly)
                    .opacity(viewModel.state.unratedOnly ? AppTokens.secondaryIconAlpha : 1.0)

                    // Search button
                    Button {
                        func mapSlots(_ slots: [SearchFilterSlotUiModel]) -> [SearchFilterSlot] {
                            slots.map { slot in
                                SearchFilterSlot(
                                    pokemonId: slot.pokemonId,
                                    itemId: slot.item.map { KotlinInt(int: $0.id) },
                                    teraTypeId: slot.teraType.map { KotlinInt(int: $0.id) },
                                    abilityId: slot.ability.map { KotlinInt(int: $0.id) },
                                    pokemonName: slot.pokemonName,
                                    pokemonImageUrl: slot.pokemonImageUrl,
                                    itemName: slot.item?.name,
                                    itemImageUrl: slot.item?.imageUrl,
                                    teraTypeImageUrl: slot.teraType?.imageUrl,
                                    abilityName: slot.ability?.name
                                )
                            }
                        }
                        let filters = mapSlots(viewModel.state.filterSlots)
                        let team2Filters = mapSlots(viewModel.state.team2FilterSlots)
                        let resolvedFormatId = viewModel.state.selectedFormat?.id
                            ?? sortedFormatItems.first?.id
                            ?? 1
                        let resolvedFormatName = viewModel.state.selectedFormat?.displayName
                            ?? sortedFormatItems.first?.displayName
                        let resolvedOrderBy = viewModel.state.selectedOrderBy
                        searchParams = SearchParams(
                            filters: filters,
                            team2Filters: team2Filters,
                            formatId: resolvedFormatId,
                            minimumRating: viewModel.state.unratedOnly ? nil : viewModel.minRating.map { KotlinInt(int: $0) },
                            maximumRating: viewModel.state.unratedOnly ? nil : viewModel.maxRating.map { KotlinInt(int: $0) },
                            unratedOnly: viewModel.state.unratedOnly,
                            orderBy: resolvedOrderBy,
                            timeRangeStart: viewModel.timeStart.map { KotlinLong(value: Int64($0.timeIntervalSince1970)) },
                            timeRangeEnd: viewModel.timeEnd.map { KotlinLong(value: Int64($0.timeIntervalSince1970)) },
                            playerName: viewModel.state.playerName.isEmpty ? nil : viewModel.state.playerName,
                            formatName: resolvedFormatName
                        )
                    } label: {
                        Text("Search")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.borderedProminent)
                    .controlSize(.large)
                    .disabled(!searchEnabled)
                }
                .padding(.horizontal, 16)
                .padding(.top, 32)
                .padding(.bottom, 16)
            }
            .background(Color(.systemBackground))
            .navigationDestination(isPresented: Binding(
                get: { searchParams != nil },
                set: { if !$0 { searchParams = nil } }
            )) {
                if let params = searchParams {
                    ContentListView(
                        repository: container.battleRepository,
                        mode: .search(params: params),
                        favoritesStore: container.favoritesStore,
                        settingsStore: container.settingsStore,
                        pokemonCatalogItems: catalogStore.pokemonItems,
                        onSearchParamsChanged: { newParams in
                            searchParams = newParams
                        }
                    )
                }
            }
            .sheet(item: $activeSheet) { sheet in
                switch sheet {
                case .pokemon(let team):
                    let excludeIds = team == 1
                        ? Set(viewModel.state.filterSlots.map { $0.pokemonId })
                        : Set(viewModel.state.team2FilterSlots.map { $0.pokemonId })
                    PokemonPickerSheet(
                        items: catalogStore.pokemonItems,
                        isLoading: catalogStore.pokemonLoading,
                        error: catalogStore.pokemonError,
                        excludeIds: excludeIds
                    ) { pokemon in
                        if team == 1 { viewModel.addPokemon(pokemon) }
                        else { viewModel.addTeam2Pokemon(pokemon) }
                    }

                case .item(let team, let slotIndex):
                    ItemPickerSheet(
                        items: catalogStore.itemItems,
                        isLoading: catalogStore.itemLoading,
                        error: catalogStore.itemError
                    ) { item in
                        if team == 1 { viewModel.setItem(at: slotIndex, item: item) }
                        else { viewModel.setTeam2Item(at: slotIndex, item: item) }
                    }

                case .teraType(let team, let slotIndex):
                    TeraTypePickerSheet(
                        items: catalogStore.teraTypeItems,
                        isLoading: catalogStore.teraTypeLoading,
                        error: catalogStore.teraTypeError
                    ) { teraType in
                        if team == 1 { viewModel.setTeraType(at: slotIndex, teraType: teraType) }
                        else { viewModel.setTeam2TeraType(at: slotIndex, teraType: teraType) }
                    }

                case .ability(let team, let slotIndex):
                    AbilityPickerSheet(
                        items: catalogStore.abilityItems,
                        isLoading: catalogStore.abilityLoading,
                        error: catalogStore.abilityError
                    ) { ability in
                        if team == 1 { viewModel.setAbility(at: slotIndex, ability: ability) }
                        else { viewModel.setTeam2Ability(at: slotIndex, ability: ability) }
                    }

                case .format:
                    FormatPickerSheet(
                        items: sortedFormatItems,
                        isLoading: catalogStore.formatLoading,
                        error: catalogStore.formatError
                    ) { format in
                        viewModel.setFormat(format)
                    }

                case .playerName:
                    PlayerNamePickerSheet(
                        currentName: viewModel.state.playerName
                    ) { name in
                        viewModel.setPlayerName(name)
                    }

                case .minRating:
                    MinRatingPickerSheet(
                        selectedRating: viewModel.minRating,
                        disabledAbove: viewModel.maxRating
                    ) { rating in
                        viewModel.setMinRating(rating)
                    }

                case .maxRating:
                    MaxRatingPickerSheet(
                        selectedRating: viewModel.maxRating,
                        disabledBelow: viewModel.minRating
                    ) { rating in
                        viewModel.setMaxRating(rating)
                    }

                case .sortOrder:
                    SortOrderPickerSheet(
                        selectedOrderBy: viewModel.state.selectedOrderBy
                    ) { orderBy in
                        viewModel.setOrderBy(orderBy)
                    }

                case .startDate:
                    DatePickerSheet(
                        title: "Select Start Date",
                        selectedDate: viewModel.timeStart ?? Date(),
                        minDate: nil,
                        maxDate: viewModel.timeEnd ?? Date()
                    ) { date in
                        viewModel.setTimeRange(start: date, end: viewModel.timeEnd)
                    }

                case .endDate:
                    DatePickerSheet(
                        title: "Select End Date",
                        selectedDate: viewModel.timeEnd ?? Date(),
                        minDate: viewModel.timeStart,
                        maxDate: Date()
                    ) { date in
                        viewModel.setTimeRange(start: viewModel.timeStart, end: date)
                    }
                }
            }
        }
    }

    private func formatDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "MM/dd/yy"
        return formatter.string(from: date)
    }
}

private struct SearchOptionButton: View {
    let text: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(text)
                .font(.system(size: 14))
                .foregroundColor(Color(.label))
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
                .background(Color(.systemGray5))
                .cornerRadius(AppTokens.searchButtonCornerRadius)
        }
    }
}

private struct DateOptionButton: View {
    let text: String
    var onClear: (() -> Void)?
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 4) {
                Text(text)
                    .font(.system(size: 14))
                    .foregroundColor(Color(.label))
                    .frame(maxWidth: .infinity)
                if onClear != nil {
                    Button {
                        onClear?()
                    } label: {
                        Image(systemName: "xmark")
                            .font(.system(size: 10, weight: .semibold))
                            .foregroundColor(Color(.secondaryLabel))
                            .frame(width: 24, height: 24)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.vertical, 11)
            .padding(.horizontal, 8)
            .background(Color(.systemGray5))
            .cornerRadius(AppTokens.searchButtonCornerRadius)
        }
    }
}

struct DatePickerSheet: View {
    let title: String
    let onSelect: (Date) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var selectedDate: Date
    private let minDate: Date?
    private let maxDate: Date

    init(title: String, selectedDate: Date, minDate: Date?, maxDate: Date, onSelect: @escaping (Date) -> Void) {
        self.title = title
        self.onSelect = onSelect
        self.minDate = minDate
        self.maxDate = maxDate
        _selectedDate = State(initialValue: selectedDate)
    }

    var body: some View {
        NavigationStack {
            VStack {
                DatePicker(
                    title,
                    selection: $selectedDate,
                    in: (minDate ?? .distantPast)...maxDate,
                    displayedComponents: .date
                )
                .datePickerStyle(.graphical)
                .padding()
                Spacer()
            }
            .navigationTitle(title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") {
                        onSelect(selectedDate)
                        dismiss()
                    }
                }
            }
        }
    }
}

#Preview {
    let container = DependencyContainer()
    return SearchView(catalogStore: container.catalogStore, appConfigStore: container.appConfigStore)
        .environmentObject(container)
}
