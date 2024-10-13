package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.LogoutManager
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
    val logoutManager = LogoutManager(this, tokenManager)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tokenManager = TokenManager(this)  // TokenManager 초기화
        logoutButton = findViewById(R.id.logout)
        testButton = findViewById(R.id.test_button) // 테스트 버튼 연결

        logoutButton.setOnClickListener {
            logoutManager.logout()
        }
    }
}
