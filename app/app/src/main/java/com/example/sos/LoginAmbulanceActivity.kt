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

class LoginAmbulanceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_ambulance)

        val usernameEditText = findViewById<EditText>(R.id.edit_text_username)
        val passwordEditText = findViewById<EditText>(R.id.edit_text_password)
        val loginButton = findViewById<Button>(R.id.btn_login)
        val registerButton = findViewById<Button>(R.id.btn_register) // 회원가입 버튼 추가

        // 로그인 버튼 클릭 시 작동
        loginButton.setOnClickListener {
            val id = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            val authService = RetrofitClientInstance.retrofitInstance.create(AuthService::class.java)
            val loginRequest = LoginRequest(id, password)

            authService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val token = response.body()?.token
                        saveToken(token)
                        Toast.makeText(this@LoginAmbulanceActivity, "success", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginAmbulanceActivity, MainActivity::class.java))
                    } else {
                        Toast.makeText(this@LoginAmbulanceActivity, "fail", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginAmbulanceActivity, "error", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // 회원가입 버튼 클릭 시 액티비티 이동
        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterAmbulanceActivity::class.java))
        }
    }

    private fun saveToken(token: String?) {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("jwt_token", token).apply()
    }
}
