package com.example.sos

data class Ambulance(
    val role: String,
    val id: String,
    val password: String,
    val name: String,
    val phoneNumber: String? = null,
    val address: String? = null
)
