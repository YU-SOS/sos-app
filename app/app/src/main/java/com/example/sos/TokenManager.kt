package com.example.sos

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class TokenManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)

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

    // 리프레시 토큰 삭제
    fun clearRefreshToken() {
        sharedPreferences.edit().apply {
            remove("refresh_token")
            apply()  // 비동기적으로 삭제
        }
        Log.d("TokenManager", "리프레시 토큰 삭제 완료")
    }
}