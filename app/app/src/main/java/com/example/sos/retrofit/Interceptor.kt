package com.example.sos.retrofit

import com.example.sos.token.TokenManager

class Interceptor(private val tokenManager: TokenManager, private val authService: AuthService) : okhttp3.Interceptor {

    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val accessToken = tokenManager.getAccessToken()

        // Access Token이 없는 경우(로그인 요청)에는 헤더 추가 안 함
        val requestBuilder = chain.request().newBuilder()
        if (!accessToken.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $accessToken")
        }

        val response = chain.proceed(requestBuilder.build())

        if (response.code == 401 && response.peekBody(Long.MAX_VALUE).string().contains("JWT expired")) {
            response.close()
            val refreshToken = tokenManager.getRefreshToken()
            val tokenResponse = authService.refreshToken("$refreshToken").execute()

            if (tokenResponse.isSuccessful) {
                tokenResponse.body()?.let {
                    tokenManager.saveAccessToken(it.accessToken)
                    tokenManager.saveRefreshToken(it.refreshToken)

                    val newRequest = chain.request().newBuilder()
                        .header("Authorization", "Bearer ${it.accessToken}")
                        .build()

                    return chain.proceed(newRequest)
                }
            }
        }

        return response
    }
}
