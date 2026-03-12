package com.example.showdown26.domain.model

data class Pagination(
    val page: Int,
    val itemsPerPage: Int,
    val totalItems: Int,
    val totalPages: Int
)
