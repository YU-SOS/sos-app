package com.example.sos.ambulance

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.sos.HospitalRes
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.retrofit.HospitalDetailResponse
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

        // 이전 Intent에서 병원 ID 가져오기
        val hospitalId = intent.getStringExtra("HOSPITAL_ID") ?: return

        // 병원 상세 정보 요청
        getHospitalDetails(hospitalId)
    }

    private fun getHospitalDetails(hospitalId: String) {
        val accessToken = tokenManager.getAccessToken()

        accessToken?.let {
            apiService.getHospitalDetails("Bearer $it", hospitalId).enqueue(object : Callback<HospitalDetailResponse> {
                override fun onResponse(call: Call<HospitalDetailResponse>, response: Response<HospitalDetailResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { hospitalResponse ->
                            displayHospitalDetails(hospitalResponse.data) // 상세 정보 표시
                        }
                    } else {
                        Log.e("HospitalDetail", "실패: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<HospitalDetailResponse>, t: Throwable) {
                    Log.e("HospitalDetail", "에러 발생: ${t.message}")
                }
            })
        }
    }

    private fun displayHospitalDetails(hospital: HospitalRes) {
        findViewById<TextView>(R.id.hospitalNameTextView).text = hospital.name
        findViewById<TextView>(R.id.hospitalAddressTextView).text = hospital.address
        findViewById<TextView>(R.id.hospitalTelephoneTextView).text = hospital.telephoneNumber

        // 이미지 URL이 있다면 Glide를 사용해 이미지 로드
        val hospitalImageView: ImageView = findViewById(R.id.hospitalImageView)
        Glide.with(this)
            .load(hospital.imageUrl) // Firebase에서 로드
            .into(hospitalImageView)
    }
}
