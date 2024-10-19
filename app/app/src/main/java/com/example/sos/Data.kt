package com.example.sos

data class Data(
    val hospitals: List<HospitalRes>, // 병원 리스트
    val page: Page // 페이지 정보
)