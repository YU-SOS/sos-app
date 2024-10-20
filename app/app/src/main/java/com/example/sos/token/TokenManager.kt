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
        // 토큰 파싱 후 추가 정보 저장
        parseAndStoreTokenData(token)
    }

    // JWT 토큰 파싱 후 필요한 정보 저장
    private fun parseAndStoreTokenData(token: String) {
        try {
            // JWT 라이브러리를 이용해 페이로드 정보를 가져오기
            val jwt = JWT(token)
            val id = jwt.getClaim("sub").asString()  // 'sub'에서 사용자 ID 추출
            val role = jwt.getClaim("role").asString()  // 'role'에서 역할 추출
            val tokenType = jwt.getClaim("tokenType").asString()  // 'tokenType'에서 토큰 타입 추출 (필요 없으면 추후 제거 예정)

            // SharedPreferences에 추가 정보 저장
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

    // 액세스 토큰 및 리프레시 토큰 삭제
    fun clearTokens() {
        sharedPreferences.edit().apply {
            remove("jwt_token")  // 액세스 토큰 삭제
            remove("refresh_token")  // 리프레시 토큰 삭제
            remove("userId")  // 사용자 ID 삭제
            remove("userRole")  // 역할 삭제
            remove("tokenType")  // 토큰 타입 삭제
            remove("reception_id")  // 접수 고유 번호 삭제
            apply()
        }
        Log.d("TokenManager", "토큰 삭제 완료")
    }

    // 구급대 입장 환자 접수 고유 번호 저장
    fun saveReceptionId(receptionId: String) {
        sharedPreferences.edit().apply {
            putString("reception_id", receptionId)
            apply()
        }
        Log.d("TokenManager", "접수 고유 번호 저장 완료")
    }

    // 접수 고유 번호 가져오기
    fun getReceptionId(): String? {
        return sharedPreferences.getString("reception_id", null)
    }
}
