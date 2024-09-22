package com.example.sos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginUserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_user)

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
                        // 사용자 정보가 성공적으로 받아졌다면
                        val name = user.kakaoAccount?.profile?.nickname ?: "이름 없음" //그럴리는 없겠지만 이름이 null값으로 들어온다면 이름 없음으로 처리
                        val providerId = user.id.toString()  // 카카오 유저 고유번호
                        val email = user.kakaoAccount?.email ?: "이메일 없음"

                        // 받은 데이터를 바탕으로 sendToken() 호출
                        sendToken(name, providerId, email, token.accessToken)

                        val intent = Intent(this, UserMainActivity::class.java)
                        startActivity(intent)
                        finish()
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


    private fun sendToken(name: String, providerId: String, email: String, accessToken: String) {
        // PostData 객체 생성
        val tokenRequest = PostData(
            name = name,
            providerId = providerId,
            provider = "kakao", // 로그인 제공자(고정 값)
            email = email,
            accessToken = accessToken
        )

        // Retrofit을 통해 백엔드 서버로 POST 요청 전송
        RetrofitBuilder.api.sendToken(tokenRequest)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("SendData", "데이터 전송 성공")
                    } else {
                        Log.e("SendData", "데이터 전송 실패: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("SendData", "데이터 전송 중 오류 발생", t)
                }
            })
    }
}
