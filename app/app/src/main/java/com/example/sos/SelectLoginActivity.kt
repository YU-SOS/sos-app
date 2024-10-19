package com.example.sos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.jwt.JWT
import com.example.sos.ambulance.LoginAmbulanceActivity
import com.example.sos.ambulance.MainActivity
import com.example.sos.token.TokenManager
import com.example.sos.user.UserLoginActivity
import com.example.sos.user.UserMainActivity

class SelectLoginActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_select_login)
        tokenManager = TokenManager(this)

        // 토큰이 있다면 역할 확인 후 해당 액티비티로 이동
        val accessToken = tokenManager.getAccessToken()
        if (accessToken != null) {
            Log.d("SelectLoginActivity", "토큰 존재")
            Log.d("SelectLoginActivity", "$accessToken")
            val role = getRoleFromToken(accessToken)
            when (role) {
                "ROLE_USER" -> {
                    Log.d("SelectLoginActivity", "유저메인화면으로 이동")
                    startActivity(Intent(this, UserMainActivity::class.java))
                    finish()
                }
                "ROLE_AMB" -> {
                    Log.d("SelectLoginActivity", "구급대메인화면으로 이동")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                else -> {
                    Log.d("SelectLogin", "역할 알수없음")
                }
            }
        }

        val selectAmbulanceButton = findViewById<ImageButton>(R.id.select_ambulance_button)
        selectAmbulanceButton.setOnClickListener {
            val intent = Intent(this, LoginAmbulanceActivity::class.java)
            startActivity(intent)
            finish()
        }

        val selectUserButton = findViewById<ImageButton>(R.id.select_user_button)
        selectUserButton.setOnClickListener {
            val intent = Intent(this, UserLoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // 토큰에서 역할(Role) 가져오기
    private fun getRoleFromToken(token: String): String? {
        return try {
            val jwt = JWT(token)
            // "role"이라는 이름의 클레임에서 문자열 값 추출
            val role = jwt.getClaim("role").asString()

            if (role != null && role.isNotEmpty()) {
                role // role 클레임이 있으면 반환
            } else {
                null // role이 없으면 null 반환
            }
        } catch (e: Exception) {
            Log.e("SelectLoginActivity", "Error extracting role: ${e.message}")
            null
        }
    }
}
