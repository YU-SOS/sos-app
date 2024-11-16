package com.example.sos

data class Page<T>(
    val content: List<T>, // 데이터 리스트
    val size: Int,
    val number: Int,
    val totalElements: Int,
    val totalPages: Int
)