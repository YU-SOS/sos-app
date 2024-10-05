package com.example.sos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterAmbulanceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_ambulance)

        val idEditText = findViewById<EditText>(R.id.editTextUserId)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val confirmPasswordEditText = findViewById<EditText>(R.id.editTextConfirmPassword)
        val nameEditText = findViewById<EditText>(R.id.editTextAmbulanceName)
        val phoneEditText = findViewById<EditText>(R.id.editTextAmbulancePhone)
        val addressEditText = findViewById<EditText>(R.id.editTextAmbulanceAddress)
        val submitButton = findViewById<Button>(R.id.buttonSubmit)

        submitButton.setOnClickListener {
            val id = idEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            val name = nameEditText.text.toString()
            val telephoneNumber = phoneEditText.text.toString()
            val address = addressEditText.text.toString()

            if (password != confirmPassword) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 카카오 주소 검색 API 호출
            lifecycleScope.launch {
                val kakaoService = KakaoRetrofitClientInstance.kakaoService
                val response = kakaoService.searchAddress("KakaoAK 24a76ea9dc5ffd6677de0900eedb7f72", address)
                if (response.isSuccessful) {
                    val documents = response.body()?.documents
                    if (!documents.isNullOrEmpty()) {
                        val latitude = documents[0].y
                        val longitude = documents[0].x
                        val location = Location(latitude, longitude)
                        val imageUrl = "temp" // 이미지 URL 처리

                        // 회원가입 요청 보내기
                        registerAmbulance(
                            RegisterRequest(id, password, name, address, telephoneNumber, location, imageUrl)
                        )
                    } else {
                        Toast.makeText(this@RegisterAmbulanceActivity, "주소 검색 실패", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@RegisterAmbulanceActivity, "주소 검색 실패 : ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun registerAmbulance(registerRequest: RegisterRequest) {
        val authService = RetrofitClientInstance.api
        authService.register(registerRequest).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful && response.body()?.statusCode == "200") {
                    Toast.makeText(this@RegisterAmbulanceActivity, "회원가입 성공", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterAmbulanceActivity, MainActivity::class.java))
                } else {
                    Toast.makeText(this@RegisterAmbulanceActivity, "회원가입 실패: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Toast.makeText(this@RegisterAmbulanceActivity, "에러 발생: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
