package com.example.sos

import com.google.gson.annotations.SerializedName

data class PostData(
    @SerializedName("name") val name: String,                // 사용자 이름
    @SerializedName("providerId") val providerId: String,    // 카카오 앱 내 유저 고유번호
    @SerializedName("provider") val provider: String = "kakao",  // 로그인 제공자 (고정 값: "kakao")
    @SerializedName("email") val email: String,              // 카카오에서 받은 이메일
    @SerializedName("accessToken") val accessToken: String   // Access Token
)
