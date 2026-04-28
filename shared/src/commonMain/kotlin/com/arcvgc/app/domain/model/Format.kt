package com.arcvgc.app.domain.model

data class Format(
    val id: Int,
    val name: String,
    val formattedName: String?,
    val isHistoric: Boolean = false,
    val isOpenTeamsheet: Boolean = false,
    val isOfficial: Boolean = false,
    val hasSeries: Boolean = false
)
