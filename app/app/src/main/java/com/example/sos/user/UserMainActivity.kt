package com.example.sos.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.R
import com.example.sos.SelectLoginActivity
import com.example.sos.token.TokenManager

class UserMainActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_main)

        // TokenManager 초기화
        tokenManager = TokenManager(this)
        setupButtons()

        // 비동기적으로 토큰을 받아온 후 유효성 검사 진행
        //checkTokenAfterReceiving()
    }

    /*private fun checkTokenAfterReceiving() {
        // 토큰을 받아오는 네트워크 요청 또는 비동기 작업 후 유효성 검사 수행
        val accessToken = tokenManager.getAccessToken()
        Log.d("UserMainActivity", "Access Token: $accessToken")

        if (accessToken == null || tokenManager.isTokenExpired(accessToken)) {
            Log.d("UserMainActivity", "토큰이 만료됨")
            // 토큰이 없거나 만료되었으면 로그인 화면으로 이동
            val intent = Intent(this, SelectLoginActivity::class.java)
            startActivity(intent)
        } else {
            Log.d("UserMainActivity", "토큰이 유효함")
            // 토큰이 유효하면 화면을 표시
            setupButtons()
        }
    }*/

    private fun setupButtons() {
        val selectUserMapButton = findViewById<ImageButton>(R.id.user_map_button)
        selectUserMapButton.setOnClickListener {
            val intent = Intent(this, UserMapActivity::class.java)
            startActivity(intent)
            finish()
        }

        val selectUserReceptionButton = findViewById<ImageButton>(R.id.user_reception_button)
        selectUserReceptionButton.setOnClickListener {
            val intent = Intent(this, UserReceptionActivity::class.java)
            startActivity(intent)
            finish()
        }

        val userLogoutButton: Button = findViewById(R.id.logout_button)
        userLogoutButton.setOnClickListener {
            tokenManager.clearTokens()
            val intent = Intent(this, SelectLoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}


