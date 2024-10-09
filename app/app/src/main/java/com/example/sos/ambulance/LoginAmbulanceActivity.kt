package com.example.sos.ambulance

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.R
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.LoginRequest
import com.example.sos.retrofit.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

            Log.d("LoginAmbulanceActivity", "로그인 시도: ID = $id") // 로그인 시도 로그

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
                        if (authorizationHeader != null) {
                            // 액세스 토큰 저장
                            saveToken(authorizationHeader)
                            Log.d("LoginAmbulanceActivity", "로그인 성공: 토큰 저장 완료") // 로그인 성공 로그
                            Log.d("LoginAmbulanceActivity", "저장된 액세스 토큰: $authorizationHeader") // 토큰 출력
                            Toast.makeText(this@LoginAmbulanceActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginAmbulanceActivity, MainActivity::class.java)) // 메인 화면으로 이동
                        } else {
                            Log.d("LoginAmbulanceActivity", "로그인 실패: 토큰을 받지 못했습니다") // 토큰 없음 로그
                            Toast.makeText(this@LoginAmbulanceActivity, "토큰을 받지 못했습니다", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // 상태 코드가 200이 아닌 경우 실패 메시지 표시
                        val errorMessage = loginResponse?.message ?: "로그인 실패"
                        Log.d("LoginAmbulanceActivity", "로그인 실패: $errorMessage") // 로그인 실패 로그
                        Toast.makeText(this@LoginAmbulanceActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Log.e("LoginAmbulanceActivity", "로그인 에러: ${t.message}", t) // 로그인 에러 로그
                    Toast.makeText(this@LoginAmbulanceActivity, "에러 발생: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // 회원가입 버튼 클릭 시 동작
        registerButton.setOnClickListener {
            Log.d("LoginAmbulanceActivity", "회원가입 버튼 클릭") // 회원가입 클릭 로그
            startActivity(Intent(this, RegisterAmbulanceActivity::class.java)) // 회원가입 화면으로 이동
        }
    }

    // JWT 토큰을 SharedPreferences에 저장하는 함수
    private fun saveToken(token: String?) {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("jwt_token", token).apply()
        Log.d("LoginAmbulanceActivity", "JWT 토큰 저장: $token") // 토큰 저장 로그
    }
}
