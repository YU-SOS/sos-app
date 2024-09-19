package com.example.sos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient

class LoginUserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_user)

        val kakaoLoginButton: ImageButton = findViewById(R.id.kakao_login_button)
        kakaoLoginButton.setOnClickListener {
            kakaoLogin()  // 로그인 시도
        }
    }

    private fun kakaoLogin() {
        // 카카오 로그인 콜백
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                // 로그인 실패 처리
                Log.e("KakaoLogin", "로그인 실패: ${error.message}")
            } else if (token != null) {
                // 로그인 성공 처리
                Log.i("KakaoLogin", "로그인 성공. 토큰: ${token.accessToken}")
                // 로그인 성공 후 처리
                val intent = Intent(this, UserMainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        // 카카오톡 로그인 가능 여부 확인 후 로그인 시도
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            // 카카오톡 로그인 시도
            UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
        } else {
            // 카카오 계정 로그인 시도
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }
}
