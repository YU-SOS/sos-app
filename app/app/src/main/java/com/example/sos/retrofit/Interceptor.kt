package com.example.sos.retrofit

import android.util.Log
import com.example.sos.token.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.Request
import retrofit2.Retrofit

class Interceptor(
    private val tokenManager: TokenManager,
    private val retrofit: Retrofit
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        Log.d("Interceptor", "인터셉터 실행")
        var request = chain.request()
        val accessToken = tokenManager.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            request = addAuthorizationHeader(request, accessToken)
        }

        val response = chain.proceed(request)

        // 401 오류 및 토큰 만료 메시지 확인
        if (response.code == 401) {
            Log.d("Interceptor", "401 오류 확인")
            val errorBody = response.peekBody(Long.MAX_VALUE).string()
            if (errorBody.contains("토큰 만료")) {
                Log.d("Interceptor", "토큰 만료 메세지 확인")
                response.close()
                val newTokens = reissueToken()

                return if (newTokens != null) {
                    tokenManager.saveAccessToken(newTokens.accessToken)
                    tokenManager.saveRefreshToken(newTokens.refreshToken)

                    // 새로운 토큰으로 재시도
                    val newRequest = addAuthorizationHeader(request, newTokens.accessToken)
                    chain.proceed(newRequest)
                } else {
                    // 토큰 재발급 실패 시 기존 응답 반환
                    response
                }
            }
        }

        return response
    }

    private fun addAuthorizationHeader(request: Request, token: String): Request {
        return request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }

    private fun reissueToken(): RefreshResponse? {
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            Log.e("AuthInterceptor", "리프레시 토큰 없음 - 재발급 불가")
            return null
        }

        Log.d("AuthInterceptor", "리프레시 토큰으로 액세스 토큰 재발급 요청")
        val call = retrofit.create(AuthService::class.java).refreshToken(refreshToken)
        val response = call.execute()

        return if (response.isSuccessful) {
            response.body()?.apply {
                Log.d("AuthInterceptor", "토큰 재발급 성공 - 새로운 액세스 토큰 저장")
            }
        } else {
            Log.e("AuthInterceptor", "토큰 재발급 실패 - 응답 코드: ${response.code()}")
            null
        }
    }
}
