package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.HospitalRes
import com.example.sos.Page
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_hospital)

        tokenManager = TokenManager(this)
        authService = RetrofitClientInstance.getApiService(tokenManager)

        recyclerView = findViewById(R.id.recycler_view_hospitals)
        recyclerView.layoutManager = LinearLayoutManager(this)
        hospitalAdapter = HospitalAdapter { hospitalId ->
            val intent = Intent(this, DetailHospitalActivity::class.java)
            intent.putExtra("hospitalId", hospitalId)
            startActivity(intent)
        }
        recyclerView.adapter = hospitalAdapter

        // 초기 병원 목록 로드
        fetchHospitals(null)

        // 카테고리 버튼 그룹 설정
        setupCategoryToggleButtons()
    }

    private fun setupCategoryToggleButtons() {
        val buttonIds = listOf(
            R.id.button_gynecology to "산부인과",
            R.id.button_orthopedics to "정형외과",
            R.id.button_chest to "흉부외과",
            R.id.button_burn to "화상외과",
            R.id.button_internal to "내과"
        )

        buttonIds.forEach { (buttonId, _) ->
            findViewById<MaterialButton>(buttonId).setOnClickListener {
                fetchHospitals(getSelectedCategories(buttonIds))
            }
        }
    }

    private fun getSelectedCategories(buttonIds: List<Pair<Int, String>>): List<String> {
        val selectedCategories = mutableListOf<String>()

        buttonIds.forEach { (buttonId, categoryName) ->
            val button = findViewById<MaterialButton>(buttonId)
            if (button.isChecked) {
                selectedCategories.add(categoryName)
            }
        }

        return selectedCategories
    }

    private fun fetchHospitals(categories: List<String>?) {
        val jwtToken = tokenManager.getAccessToken()
        if (jwtToken.isNullOrEmpty()) {
            Toast.makeText(this, "No token found. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        authService.getHospitalList("Bearer $jwtToken", categories = categories, page = 0)
            .enqueue(object : Callback<HospitalLoadResponse<HospitalRes>> {
                override fun onResponse(
                    call: Call<HospitalLoadResponse<HospitalRes>>,
                    response: Response<HospitalLoadResponse<HospitalRes>>
                ) {
                    if (response.isSuccessful) {
                        val hospitalList = response.body()?.data?.content
                        if (!hospitalList.isNullOrEmpty()) {
                            Log.d("LoadHospitalActivity", "Fetched hospitals: $hospitalList")
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
