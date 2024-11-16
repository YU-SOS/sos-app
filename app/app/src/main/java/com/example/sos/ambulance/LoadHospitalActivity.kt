package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.HospitalRes
import com.example.sos.Page
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoadHospitalActivity : AppCompatActivity() {

    private lateinit var authService: AuthService
    private lateinit var tokenManager: TokenManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var hospitalAdapter: HospitalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_hospital)

        tokenManager = TokenManager(this)
        authService = RetrofitClientInstance.getApiService(tokenManager)

        recyclerView = findViewById(R.id.recycler_view_hospitals)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // hospitalAdapter를 클릭 리스너와 함께 초기화
        hospitalAdapter = HospitalAdapter(emptyList()) { hospitalId ->
            val intent = Intent(this, DetailHospitalActivity::class.java)
            intent.putExtra("hospitalId", hospitalId)  // hospitalId를 전달
            startActivity(intent)
        }

        recyclerView.adapter = hospitalAdapter

        // 초기 병원 목록 로드
        fetchHospitals(null)

        // 카테고리 버튼 그룹 설정
        setupCategoryToggleButtons()
    }

    private fun setupCategoryToggleButtons() {
        val buttonGroup = findViewById<MaterialButtonToggleGroup>(R.id.toggle_group_categories)

        // 버튼 클릭 이벤트 설정
        val buttonIds = listOf(
            R.id.button_gynecology to "산부인과",
            R.id.button_orthopedics to "정형외과",
            R.id.button_chest to "흉부외과",
            R.id.button_burn to "화상외과",
            R.id.button_internal to "내과"
        )

        buttonIds.forEach { (buttonId, _) ->
            findViewById<MaterialButton>(buttonId).setOnClickListener {
                fetchHospitals(getSelectedCategories())
            }
        }
    }

    private fun getSelectedCategories(): List<String> {
        val categories = mutableListOf<String>()
        val buttonGroup = findViewById<MaterialButtonToggleGroup>(R.id.toggle_group_categories)

        val buttonIds = listOf(
            R.id.button_gynecology to "산부인과",
            R.id.button_orthopedics to "정형외과",
            R.id.button_chest to "흉부외과",
            R.id.button_burn to "화상외과",
            R.id.button_internal to "내과"
        )

        buttonIds.forEach { (buttonId, categoryName) ->
            val button = findViewById<MaterialButton>(buttonId)
            if (buttonGroup.checkedButtonIds.contains(button.id)) {
                categories.add(categoryName)
            }
        }
        return categories
    }

    private fun fetchHospitals(categories: List<String>?) {
        val jwtToken = tokenManager.getAccessToken()
        if (jwtToken != null) {
            authService.getHospitalList("Bearer $jwtToken", categories = categories, page = 0)
                .enqueue(object : Callback<Page<HospitalRes>> {
                    override fun onResponse(call: Call<Page<HospitalRes>>, response: Response<Page<HospitalRes>>) {
                        if (response.isSuccessful) {
                            response.body()?.content?.let { hospitalList ->
                                hospitalAdapter.updateData(hospitalList)
                            } ?: run {
                                Toast.makeText(this@LoadHospitalActivity, "No data available", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@LoadHospitalActivity, "Failed to retrieve hospitals", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Page<HospitalRes>>, t: Throwable) {
                        Toast.makeText(this@LoadHospitalActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            Toast.makeText(this, "No token found. Please log in again.", Toast.LENGTH_SHORT).show()
        }
    }
}
