package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.R
import com.example.sos.retrofit.AmbulanceRequest
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.SelectLoginActivity
import com.example.sos.retrofit.AmbulanceResponse
import com.example.sos.token.TokenManager
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.RefreshRequest
import com.example.sos.retrofit.RefreshResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var logoutButton: Button
    private lateinit var testButton: Button // 테스트 버튼 추가
    private lateinit var tokenManager: TokenManager // TokenManager 선언

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tokenManager = TokenManager(this)  // TokenManager 초기화
        logoutButton = findViewById(R.id.logout)
        testButton = findViewById(R.id.test_button) // 테스트 버튼 연결

        logoutButton.setOnClickListener {
            logout()
        }

        testButton.setOnClickListener {
            registerAmbulance() // 구급대 등록 요청 보내기
        }
    }

    private fun registerAmbulance() {
        val token = tokenManager.getAccessToken()
        val ambulanceId = "shinuk"
        if (token != null) {
            val authService = RetrofitClientInstance.getApiService(tokenManager, this)
            val ambulanceRequest = AmbulanceRequest(
                name = "Test Ambulance",
                phoneNumber = "123-456-7890"
            )

            authService.ambulanceMember("Bearer $token", ambulanceId, ambulanceRequest).enqueue(object : Callback<AmbulanceResponse> {
                override fun onResponse(call: Call<AmbulanceResponse>, response: Response<AmbulanceResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MainActivity, "구급대 등록 성공", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "구급대 등록 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AmbulanceResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "에러 발생: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun logout() {
        tokenManager.clearTokens()
        val intent = Intent(this, SelectLoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
