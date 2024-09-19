package com.example.sos

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

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
                Log.e("KakaoLogin", "로그인 실패", error)
            } else if (token != null) {
                Log.i("KakaoLogin", "로그인 성공: ${token.accessToken}")

                // 로그인 성공 시 사용자 정보 요청
                requestUserInfo(token.accessToken)
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 그렇지 않으면 카카오 계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    private fun requestUserInfo(accessToken: String) {
        // 사용자 정보 가져오기
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("KakaoLogin", "사용자 정보 요청 실패", error)
                val intent = Intent(this, UserMainActivity::class.java)
                startActivity(intent)
            } else if (user != null) {
                //어떤 정보를 전송해야할지 아직 수정 전 임의로 만들어놓은 예시
                val nickname = user.kakaoAccount?.profile?.nickname
                val email = user.kakaoAccount?.email

                // 사용자 정보 백엔드로 전송
                sendUserInfoToBackend(nickname, email)

                // 사용자 메인 화면으로 전환
                val intent = Intent(this, UserMainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun sendUserInfoToBackend(nickname: String?, email: String?) {
        // Retrofit으로 사용자 정보 백엔드로 전송
        val retrofit = Retrofit.Builder()
            .baseUrl("https://your-backend-api.com")  // 백엔드 API의 기본 URL(아직 수정 전)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        // 사용자 정보 전송(아직 수정 전)
        val userInfo = UserInfo(nickname ?: "", email ?: "")
        apiService.sendUserInfo(userInfo).enqueue(object : retrofit2.Callback<Void> {
            override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                Log.i("KakaoLogin", "사용자 정보 전송 성공")
            }

            override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                Log.e("KakaoLogin", "사용자 정보 전송 실패", t)
            }
        })
    }
}

data class UserInfo(val nickname: String, val email: String)

interface ApiService {
    @POST("/user/info")
    fun sendUserInfo(@Body userInfo: UserInfo): retrofit2.Call<Void>
}
