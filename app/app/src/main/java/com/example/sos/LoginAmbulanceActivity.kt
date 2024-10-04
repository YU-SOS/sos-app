package com.example.sos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginAmbulanceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_ambulance)

        // 사용자 입력 필드 가져오기
        val usernameEditText = findViewById<EditText>(R.id.edit_text_username)
        val passwordEditText = findViewById<EditText>(R.id.edit_text_password)
        val loginButton = findViewById<Button>(R.id.btn_login)
        val registerButton = findViewById<Button>(R.id.btn_register)

        // 로그인 버튼 클릭 시 동작
        loginButton.setOnClickListener {
            val id = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Retrofit을 통해 AuthService 생성
            val authService = RetrofitClientInstance.retrofitInstance.create(AuthService::class.java)
            val loginRequest = LoginRequest("AMB", id, password) // 로그인 요청 데이터 생성

            // 로그인 API 호출
            authService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    val loginResponse = response.body()

                    if (loginResponse != null && loginResponse.status == 200) {  // 응답 status가 200이면
                        // 서버로부터 받은 Authorization 헤더 가져오기
                        val authorizationHeader = response.headers().get("Authorization")
                        val refreshToken = response.headers().get("Set-Cookie") // 리프레시 토큰 가져오기

                        if (authorizationHeader != null) {
                            // 액세스 토큰 저장
                            saveToken(authorizationHeader)

                            // 리프레시 토큰 저장
                            saveRefreshToken(refreshToken)

                            Toast.makeText(this@LoginAmbulanceActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginAmbulanceActivity, MainActivity::class.java)) // 메인 화면으로 이동
                        } else {
                            Toast.makeText(this@LoginAmbulanceActivity, "토큰을 받지 못했습니다", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // 상태 코드가 200이 아닌 경우 실패 메시지 표시
                        val errorMessage = loginResponse?.message ?: "로그인 실패"
                        Toast.makeText(this@LoginAmbulanceActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginAmbulanceActivity, "에러 발생: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // 회원가입 버튼 클릭 시 동작
        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterAmbulanceActivity::class.java)) // 회원가입 화면으로 이동
        }
    }

    // JWT 토큰을 SharedPreferences에 저장하는 함수
    private fun saveToken(token: String?) {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("jwt_token", token).apply()
    }

    // 리프레시 토큰을 SharedPreferences에 저장하는 함수
    private fun saveRefreshToken(refreshToken: String?) {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        // Set-Cookie에서 실제 리프레시 토큰 값만 추출하여 저장
        refreshToken?.let {
            // "refreshToken" 키워드를 기준으로 토큰 값 추출 (예: refreshToken="token_value")
            val tokenValue = it.split(";").firstOrNull()?.split("=")?.get(1)
            sharedPreferences.edit().putString("refresh_token", tokenValue).apply()
        }
    }
}
