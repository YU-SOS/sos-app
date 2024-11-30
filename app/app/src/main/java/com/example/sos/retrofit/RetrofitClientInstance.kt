package com.example.sos.retrofit

import com.example.sos.token.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClientInstance {
    private const val BASE_URL = "http://43.203.205.27:8080"
    private var retrofit: Retrofit? = null

    private fun createHttpClient(tokenManager: TokenManager, authService: AuthService): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .addInterceptor(Interceptor(tokenManager, authService))
            .build()
    }

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
