package com.example.sos

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//Retrofit 객체를 생성해서, API 인터페이스를 구현 - 인터페이스는 get/post 함수 사용 불가
object RetrofitBuilder {
    private const val BASE_URL = "http://shinuk.example.com" // 서버 주소 (실제 주소로 교체해야 함)

    // Retrofit 인스턴스 생성
    var api: API = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(API::class.java)
}
