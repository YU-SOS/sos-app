package com.example.sos.token

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
            apply()  // 동기적으로 저장

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
}
