package com.example.sos.ambulance

import HospitalAdapter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.retrofit.SearchHospitalResponse
import com.example.sos.token.TokenManager
import com.example.sos.Hospital
import com.example.sos.LogoutManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AmbulanceSearchHospitalActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var apiService: AuthService
    private lateinit var hospitalListTextView: TextView  // TextView 추가

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ambulance_search_hospital)

        tokenManager = TokenManager(this)
        apiService = RetrofitClientInstance.getApiService(tokenManager)
        val logoutManager = LogoutManager(this, tokenManager)
        // TextView 초기화
        hospitalListTextView = findViewById(R.id.hospitalListTextView)

        val accessToken = tokenManager.getAccessToken()
        val categories = listOf("소아과", "정형외과")

        searchHospitals(accessToken, categories)

        val logoutButton: Button = findViewById(R.id.logout_button)
        logoutButton.setOnClickListener {
            logoutManager.logout()
        }
    }

    private fun searchHospitals(accessToken: String?, categories: List<String>) {
        accessToken?.let {
            apiService.searchHospital("Bearer $it", categories).enqueue(object : Callback<SearchHospitalResponse> {
                override fun onResponse(call: Call<SearchHospitalResponse>, response: Response<SearchHospitalResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { hospitalResponse ->
                            displayHospital(hospitalResponse)
                        }
                    } else {
                        Log.e("HospitalActivity", "Request failed: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<SearchHospitalResponse>, t: Throwable) {
                    Log.e("HospitalActivity", "Error: ${t.message}")
                }
            })
        }
    }

    private fun displayHospital(response: SearchHospitalResponse) {
        // 응답 데이터가 null인지 확인
        if (response.id == null || response.name == null) {
            Log.e("AmbulanceSearchHospitalActivity", "응답 데이터가 유효하지 않습니다.")
            return
        }

        // 유효한 데이터로 병원 정보 생성
        val hospitalData = """
        병원 ID: ${response.id}
        병원 이름: ${response.name}
        병원 주소: ${response.address}
        병원 전화번호: ${response.telephoneNumber}
        병원 위치: ${response.location}
        병원 카테고리: ${response.categories.joinToString(", ")}
    """.trimIndent()

        // 병원 정보를 텍스트 뷰에 설정
        hospitalListTextView.text = hospitalData
    }

}
