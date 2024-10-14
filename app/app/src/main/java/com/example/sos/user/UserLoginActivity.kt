package com.example.sos.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.R
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import com.example.sos.retrofit.UserLoginResponse
import com.example.sos.retrofit.UserSignupRequest
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserLoginActivity : AppCompatActivity() {
    // TokenManager 선언 (초기화는 나중에)
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_user)

        tokenManager = TokenManager(this)

        val kakaoLoginButton: ImageButton = findViewById(R.id.kakao_login_button)
        kakaoLoginButton.setOnClickListener {
            kakaoLogin()
        }
    }

    private fun kakaoLogin() {
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e("KakaoLogin", "로그인 실패: ${error.message}")
            } else if (token != null) {
                // 카카오 API에서 사용자 정보를 가져옴
                UserApiClient.instance.me { user, meError ->
                    if (meError != null) {
                        Log.e("KakaoLogin", "사용자 정보 요청 실패: ${meError.message}")
                    } else if (user != null) {
                        val name = user.kakaoAccount?.profile?.nickname ?: "이름 없음"
                        val providerId = user.id.toString()
                        val email = user.kakaoAccount?.email ?: "이메일 없음"
                        val provider = "kakao"
                        Log.d("success login?", "name: $name")
                        Log.d("success login?", "providerId: $providerId")
                        Log.d("success login?", "email: $email")
                        Log.d("success login?", "provider: $provider")

                        // 가져온 사용자 정보를 서버로 전송
                        sendUserDataToServer(name, providerId, provider, email)
                    }
                }
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    private fun sendUserDataToServer(name: String, providerId: String, provider: String, email: String) {
        val userSignupRequest = UserSignupRequest(name, providerId, provider, email)


        val authService = RetrofitClientInstance.getApiService(tokenManager)

        authService.loginUser(userSignupRequest).enqueue(object : Callback<UserLoginResponse> {

            override fun onResponse(call: Call<UserLoginResponse>, response: Response<UserLoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    Log.d("LoginUser", "로그인 성공: ${loginResponse?.message}")

                    val accessToken = response.headers()["Authorization"]
                    val refreshToken = response.headers()["Set-Cookie"]
                    val statusCode = response.code()

                    Log.d("LoginUser", "액세스 토큰: $accessToken")
                    Log.d("LoginUser", "리프레시 토큰: $refreshToken")
                    Log.d("LoginUser", "스테이터스 코드: $statusCode")

                    if (refreshToken != null && accessToken != null) {
                        tokenManager.saveRefreshToken(refreshToken)
                        tokenManager.saveAccessToken(accessToken)

                        // 토큰이 성공적으로 저장된 후에만 UserMainActivity로 이동
                        val intent = Intent(this@UserLoginActivity, UserMainActivity::class.java)
                        startActivity(intent)
                        finish()  // 현재 로그인 액티비티 종료
                    } else {
                        Log.e("LoginUser", "리프레시 또는 액세스 토큰을 찾을 수 없습니다.")
                    }

                } else {
                    Log.e("LoginUser", "로그인 실패: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<UserLoginResponse>, t: Throwable) {
                Log.e("LoginUser", "로그인 중 오류 발생", t)
            }
        })
    }
}
