package com.arcvgc.app.ui.mapper

import com.arcvgc.app.domain.model.Ability
import com.arcvgc.app.ui.model.AbilityUiModel

object AbilityUiMapper {

    fun map(ability: Ability): AbilityUiModel {
        return AbilityUiModel(
            id = ability.id,
            name = ability.displayName
        )
    }

    fun mapList(abilities: List<Ability>): List<AbilityUiModel> {
        return abilities.filter { it.name.isNotBlank() }.map { map(it) }
    }
}
