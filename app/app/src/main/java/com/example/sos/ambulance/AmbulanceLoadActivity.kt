package com.example.sos.ambulance

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.AmbulanceLoadResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AmbulanceLoadActivity : AppCompatActivity() {
    private lateinit var tokenManager: TokenManager
    private lateinit var authService: AuthService

    private lateinit var nameTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var telephoneNumberTextView: TextView
    private lateinit var memberImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ambulance_load_member)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // UI 요소 초기화
        nameTextView = findViewById(R.id.text_view_name)
        addressTextView = findViewById(R.id.text_view_address)
        telephoneNumberTextView = findViewById(R.id.text_view_telephone_number)
        memberImageView = findViewById(R.id.image_view_member)

        tokenManager = TokenManager(this)
        authService = RetrofitClientInstance.getApiService(tokenManager)

        // TokenManager에서 ID 가져오기
        val ambulanceId = tokenManager.getTokenId()
        fetchMemberDetails(ambulanceId)
    }

    private fun fetchMemberDetails(ambulanceId: String) {
        val authorization = "Bearer ${tokenManager.getAccessToken()}" // JWT 토큰 가져오기

        // API 호출
        authService.getAmbulanceMember(authorization, ambulanceId).enqueue(object : Callback<AmbulanceLoadResponse> {
            override fun onResponse(call: Call<AmbulanceLoadResponse>, response: Response<AmbulanceLoadResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { MemberLoadResponse ->
                        // 멤버 정보 가져오기
                        nameTextView.text = MemberLoadResponse.data.name
                        addressTextView.text = MemberLoadResponse.data.address
                        telephoneNumberTextView.text = MemberLoadResponse.data.telephoneNumber

                        // Firebase에서 이미지 로드
                        loadImageFromFirebase(MemberLoadResponse.imageUrl)
                    }
                } else {
                    Log.e("AmbulanceLoadActivity", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@AmbulanceLoadActivity, "정보 로드 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AmbulanceLoadResponse>, t: Throwable) {
                Log.e("AmbulanceLoadActivity", "Failure: ${t.message}")
                Toast.makeText(this@AmbulanceLoadActivity, "서버 연결 오류", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadImageFromFirebase(imageUri: String) {
        // Glide를 사용하여 Firebase에서 이미지 로드
        Glide.with(this)
            .load(imageUri)
            .into(memberImageView)
    }
}
