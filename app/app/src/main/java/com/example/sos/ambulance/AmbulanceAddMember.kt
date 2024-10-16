package com.example.sos.ambulance

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.MemberRequest
import com.example.sos.retrofit.MemberResponse
import com.example.sos.token.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AmbulanceAddMember : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var authService: AuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ambulance_add_member)

        // TokenManager 및 Retrofit 초기화
        tokenManager = TokenManager(this)
        val retrofit = Retrofit.Builder()
            .baseUrl("YOUR_API_BASE_URL") // 실제 API의 base URL로 변경하세요
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        authService = retrofit.create(AuthService::class.java)

        // EditText와 Button 연결
        val nameEditText = findViewById<EditText>(R.id.edit_text_name_member)
        val phoneEditText = findViewById<EditText>(R.id.edit_text_member_phone)
        val addButton = findViewById<Button>(R.id.button)

        addButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val phoneNumber = phoneEditText.text.toString().trim()

            if (name.isNotEmpty() && phoneNumber.isNotEmpty()) {
                addMember(name, phoneNumber)
            } else {
                Toast.makeText(this, "이름과 전화번호 입력", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addMember(name: String, phoneNumber: String) {
        val ambulanceId = "YOUR_AMBULANCE_ID" // 구급대 조회 완성 후 여기로 가져와서 보내기.
        val authorization = "Bearer ${tokenManager.getAccessToken()}" // JWT 토큰 가져오기

        val memberRequest = MemberRequest(name, phoneNumber)

        authService.addAmbulanceMember(authorization, ambulanceId, memberRequest).enqueue(object : Callback<MemberResponse> {
            override fun onResponse(call: Call<MemberResponse>, response: Response<MemberResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        Toast.makeText(this@AmbulanceAddMember, it.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("AmbulanceAddMember", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@AmbulanceAddMember, "등록 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MemberResponse>, t: Throwable) {
                Log.e("AmbulanceAddMember", "Failure: ${t.message}")
                Toast.makeText(this@AmbulanceAddMember, "서버 연결 오류", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
