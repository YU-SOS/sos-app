package com.example.sos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.util.Log
import android.widget.ImageButton
import com.auth0.android.jwt.JWT
import com.example.sos.ambulance.AmbulanceMainActivity
import com.example.sos.token.TokenManager
import com.example.sos.user.UserMapActivity
import com.kakao.sdk.common.util.Utility

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        val keyHash = Utility.getKeyHash(this)
        Log.d("mykey", keyHash)

        // 로그인 버튼 클릭 이벤트
        findViewById<ImageButton>(R.id.main_login_button).setOnClickListener {
            val intent = Intent(this, LoginMainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Role에 맞게 화면 전환
        val tokenManager = TokenManager(this)
        val accessToken = tokenManager.getAccessToken()
        if (accessToken != null) {
            Log.d("SelectLoginActivity", "토큰 존재")
            Log.d("SelectLoginActivity", "$accessToken")
            val role = getRoleFromToken(accessToken)
            when (role) {
                "ROLE_USER" -> {
                    Log.d("SelectLoginActivity", "유저메인화면으로 이동")
                    startActivity(Intent(this, UserMapActivity::class.java))
                    finish()
                }
                "ROLE_AMB" -> {
                    Log.d("SelectLoginActivity", "구급대메인화면으로 이동")
                    startActivity(Intent(this, AmbulanceMainActivity::class.java))
                    finish()
                }
                else -> {
                    Log.d("SelectLogin", "역할 알수없음")
                }
            }
        }
    }

    // 토큰에서 역할(Role) 가져오기
    private fun getRoleFromToken(token: String): String? {
        return try {
            val jwt = JWT(token)
            val role = jwt.getClaim("role").asString()
            if (role != null && role.isNotEmpty()) {
                role
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("SelectLoginActivity", "Error extracting role: ${e.message}")
            null
        }
    }

}
