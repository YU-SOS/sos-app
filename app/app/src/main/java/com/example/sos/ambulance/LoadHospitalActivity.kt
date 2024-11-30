package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.R
import com.example.sos.res.HospitalRes
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.HospitalLoadResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
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
        setContentView(R.layout.activity_hospital_load)

        tokenManager = TokenManager(this)
        authService = RetrofitClientInstance.getApiService(tokenManager)

        recyclerView = findViewById(R.id.recycler_view_hospitals)
        recyclerView.layoutManager = LinearLayoutManager(this)
        hospitalAdapter = HospitalAdapter { hospitalId, hospitalName ->
            val intent = Intent().apply {
                putExtra("selectedHospitalId", hospitalId)
                putExtra("selectedHospitalName", hospitalName)
            }
            setResult(RESULT_OK, intent)
            finish()
        }
        recyclerView.adapter = hospitalAdapter

        nextPageButton = findViewById(R.id.button_next_page)
        previousPageButton = findViewById(R.id.button_previous_page)

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
            val button = findViewById<Button>(buttonId)
            button.setOnClickListener {
                if (selectedCategories.contains(category)) {
                    selectedCategories.remove(category)
                } else {
                    selectedCategories.add(category)
                }
                currentPage = 0
                fetchHospitals()
            }
        }
    }

    private fun fetchHospitals() {
        val jwtToken = tokenManager.getAccessToken()
        if (jwtToken.isNullOrEmpty()) {
            Toast.makeText(this, "토큰 오류입니다. 다시 로그인 해주세요.", Toast.LENGTH_SHORT).show()
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
                        totalPages = response.body()?.data?.page?.totalPages ?: 1

                        if (!hospitalList.isNullOrEmpty()) {
                            // 기존 데이터를 새 데이터로 교체
                            hospitalAdapter.updateData(hospitalList)
                        } else {
                            Toast.makeText(this@LoadHospitalActivity, "No data available.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(
                            this@LoadHospitalActivity,
                            "구급대원 불러오기에 실패했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<HospitalLoadResponse<HospitalRes>>, t: Throwable) {
                    Toast.makeText(this@LoadHospitalActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
