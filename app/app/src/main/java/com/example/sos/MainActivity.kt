package com.example.sos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.jwt.JWT

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!checkLoginStatus()) {
            val intent = Intent(this, SelectLoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // 토큰 유효성 검사하는 함수
    private fun isTokenExpired(token: String): Boolean {
        val jwt = JWT(token)
        return jwt.isExpired(0)
    }
    // 로그인이 되어있는지 체크하는 함수
    private fun checkLoginStatus(): Boolean {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("jwt_token", null)

        return token != null && !isTokenExpired(token)
    }
}
