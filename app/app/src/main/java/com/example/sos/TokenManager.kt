package com.example.sos

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.auth0.android.jwt.JWT

class TokenManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    // 액세스 토큰 저장
    fun saveAccessToken(token: String) {
        sharedPreferences.edit().apply {
            putString("jwt_token", token)
            apply()  // 비동기적으로 저장
        }
        Log.d("TokenManager", "액세스 토큰 저장 완료")
    }

    // 액세스 토큰 불러오기
    fun getAccessToken(): String? {
        return sharedPreferences.getString("jwt_token", null)
    }

    // 리프레시 토큰 저장
    fun saveRefreshToken(refreshToken: String) {
        sharedPreferences.edit().apply {
            putString("refresh_token", refreshToken)
            apply()  // 비동기적으로 저장
        }
        Log.d("TokenManager", "리프레시 토큰 저장 완료")
    }

    // 리프레시 토큰 불러오기
    fun getRefreshToken(): String? {
        return sharedPreferences.getString("refresh_token", null)
    }

    // 액세스 토큰 및 리프레시 토큰 삭제
    fun clearTokens() {
        sharedPreferences.edit().apply {
            remove("jwt_token")  // 액세스 토큰 삭제
            remove("refresh_token")  // 리프레시 토큰 삭제
            apply()
        }
        Log.d("TokenManager", "토큰 삭제 완료")
    }

    // 토큰 만료 여부 확인
    fun isTokenExpired(token: String): Boolean {
        val jwt = JWT(token)
        return jwt.isExpired(0) // 현재 시간을 기준으로 토큰 만료 여부 체크
    }

    // 토큰 유효성 검사
    fun isTokenExpiringSoon(token: String, days: Int): Boolean {
        val jwt = JWT(token)
        val expirationTime = jwt.expiresAt?.time ?: 0
        val currentTime = System.currentTimeMillis()
        val daysInMillis = days * 24 * 60 * 60 * 1000 // n일을 밀리초로 변환
        return expirationTime - currentTime <= daysInMillis // 만료가 가까운지 확인
    }
}
