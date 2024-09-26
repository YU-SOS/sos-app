package com.example.sos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterAmbulanceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_ambulance)

        // XML 레이아웃에서 EditText 및 Button 요소를 가져옴
        val idEditText = findViewById<EditText>(R.id.editTextUserId)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val confirmPasswordEditText = findViewById<EditText>(R.id.editTextConfirmPassword)
        val nameEditText = findViewById<EditText>(R.id.editTextAmbulanceName)
        val phoneEditText = findViewById<EditText>(R.id.editTextAmbulancePhone)
        val addressEditText = findViewById<EditText>(R.id.editTextAmbulanceAddress)
        val submitButton = findViewById<Button>(R.id.buttonSubmit)

        // 버튼 클릭 시 회원가입 시도
        submitButton.setOnClickListener {
            val id = idEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            val name = nameEditText.text.toString()
            val address = addressEditText.text.toString()
            val telephoneNumber = phoneEditText.text.toString()

            val longitude = "temp"
            val latitude = "temp"
            val location = Location(latitude, longitude)
            val imageUrl = "temp"

            // 비밀번호와 비밀번호 확인이 일치하는지 체크
            if (password != confirmPassword) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Retrofit을 이용한 회원가입 요청
            val authService = RetrofitClientInstance.retrofitInstance.create(AuthService::class.java)
            val registerRequest = RegisterRequest(id, password, name, address, telephoneNumber, location, imageUrl)

            authService.register(registerRequest).enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                    // 응답 성공 시 처리
                    if (response.isSuccessful && response.body()?.statusCode == "200") {
                        Toast.makeText(this@RegisterAmbulanceActivity, "회원가입 성공", Toast.LENGTH_SHORT).show()
                        // 메인 액티비티로 이동
                        startActivity(Intent(this@RegisterAmbulanceActivity, MainActivity::class.java))
                    } else {
                        Toast.makeText(this@RegisterAmbulanceActivity, "회원가입 실패: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    // 네트워크 오류 또는 서버 연결 실패 시 처리
                    Toast.makeText(this@RegisterAmbulanceActivity, "에러 발생: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}