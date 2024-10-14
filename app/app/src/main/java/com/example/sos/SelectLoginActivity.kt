package com.example.sos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
            val role = getRoleFromToken(accessToken)
            when (role) {
                "USER" -> {
                    Log.d("SelectLoginActivity", "유저메인화면으로 이동")
                    startActivity(Intent(this, UserMainActivity::class.java))
                    finish()
                }
                "AMB" -> {
                    Log.d("SelectLoginActivity", "구급대메인화면으로 이동")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                else -> {
                    // Role이 없거나 알 수 없는 경우, select_login 화면 유지
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val selectAmbulanceButton = findViewById<ImageButton>(R.id.select_ambulance_button)
        selectAmbulanceButton.setOnClickListener {
            val intent = Intent(this, LoginAmbulanceActivity::class.java)
            startActivity(intent)

        }

        val selectUserButton = findViewById<ImageButton>(R.id.select_user_button)
        selectUserButton.setOnClickListener {
            val intent = Intent(this, UserLoginActivity::class.java)
            startActivity(intent)
        }
    }

    // 토큰에서 역할(Role) 가져오기
    private fun getRoleFromToken(token: String): String? {
        return try {
            val jwt = JWT(token)
            jwt.getClaim("role").asString()
        } catch (e: Exception) {
            Log.e("SelectLoginActivity", "에러: ${e.message}")
            null

        }
    }
}
