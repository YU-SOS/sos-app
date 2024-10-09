package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.R
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.SelectLoginActivity
import com.example.sos.token.TokenManager
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.LoginResponse
import com.example.sos.retrofit.RefreshRequest
import com.example.sos.retrofit.RefreshResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var logoutButton: Button
    private lateinit var tokenManager: TokenManager // TokenManager 선언

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tokenManager = TokenManager(this)  // TokenManager 초기화
        logoutButton = findViewById(R.id.logout)

        // 로그인 상태 확인 및 자동 로그인
        if (!checkLoginStatus()) {
            Log.d("MainActivity", "User is not logged in. Redirecting to SelectLoginActivity.")
            val intent = Intent(this, SelectLoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val token = tokenManager.getAccessToken()  // 토큰 가져오기
            Log.d("MainActivity", "Stored Token: $token")
            if (token != null) {
                autoLoginWithToken(token)
            } else {
                Log.d("MainActivity", "No access token found.")
            }
        }

        logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun checkLoginStatus(): Boolean {
        val token = tokenManager.getAccessToken()  // 액세스 토큰 가져오기
        Log.d("MainActivity", "Checking login status. Stored Token: $token")
        val isLoggedIn = token != null && !tokenManager.isTokenExpired(token) // 토큰 만료 여부 확인
        Log.d("MainActivity", "User login status: $isLoggedIn")
        return isLoggedIn
    }

    private fun autoLoginWithToken(token: String) {
        if (tokenManager.isTokenExpiringSoon(token, 5 * 24 * 60)) { // 5일 미만 체크 (분 단위)
            Log.d("MainActivity", "Token is about to expire in less than 5 days. Refreshing token...")
            refreshToken()
        } else {
            Log.d("MainActivity", "Token is valid and not expiring soon.")
        }
    }

    private fun refreshToken() {
        val refreshToken = tokenManager.getRefreshToken()  // 리프레시 토큰 가져오기
        Log.d("MainActivity", "Attempting to refresh token. Refresh Token: $refreshToken")
        if (refreshToken != null) {
            val authService = RetrofitClientInstance.retrofitInstance.create(AuthService::class.java)
            val refreshRequest = RefreshRequest(refreshToken)
            authService.refreshToken(refreshRequest).enqueue(object : Callback<RefreshResponse> {
                override fun onResponse(call: Call<RefreshResponse>, response: Response<RefreshResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val newAccessToken = response.body()?.accessToken
                        if (newAccessToken != null) {
                            tokenManager.saveAccessToken(newAccessToken)  // 토큰 저장
                            Log.d("MainActivity", "Token refreshed successfully. New Access Token: $newAccessToken")
                        } else {
                            Log.d("MainActivity", "No new access token received.")
                        }
                    } else {
                        Log.e("MainActivity", "Error refreshing token: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<RefreshResponse>, t: Throwable) {
                    Log.e("MainActivity", "Error refreshing token: ${t.message}")
                }
            })
        } else {
            Log.d("MainActivity", "No refresh token found. Cannot refresh.")
        }
    }

    private fun logout() {
        Log.d("MainActivity", "Logging out user.")
        tokenManager.clearTokens()  // 모든 토큰 삭제
        val intent = Intent(this, SelectLoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
