package com.example.sos

data class Page<T>(
    val content: List<T>,
    val page: PageInfo
)

data class PageInfo(
    val size: Int,
    val number: Int,
    val totalElements: Int,
    val totalPages: Int
)