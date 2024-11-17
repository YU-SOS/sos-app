package com.example.sos.res

import com.example.sos.Location

data class AmbulanceRes (
    val id: String,
    val name: String,
    val address: String,
    val telephoneNumber: String,
    val location: Location
)