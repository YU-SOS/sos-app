package com.example.sos

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.ReceptionGuestResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GuestSearchReceptionActivity : AppCompatActivity() {

    private lateinit var apiService: AuthService
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guest_search_reception)

        // Retrofit 초기화
        tokenManager = TokenManager(this)
        apiService = RetrofitClientInstance.getApiService(tokenManager)

        // 뷰 초기화
        val receptionIdInput: EditText = findViewById(R.id.reception_id_input)
        val searchButton: Button = findViewById(R.id.search_reception_button)
        val hospitalInfoTextView: TextView = findViewById(R.id.hospital_info_textview)
        val ambulanceInfoTextView: TextView = findViewById(R.id.ambulance_info_textview)

        // 조회 버튼 클릭 리스너
        searchButton.setOnClickListener {
            val receptionId = receptionIdInput.text.toString()
            if (receptionId.isNotEmpty()) {
                searchReception(receptionId, hospitalInfoTextView, ambulanceInfoTextView)
            } else {
                hospitalInfoTextView.text = "Reception ID를 입력해주세요."
            }
        }
    }

    // 서버로 Reception ID 조회 요청
    private fun searchReception(receptionId: String, hospitalInfoTextView: TextView, ambulanceInfoTextView: TextView) {
        apiService.getReceptionGuest(receptionId).enqueue(object : Callback<ReceptionGuestResponse> {
            override fun onResponse(call: Call<ReceptionGuestResponse>, response: Response<ReceptionGuestResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { receptionResponse ->
                        val hospital = receptionResponse.data.hospital
                        val ambulance = receptionResponse.data.ambulance

                        // 병원 정보 설정
                        hospitalInfoTextView.text = """
                            병원 이름: ${hospital.name}
                            주소: ${hospital.address}
                            이미지 URL: ${hospital.imageUrl}
                            위치: (${hospital.location.latitude}, ${hospital.location.longitude})
                        """.trimIndent()

                        // 구급차 정보 설정
                        ambulanceInfoTextView.text = """
                            구급차 이름: ${ambulance.name}
                            주소: ${ambulance.address}
                            이미지 URL: ${ambulance.imageUrl}
                        """.trimIndent()
                    }
                } else {
                    hospitalInfoTextView.text = "조회 실패: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<ReceptionGuestResponse>, t: Throwable) {
                Log.e("GuestSearchReception", "조회 오류", t)
                hospitalInfoTextView.text = "조회 중 오류 발생: ${t.message}"
            }
        })
    }
}
