package com.example.sos

data class PatientReq (
    val name: String,
    val age: Int,
    val phoneNumber: String,
    val medication: String,
    val reference: String,
    val gender: Boolean
)