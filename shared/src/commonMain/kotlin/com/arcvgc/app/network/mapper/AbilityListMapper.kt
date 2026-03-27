package com.arcvgc.app.network.mapper

import com.arcvgc.app.domain.model.Ability
import com.arcvgc.app.network.model.AbilityListItemDto

fun AbilityListItemDto.toDomain(): Ability {
    return Ability(id = id, name = name)
}
