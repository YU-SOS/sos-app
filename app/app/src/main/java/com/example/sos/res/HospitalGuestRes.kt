package com.example.sos.res

import com.example.sos.Location

data class HospitalGuestRes(
    val name: String,
    val address: String,
    val imageUrl: String,
    val location: Location
)
