package com.example.sos.res

import com.example.sos.Location

data class HospitalRes (
    val id: String,
    val name: String,
    val address: String,
    val telephoneNumber: String,
    val imageUrl: String,
    val location: Location,
    val categories: List<CategoryRes>,
    val emergencyRoomStatus: Boolean // 0 수용가능, 1 수용불가
)