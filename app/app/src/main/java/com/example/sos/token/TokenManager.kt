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
            apply()
        }
        Log.d("TokenManager", "액세스 토큰 저장 완료")
        parseAndStoreTokenData(token)
    }

    // JWT 토큰 파싱 후 필요한 정보 저장
    private fun parseAndStoreTokenData(token: String) {
        try {
            val jwt = JWT(token)
            val id = jwt.getClaim("sub").asString() // 사용자 ID
            val role = jwt.getClaim("role").asString() // 역할
            val tokenType = jwt.getClaim("tokenType").asString() // 토큰 타입

            sharedPreferences.edit().apply {
                putString("userId", id)
                putString("userRole", role)
                putString("tokenType", tokenType)
                apply()
            }
            Log.d("TokenManager", "JWT 정보 저장 완료")
        } catch (e: Exception) {
            Log.e("TokenManager", "JWT 파싱 오류: ${e.message}")
        }
    }

    // 액세스 토큰 불러오기
    fun getAccessToken(): String? {
        return sharedPreferences.getString("jwt_token", null)
    }

    // 리프레시 토큰 저장
    fun saveRefreshToken(refreshToken: String) {
        sharedPreferences.edit().apply {
            putString("refresh_token", refreshToken)
            apply()
        }
        Log.d("TokenManager", "리프레시 토큰 저장 완료")
    }

    // 리프레시 토큰 불러오기
    fun getRefreshToken(): String? {
        return sharedPreferences.getString("refresh_token", null)
    }

    // 사용자 ID 가져오기
    fun getTokenId(): String {
        val accessToken = getAccessToken() ?: throw IllegalStateException("Access Token이 없습니다.")
        val jwt = JWT(accessToken)
        return jwt.getClaim("sub").asString() ?: throw IllegalStateException("User ID를 찾을 수 없습니다.")
    }

    // 사용자 역할 가져오기
    fun getTokenRole(): String? {
        return sharedPreferences.getString("userRole", null)
    }

    // 토큰 타입 가져오기
    fun getTokenType(): String? {
        return sharedPreferences.getString("tokenType", null)
    }

    // 토큰 및 저장된 데이터 삭제
    fun clearTokens() {
        sharedPreferences.edit().apply {
            remove("jwt_token") // 액세스 토큰 삭제
            remove("refresh_token") // 리프레시 토큰 삭제
            remove("userId") // 사용자 ID 삭제
            remove("userRole") // 역할 삭제
            remove("tokenType") // 토큰 타입 삭제
            remove("reception_id") // 접수 고유 번호 삭제
            remove("selected_paramedic_id") // 선택된 구급대원 ID 삭제
            apply()
        }
        Log.d("TokenManager", "모든 토큰 및 저장된 데이터 삭제 완료")
    }

    // 구급대원 ID 저장
    fun saveSelectedParamedicId(paramedicId: String) {
        sharedPreferences.edit().putString("selected_paramedic_id", paramedicId).apply()
        Log.d("TokenManager", "선택된 구급대원의 ID 저장 완료")
    }

    // 저장된 구급대원의 ID 불러오기
    fun getSelectedParamedicId(): String? {
        return sharedPreferences.getString("selected_paramedic_id", null)
    }
}
