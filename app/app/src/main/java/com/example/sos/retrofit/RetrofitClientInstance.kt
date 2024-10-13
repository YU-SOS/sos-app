package com.example.sos.retrofit

import android.content.Context
import com.example.sos.token.TokenManager

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClientInstance {


    private const val BASE_URL = "http://3.35.136.82:8080/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun getHttpClient(tokenManager: TokenManager, context: Context): OkHttpClient {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(Interceptor(tokenManager, retrofit)) // AuthInterceptor 추가
            .build()
    }

    fun getRetrofitInstance(tokenManager: TokenManager, context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getHttpClient(tokenManager, context)) // OkHttpClient 설정
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getApiService(tokenManager: TokenManager, context: Context): AuthService {
        return getRetrofitInstance(tokenManager, context).create(AuthService::class.java)
    }
}

// 카카오 주소 검색 API 설정
object KakaoRetrofitClientInstance {

    private const val KAKAO_BASE_URL = "https://dapi.kakao.com/"

    val retrofitInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(KAKAO_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val kakaoService: KakaoMapService by lazy {
        retrofitInstance.create(KakaoMapService::class.java)
    }
}

