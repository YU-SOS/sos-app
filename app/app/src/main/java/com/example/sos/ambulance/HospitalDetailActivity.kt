package com.example.sos.ambulance

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.HospitalDetailResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HospitalDetailActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var apiService: AuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital_detail)

        tokenManager = TokenManager(this)
        apiService = RetrofitClientInstance.getApiService(tokenManager)

        val hospitalId = intent.getStringExtra("hospitalId") ?: return
        fetchHospitalDetails(hospitalId)
    }

    private fun fetchHospitalDetails(hospitalId: String) {
        val accessToken = tokenManager.getAccessToken()
        apiService.getHospitalDetails("Bearer $accessToken", hospitalId).enqueue(object :
            Callback<HospitalDetailResponse> {
            override fun onResponse(call: Call<HospitalDetailResponse>, response: Response<HospitalDetailResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { details ->
                        // 병원 상세 정보를 화면에 표시
                        displayHospitalDetails(details)
                    }
                } else {
                    Log.e("HospitalDetail", "Failed to fetch details")
                }
            }

            override fun onFailure(call: Call<HospitalDetailResponse>, t: Throwable) {
                Log.e("HospitalDetail", "Error: ${t.message}")
            }
        })
    }

    private fun displayHospitalDetails(details: HospitalDetailResponse) {
        val detailsTextView: TextView = findViewById(R.id.hospitalDetailsTextView)
        detailsTextView.text = """
            병원 이름: ${details.name}
            주소: ${details.address}
            전화번호: ${details.telephoneNumber}
            응급실 상태: ${details.emergencyRoomStatus}
        """.trimIndent()
    }
}
