package com.example.sos.ambulance

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private val hospitalList = mutableListOf<SearchHospitalResponse>() // 병원 정보를 담을 리스트

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ambulance_search_hospital)

        tokenManager = TokenManager(this)
        apiService = RetrofitClientInstance.getApiService(tokenManager)

        val searchButton = findViewById<Button>(R.id.search_hospital_button)
        searchButton.setOnClickListener {
            searchHospitalDetails() // 병원 검색 버튼 클릭 시 병원 정보 가져오기
        }
    }

    // 병원 정보를 리스트에 추가
    private fun searchHospitalDetails() {
        val accessToken = tokenManager.getAccessToken()

        accessToken?.let {
            apiService.searchHospital("Bearer $it", listOf("소아과", "정형외과")).enqueue(object : Callback<SearchHospitalResponse> {
                override fun onResponse(call: Call<SearchHospitalResponse>, response: Response<SearchHospitalResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { hospitalResponse ->
                            hospitalList.add(hospitalResponse) // 병원 정보를 리스트에 추가
                            displayHospitals() // 병원 목록을 화면에 표시
                        }
                    } else {
                        Log.e("HospitalSearch", "Request failed")
                    }
                }

                override fun onFailure(call: Call<SearchHospitalResponse>, t: Throwable) {
                    Log.e("HospitalSearch", "Error: ${t.message}")
                }
            })
        }
    }

    // 병원 목록을 화면에 표시
    private fun displayHospitals() {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = HospitalAdapter(hospitalList, apiService, tokenManager) // RecyclerView 어댑터에 병원 목록 전달
    }
}
