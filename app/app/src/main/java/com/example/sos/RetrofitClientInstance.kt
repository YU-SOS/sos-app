package com.example.sos

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClientInstance {

    private val BASE_URL = "http://3.35.136.82:8080/" // 서버 URL 넣기 http://10.0.2.2:8080

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY) // 요청 및 응답 본문 로그 출력
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // 로깅 인터셉터 추가
        .build()

    val retrofitInstance: Retrofit
        get() {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient) // HTTP 클라이언트 설정
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

    val api: AuthService by lazy {
        retrofitInstance.create(AuthService::class.java)
    }
}
