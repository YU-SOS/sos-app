package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.HospitalRes
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.retrofit.SearchHospitalResponse
import com.example.sos.token.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AmbulanceSearchHospitalActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var apiService: AuthService
    private val hospitalList = mutableListOf<HospitalRes>() // 병원 정보를 담을 리스트

    // 체크박스 선언
    private lateinit var pediatricsCheckBox: CheckBox
    private lateinit var orthopedicsCheckBox: CheckBox
    private lateinit var hospitalContainer: LinearLayout // 병원 정보를 표시할 LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ambulance_search_hospital)

        tokenManager = TokenManager(this)
        apiService = RetrofitClientInstance.getApiService(tokenManager)

        // 체크박스 초기화
        pediatricsCheckBox = findViewById(R.id.checkBoxPediatrics)
        orthopedicsCheckBox = findViewById(R.id.checkBoxOrthopedics)
        hospitalContainer = findViewById(R.id.recyclerView) // 병원 정보를 표시할 레이아웃

        val searchButton = findViewById<Button>(R.id.search_hospital_button)
        searchButton.setOnClickListener {
            searchHospitalDetails() // 병원 검색 버튼 클릭 시 병원 정보 가져오기
        }
    }

    // 병원 정보를 리스트에 추가
    private fun searchHospitalDetails() {
        val accessToken = tokenManager.getAccessToken()
        val selectedCategories = mutableListOf<String>()

        // 체크된 카테고리 추가
        if (pediatricsCheckBox.isChecked) {
            selectedCategories.add("소아과")
        }
        if (orthopedicsCheckBox.isChecked) {
            selectedCategories.add("정형외과")
        }

        accessToken?.let {
            // 선택된 카테고리가 없을 경우 기본값으로 소아과와 정형외과 사용
            if (selectedCategories.isEmpty()) {
                selectedCategories.add("소아과")
                selectedCategories.add("정형외과")
            }

            // API 호출
            apiService.searchHospital("Bearer $it", selectedCategories, 0).enqueue(object : Callback<SearchHospitalResponse> {
                override fun onResponse(call: Call<SearchHospitalResponse>, response: Response<SearchHospitalResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { hospitalResponse ->
                            hospitalList.clear() // 기존 병원 리스트 초기화
                            hospitalList.addAll(hospitalResponse.data.hospitals) // 병원 정보를 리스트에 추가

                            // 페이지 정보를 로그에 출력 (예: 총 페이지 수)
                            val totalPages = hospitalResponse.data.page.totalPages
                            val totalElements = hospitalResponse.data.page.totalElements
                            Log.d("HospitalSearch", "Total Pages: $totalPages, Total Elements: $totalElements")

                            displayHospitals() // 병원 목록을 화면에 표시
                        }
                    } else {
                        Log.e("HospitalSearch", "실패: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<SearchHospitalResponse>, t: Throwable) {
                    Log.e("HospitalSearch", "에러 발생: ${t.message}")
                }
            })
        }
    }

    // 병원 목록을 화면에 표시
    private fun displayHospitals() {
        hospitalContainer.removeAllViews() // 기존 병원 정보 삭제

        for (hospital in hospitalList) {
            val hospitalTextView = TextView(this).apply {
                text = hospital.name // 병원 이름 표시
                setOnClickListener {
                    // HospitalDetailActivity로 이동하고 병원 ID 전달
                    val intent = Intent(this@AmbulanceSearchHospitalActivity, HospitalDetailActivity::class.java)
                    intent.putExtra("HOSPITAL_ID", hospital.id) // 병원 ID 전송
                    startActivity(intent)
                }
            }
            hospitalContainer.addView(hospitalTextView) // LinearLayout에 추가
        }
    }
}
