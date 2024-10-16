package com.example.sos

data class Hospital(
    val id: String,
    val name: String,
    val address: String,
    val imageUrl: String,
    val telephoneNumber: String,
    val location: Location,
    val categories: List<String>
)
