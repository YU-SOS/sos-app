package com.example.sos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.jwt.JWT
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var logoutButton: Button // 로그아웃 버튼 선언

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 로그아웃 버튼을 XML에서 찾아서 변수에 할당
        logoutButton = findViewById(R.id.logout)

        // 로그인 상태를 체크하고, 로그인이 안 되어 있으면 SelectLoginActivity로 이동
        if (!checkLoginStatus()) {
            val intent = Intent(this, SelectLoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // 로그인 상태가 유효하면 토큰 유효성 검사 및 리프레시 진행
            val token = getToken()
            if (token != null) {
                checkTokenValidityAndRefresh(token)
            }
        }

        // 로그아웃 버튼 클릭 리스너 설정
        logoutButton.setOnClickListener {
            logout() // 로그아웃 함수 호출
        }
    }

    // 토큰이 만료되었는지 확인하는 함수
    private fun isTokenExpired(token: String): Boolean {
        val jwt = JWT(token)
        return jwt.isExpired(0) // 토큰이 만료되었는지 체크
    }

    // 로그인 상태를 확인하는 함수
    private fun checkLoginStatus(): Boolean {
        val token = getToken()

        // 디버깅을 위한 로그
        Log.d("MainActivity", "Stored Token: $token")

        return token != null && !isTokenExpired(token) // 토큰이 유효한 경우
    }

    // JWT 토큰을 SharedPreferences에서 가져오는 함수
    private fun getToken(): String? {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwt_token", null)
    }

    // 만료 시간이 5일 미만인지 확인하고 리프레시 토큰으로 재발급 요청
    private fun checkTokenValidityAndRefresh(token: String) {
        val jwt = JWT(token)
        val expirationTime = jwt.expiresAt?.time ?: 0
        val currentTime = System.currentTimeMillis()
        val fiveDaysInMillis = 5 * 24 * 60 * 60 * 1000 // 5일을 밀리초로 변환 (토큰이 밀리초 연산)

        // 만료 기간이 5일 이하 남았는지 확인
        if (expirationTime - currentTime <= fiveDaysInMillis) {
            Log.d("MainActivity", "Token is about to expire. Refreshing token...")
            refreshToken()
        }
    }

    // 리프레시 토큰을 통해 새로운 액세스 토큰을 요청하는 함수
    private fun refreshToken() {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val refreshToken = sharedPreferences.getString("refresh_token", null)

        // 리프레시 토큰이 null이 아닌 경우에만 요청
        if (refreshToken != null) {
            // Retrofit을 통해 AuthService 생성
            val authService = RetrofitClientInstance.retrofitInstance.create(AuthService::class.java)
            val refreshRequest = RefreshRequest(refreshToken) // 리프레시 요청 데이터 생성

            // 리프레시 API 호출
            authService.refreshToken(refreshRequest).enqueue(object : Callback<RefreshResponse> {
                override fun onResponse(call: Call<RefreshResponse>, response: Response<RefreshResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val newAccessToken = response.body()?.accessToken // 새 액세스 토큰 가져오기
                        if (newAccessToken != null) {
                            // 새 액세스 토큰 저장
                            saveToken(newAccessToken)
                            Log.d("MainActivity", "Token refreshed successfully.")
                        } else {
                            Log.d("MainActivity", "Failed to retrieve new access token.")
                        }
                    } else {
                        Log.d("MainActivity", "Failed to refresh token.")
                    }
                }

                override fun onFailure(call: Call<RefreshResponse>, t: Throwable) {
                    Log.e("MainActivity", "Error refreshing token: ${t.message}")
                }
            })
        } else {
            Log.d("MainActivity", "No refresh token found.")
        }
    }

    // JWT 토큰을 SharedPreferences에 저장하는 함수
    private fun saveToken(token: String?) {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("jwt_token", token).apply()
    }

    // 로그아웃을 처리하는 함수
    private fun logout() {
        // SharedPreferences에서 JWT 토큰과 리프레시 토큰 삭제
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .remove("jwt_token") // 액세스 토큰 삭제
            .remove("refresh_token") // 리프레시 토큰 삭제
            .apply()

        // SelectLoginActivity로 이동
        val intent = Intent(this, SelectLoginActivity::class.java)
        startActivity(intent)
        finish() // 현재 액티비티 종료
    }
}