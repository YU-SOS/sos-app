package com.example.sos.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object KakaoRetrofitClientInstance {
    private const val KAKAO_BASE_URL = "https://dapi.kakao.com/"
    public const val API_KEY = "24a76ea9dc5ffd6677de0900eedb7f72"

    val kakaoService: KakaoMapService by lazy {
        Retrofit.Builder()
            .baseUrl(KAKAO_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KakaoMapService::class.java)
    }
}
