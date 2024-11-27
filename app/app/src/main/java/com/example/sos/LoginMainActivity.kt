package com.example.sos

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.auth0.android.jwt.JWT
import com.example.sos.ambulance.AmbulanceMainActivity
import com.example.sos.ambulance.LoginAmbulanceActivity
import com.example.sos.ambulance.RegisterAmbulanceActivity
import com.example.sos.retrofit.LoginRequest
import com.example.sos.retrofit.LoginResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.retrofit.UserLoginResponse
import com.example.sos.retrofit.UserSignupRequest
import com.example.sos.token.TokenManager
import com.example.sos.user.UserMapActivity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_main)

        // 텍스트와 바 초기화
        val userText = findViewById<TextView>(R.id.userText)
        val ambulanceText = findViewById<TextView>(R.id.ambulanceText)
        val userBar = findViewById<View>(R.id.userBar)
        val ambulanceBar = findViewById<View>(R.id.ambulanceBar)

        val userImageButton = findViewById<ImageButton>(R.id.userImageButton)
        val inputField1 = findViewById<EditText>(R.id.edit_text_username)
        val inputField2 = findViewById<EditText>(R.id.edit_text_password)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerText = findViewById<TextView>(R.id.registerText)
        registerText.paintFlags = registerText.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG

        val logoutButton = findViewById<ImageButton>(R.id.logout_button)
        logoutButton.visibility = View.GONE
        // 유저 텍스트 클릭 시
        userText.setOnClickListener {
            // 텍스트 스타일 변경
            userText.setTypeface(null, Typeface.BOLD)
            userText.setTextColor(resources.getColor(R.color.black, null))
            ambulanceText.setTypeface(null, Typeface.NORMAL)

            // 바 표시/숨김
            userBar.visibility = View.VISIBLE
            ambulanceBar.visibility = View.GONE

            // 유저용 요소 표시
            userImageButton.visibility = View.VISIBLE

            // 구급대용 요소 숨김
            inputField1.visibility = View.GONE
            inputField2.visibility = View.GONE
            loginButton.visibility = View.GONE
            registerText.visibility = View.GONE
        }

        // 구급대 텍스트 클릭 시
        ambulanceText.setOnClickListener {
            // 텍스트 스타일 변경
            ambulanceText.setTypeface(null, Typeface.BOLD)
            ambulanceText.setTextColor(resources.getColor(R.color.black, null))
            userText.setTypeface(null, Typeface.NORMAL)

            // 바 표시/숨김
            ambulanceBar.visibility = View.VISIBLE
            userBar.visibility = View.GONE

            // 구급대용 요소 표시
            inputField1.visibility = View.VISIBLE
            inputField2.visibility = View.VISIBLE
            loginButton.visibility = View.VISIBLE
            registerText.visibility = View.VISIBLE

            // 유저용 요소 숨김
            userImageButton.visibility = View.GONE
        }

        //AmbulanceLogin 부분
        // 사용자 입력 필드 가져오기
        val usernameEditText = findViewById<EditText>(R.id.edit_text_username)
        val passwordEditText = findViewById<EditText>(R.id.edit_text_password)

        // 로그인 버튼 클릭 시 동작
        loginButton.setOnClickListener {
            val id = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Retrofit을 통해 AuthService 생성
            val tokenManager = TokenManager(this)
            val authService = RetrofitClientInstance.getApiService(tokenManager)

            val loginRequest = LoginRequest("AMB", id, password) // 로그인 요청 데이터 생성

            // 로그인 API 호출
            authService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    val loginResponse = response.body()

                    if (loginResponse != null && loginResponse.status == 200) {  // 응답 status가 200이면
                        // 서버로부터 받은 Authorization 헤더 가져오기
                        val authorizationHeader = response.headers().get("Authorization")
                        if (authorizationHeader != null) {
                            // 액세스 토큰 저장
                            saveToken(authorizationHeader)
                            Toast.makeText(this@LoginMainActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginMainActivity, AmbulanceMainActivity::class.java)) // 메인 화면으로 이동
                            finish()
                        } else {
                            Toast.makeText(this@LoginMainActivity, "토큰을 받지 못했습니다", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // 상태 코드가 200이 아닌 경우 실패 메시지 표시
                        val errorMessage = loginResponse?.message ?: "로그인 실패"
                        Toast.makeText(this@LoginMainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginMainActivity, "에러 발생: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // 회원가입 버튼 클릭 시 동작
        registerText.setOnClickListener {
            val intent = Intent(this, RegisterAmbulanceActivity::class.java)
            startActivity(intent)
        }

        //UserLogin부분
        val kakaoLoginButton: ImageButton = findViewById(R.id.userImageButton)
        kakaoLoginButton.setOnClickListener {
            kakaoLogin()
        }
    }

    // JWT 토큰을 SharedPreferences에 저장하는 함수
    private fun saveToken(token: String?) {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("jwt_token", token).apply()
    }

    //UserLogin부분
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


        val tokenManager = TokenManager(this)
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
                        val intent = Intent(this@LoginMainActivity, UserMapActivity::class.java)
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
