package com.example.sos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.jwt.JWT

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
        }

        // 로그아웃 버튼 클릭 리스너 설정
        logoutButton.setOnClickListener {
            logout() // 로그아웃 함수 호출
        }
    }

    // 토큰이 만료되었는지 확인하는 함수
    private fun isTokenExpired(token: String): Boolean {
        val jwt = JWT(token)
        val expired = jwt.isExpired(0) // 토큰이 만료되었는지 체크

        // 만료 여부를 로그로 출력
        Log.d("MainActivity", "Token Expired: $expired")
        return expired
    }

    // 로그인 상태를 확인하는 함수
    private fun checkLoginStatus(): Boolean {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("jwt_token", null)

        // 디버깅을 위한 로그
        Log.d("MainActivity", "토큰 Stored Token: $token")

        if (token != null) {
            if (isTokenExpired(token)) {
                Log.d("MainActivity", "토큰 Token is expired.")
                return false // 토큰이 만료된 경우
            }
            Log.d("MainActivity", "토큰 Token is valid.")
            return true // 토큰이 유효한 경우
        }

        Log.d("MainActivity", "토큰 No token found.")
        return false // 토큰이 null인 경우
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
