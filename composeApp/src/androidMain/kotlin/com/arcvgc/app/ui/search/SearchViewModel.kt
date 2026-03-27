package com.arcvgc.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcvgc.app.data.CatalogState
import com.arcvgc.app.data.repository.AbilityCatalogRepository
import com.arcvgc.app.data.repository.AppConfigRepository
import com.arcvgc.app.data.repository.FormatCatalogRepository
import com.arcvgc.app.data.repository.ItemCatalogRepository
import com.arcvgc.app.data.repository.PokemonCatalogRepository
import com.arcvgc.app.data.repository.TeraTypeCatalogRepository
import com.arcvgc.app.domain.model.AppConfig
import com.arcvgc.app.ui.model.AbilityUiModel
import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.ui.model.PokemonPickerUiModel
import com.arcvgc.app.ui.model.SearchUiState
import com.arcvgc.app.ui.model.TeraTypeUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    pokemonCatalogRepository: PokemonCatalogRepository,
    itemCatalogRepository: ItemCatalogRepository,
    teraTypeCatalogRepository: TeraTypeCatalogRepository,
    formatCatalogRepository: FormatCatalogRepository,
    abilityCatalogRepository: AbilityCatalogRepository,
    appConfigRepository: AppConfigRepository
) : ViewModel() {

    private val logic = SearchLogic(
        scope = viewModelScope,
        appConfigFlow = appConfigRepository.config
    )

    val uiState: StateFlow<SearchUiState> = logic.uiState

    val pokemonCatalogState: StateFlow<CatalogState<PokemonPickerUiModel>> =
        pokemonCatalogRepository.state

    val itemCatalogState: StateFlow<CatalogState<ItemUiModel>> =
        itemCatalogRepository.state

    val teraTypeCatalogState: StateFlow<CatalogState<TeraTypeUiModel>> =
        teraTypeCatalogRepository.state

    val formatCatalogState: StateFlow<CatalogState<FormatUiModel>> =
        formatCatalogRepository.state

    val abilityCatalogState: StateFlow<CatalogState<AbilityUiModel>> =
        abilityCatalogRepository.state

    val appConfigState: StateFlow<AppConfig?> = appConfigRepository.config

    fun addPokemon(pokemon: PokemonPickerUiModel) = logic.addPokemon(pokemon)
    fun removePokemon(index: Int) = logic.removePokemon(index)
    fun setItem(slotIndex: Int, item: ItemUiModel?) = logic.setItem(slotIndex, item)
    fun setTeraType(slotIndex: Int, teraType: TeraTypeUiModel?) = logic.setTeraType(slotIndex, teraType)
    fun addTeam2Pokemon(pokemon: PokemonPickerUiModel) = logic.addTeam2Pokemon(pokemon)
    fun removeTeam2Pokemon(index: Int) = logic.removeTeam2Pokemon(index)
    fun setTeam2Item(slotIndex: Int, item: ItemUiModel?) = logic.setTeam2Item(slotIndex, item)
    fun setAbility(slotIndex: Int, ability: AbilityUiModel?) = logic.setAbility(slotIndex, ability)
    fun setTeam2Ability(slotIndex: Int, ability: AbilityUiModel?) = logic.setTeam2Ability(slotIndex, ability)
    fun setTeam2TeraType(slotIndex: Int, teraType: TeraTypeUiModel?) = logic.setTeam2TeraType(slotIndex, teraType)
    fun setFormat(format: FormatUiModel) = logic.setFormat(format)
    fun setMinRating(rating: Int?) = logic.setMinRating(rating)
    fun setMaxRating(rating: Int?) = logic.setMaxRating(rating)
    fun setUnratedOnly(value: Boolean) = logic.setUnratedOnly(value)
    fun setTimeRange(start: Long?, end: Long?) = logic.setTimeRange(start, end)
    fun setPlayerName(name: String) = logic.setPlayerName(name)
    fun setOrderBy(orderBy: String) = logic.setOrderBy(orderBy)
}
