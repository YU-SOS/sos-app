package com.example.sos

data class AmbulanceRes (
    val id: String,
    val name: String,
    val address: String,
    val telephoneNumber: String,
    val location: Location
)