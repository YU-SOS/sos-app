package com.example.sos.retrofit

import com.example.sos.token.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClientInstance {
    private const val BASE_URL = "http://43.203.205.27:8080/"
    private var retrofit: Retrofit? = null

    // HttpClient 생성
    private fun createHttpClient(tokenManager: TokenManager, authService: AuthService): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .addInterceptor(Interceptor(tokenManager, authService))
            .build()
    }

    // API 서비스 생성
    fun getApiService(tokenManager: TokenManager): AuthService {
        if (retrofit == null) {
            val basicRetrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val authService = basicRetrofit.create(AuthService::class.java)
            val clientWithInterceptor = createHttpClient(tokenManager, authService)

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(clientWithInterceptor)
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