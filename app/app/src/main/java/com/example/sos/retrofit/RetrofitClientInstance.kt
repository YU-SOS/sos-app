package com.example.sos.retrofit

import com.example.sos.token.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClientInstance {
    private const val BASE_URL = "http://3.35.136.82:8080/"
    private var retrofit: Retrofit? = null

    // Interceptor가 포함된 HttpClient 설정
    private fun getHttpClient(tokenManager: TokenManager): OkHttpClient {
        val authService = getBasicRetrofitInstance().create(AuthService::class.java)
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .addInterceptor(Interceptor(tokenManager, authService))
            .build()
    }

    // 기본 Retrofit 인스턴스 설정
    private fun getBasicRetrofitInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 모든 API 호출에서 Interceptor가 적용된 AuthService 반환
    fun getApiService(tokenManager: TokenManager): AuthService {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getHttpClient(tokenManager))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(AuthService::class.java)
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