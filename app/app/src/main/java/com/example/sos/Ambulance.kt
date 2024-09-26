package com.example.sos

data class Ambulance(
    val id: String,
    val password: String,
    val name: String,
    val telephoneNumber: String,
    val address: String,
    val location: Location,
    val imageURL: String
)
