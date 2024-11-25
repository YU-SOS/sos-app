package com.example.sos

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LogoutManager(private val context: Context, private val tokenManager: TokenManager) {

    fun logout() {
        val accessToken = tokenManager.getAccessToken()
        Log.d("LogoutManager", "Bearer $accessToken")

        if (accessToken != null) {
            val authService = RetrofitClientInstance.getApiService(tokenManager)
            val authorizationHeader = "Bearer $accessToken"

            authService.logout(authorizationHeader).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "로그아웃 성공", Toast.LENGTH_SHORT).show()
                        tokenManager.clearTokens() // 모든 데이터 삭제
                        val intent = Intent(context, IntroActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "로그아웃 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "로그아웃 실패", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(context, "토큰이 없습니다", Toast.LENGTH_SHORT).show()
        }
    }
}
