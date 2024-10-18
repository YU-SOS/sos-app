package com.example.sos

data class HospitalRes (
    val id: Int,
    val name: String ,
    val address: String,
    val telephoneNumber: String,
    val imageUrl: String,
    val location: Location,
    val categories: List<CategoryRes>,
    val emergencyRoomStatus: String?
)