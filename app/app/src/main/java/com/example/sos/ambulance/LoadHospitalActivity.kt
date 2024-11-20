package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.res.HospitalRes
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.HospitalLoadResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoadHospitalActivity : AppCompatActivity() {

    private lateinit var authService: AuthService
    private lateinit var tokenManager: TokenManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var hospitalAdapter: HospitalAdapter
    private lateinit var nextPageButton: Button
    private lateinit var previousPageButton: Button

    private val selectedCategories = mutableSetOf<String>()
    private var currentPage = 0
    private var totalPages = 1 // 초기값은 1로 설정

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_hospital)

        tokenManager = TokenManager(this)
        authService = RetrofitClientInstance.getApiService(tokenManager)

        recyclerView = findViewById(R.id.recycler_view_hospitals)
        recyclerView.layoutManager = LinearLayoutManager(this)
        hospitalAdapter = HospitalAdapter { _, hospitalName ->
            val intent = Intent().apply {
                putExtra("selectedHospitalName", hospitalName)
            }
            setResult(RESULT_OK, intent)
            finish() // LoadHospitalActivity 종료
        }
        recyclerView.adapter = hospitalAdapter

        nextPageButton = findViewById(R.id.button_next_page)
        previousPageButton = findViewById(R.id.button_previous_page)

        // 항상 버튼 활성화
        nextPageButton.isEnabled = true
        previousPageButton.isEnabled = true

        nextPageButton.setOnClickListener {
            if (currentPage >= totalPages - 1) {
                Toast.makeText(this, "마지막 페이지입니다.", Toast.LENGTH_SHORT).show()
            } else {
                currentPage++
                fetchHospitals()
            }
        }

        previousPageButton.setOnClickListener {
            if (currentPage <= 0) {
                Toast.makeText(this, "첫 페이지입니다.", Toast.LENGTH_SHORT).show()
            } else {
                currentPage--
                fetchHospitals()
            }
        }

        // 초기 병원 목록 로드
        fetchHospitals()

        // 카테고리 버튼 설정
        setupCategoryButtons()
    }

    private fun setupCategoryButtons() {
        val buttonCategoryMap = mapOf(
            R.id.button_internal to "내과",
            R.id.button_orthopedics to "정형외과",
            R.id.button_gynecology to "산부인과",
            R.id.button_chest to "흉부외과",
            R.id.button_burn to "화상외과"
        )

        buttonCategoryMap.forEach { (buttonId, category) ->
            val button = findViewById<MaterialButton>(buttonId)
            button.setOnClickListener {
                if (selectedCategories.contains(category)) {
                    selectedCategories.remove(category) // 선택 해제
                } else {
                    selectedCategories.add(category) // 선택
                }
                currentPage = 0 // 페이지 초기화
                fetchHospitals() // 병원 목록 다시 검색
            }
        }
    }

    private fun fetchHospitals() {
        val jwtToken = tokenManager.getAccessToken()
        if (jwtToken.isNullOrEmpty()) {
            Toast.makeText(this, "No token found. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        authService.getHospitalList("Bearer $jwtToken", categories = selectedCategories.toList(), page = currentPage)
            .enqueue(object : Callback<HospitalLoadResponse<HospitalRes>> {
                override fun onResponse(
                    call: Call<HospitalLoadResponse<HospitalRes>>,
                    response: Response<HospitalLoadResponse<HospitalRes>>
                ) {
                    if (response.isSuccessful) {
                        val hospitalList = response.body()?.data?.content
                        totalPages = response.body()?.data?.page?.totalPages ?: 1 // 총 페이지 수 업데이트

                        if (!hospitalList.isNullOrEmpty()) {
                            Log.d("LoadHospitalActivity", "Fetched hospitals: $hospitalList")

                            // 기존 데이터를 새 데이터로 교체
                            hospitalAdapter.updateData(hospitalList)
                        } else {
                            Log.d("LoadHospitalActivity", "No hospitals found")
                            Toast.makeText(this@LoadHospitalActivity, "No data available.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("LoadHospitalActivity", "Failed to retrieve hospitals. Error: ${response.message()}")
                        Toast.makeText(
                            this@LoadHospitalActivity,
                            "Failed to retrieve hospitals. Error: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<HospitalLoadResponse<HospitalRes>>, t: Throwable) {
                    Log.e("LoadHospitalActivity", "Error: ${t.message}")
                    Toast.makeText(this@LoadHospitalActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
