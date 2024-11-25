package com.example.sos

import com.kakao.sdk.user.model.Gender

data class PatientReq (
    val name: String,
    val age: Int,
    val phoneNumber: String,
    val symptom: String,
    val medication: String,
    val reference: String,
    val gender: Gender,
    val severity: String
)