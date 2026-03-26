package com.arcvgc.app.domain.model

data class Pagination(
    val page: Int,
    val itemsPerPage: Int,
    val hasNext: Boolean
)
