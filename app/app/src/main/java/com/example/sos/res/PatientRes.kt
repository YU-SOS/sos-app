package com.example.sos.res

import com.kakao.sdk.user.model.Gender

data class PatientRes(
    val name: String,
    val age: Int,
    val phoneNumber: String,
    val medication: String,
    val reference: String,
    val gender: Gender
)