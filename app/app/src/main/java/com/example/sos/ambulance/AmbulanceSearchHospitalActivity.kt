package com.example.sos.ambulance

import HospitalAdapter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.retrofit.SearchHospitalResponse
import com.example.sos.token.TokenManager
import com.example.sos.Hospital
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AmbulanceSearchHostpialActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var apiService: AuthService
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HospitalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ambulance_search_hospital)

        tokenManager = TokenManager(this)
        apiService = RetrofitClientInstance.getApiService(tokenManager)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val accessToken = tokenManager.getAccessToken()
        val categories = listOf("소아과", "정형외과")

        searchHospitals(accessToken, categories)
    }

    private fun searchHospitals(accessToken: String?, categories: List<String>) {
        accessToken?.let {
            apiService.searchHospital("Bearer $it", categories).enqueue(object : Callback<SearchHospitalResponse> {
                override fun onResponse(call: Call<SearchHospitalResponse>, response: Response<SearchHospitalResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { hospitalResponse ->
                            displayHospitals(hospitalResponse)
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


    private fun displayHospitals(response: SearchHospitalResponse) {
        val hospital = Hospital(
            id = response.id,
            name = response.name,
            address = response.address,
            imageUrl = response.imageUrl,
            telephoneNumber = response.telephoneNumber,
            location = response.location,
            categories = response.categories
        )
        adapter = HospitalAdapter(listOf(hospital))  // List 형식으로 병원 추가
        recyclerView.adapter = adapter
    }
}
