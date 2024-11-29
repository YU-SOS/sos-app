package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.ParamedicsResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoadParamedicActivity : AppCompatActivity() {

    private lateinit var authService: AuthService
    private lateinit var tokenManager: TokenManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ParamedicsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paramedic_load)

        initializeUI()
        val ambulanceId = tokenManager.getTokenId()

        if (!ambulanceId.isNullOrEmpty()) {
            fetchParamedics(ambulanceId)
        } else {
            showToast("구급대 ID를 찾을 수 없습니다. 다시 로그인하세요.")
        }
    }

    private fun initializeUI() {
        tokenManager = TokenManager(this)
        authService = RetrofitClientInstance.getApiService(tokenManager)

        recyclerView = findViewById(R.id.recycler_paramedics)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ParamedicsAdapter { selectedParamedic ->
            val ambulanceId = tokenManager.getTokenId()
            val intent = Intent(this, DetailParamedicActivity::class.java).apply {
                putExtra("paramedicId", selectedParamedic.id)
                putExtra("paramedicName", selectedParamedic.name)
                putExtra("paramedicPhone", selectedParamedic.phoneNumber)
                putExtra("ambulanceId", ambulanceId) // 구급대 ID 추가
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_request -> {
                    val intent = Intent(this, AddPatientActivity::class.java)
                    startActivity(intent) // 새 화면을 열지만 현재 화면은 닫지 않음
                    true
                }
                R.id.nav_info -> {
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchParamedics(ambulanceId: String) {
        val jwtToken = tokenManager.getAccessToken()
        if (jwtToken.isNullOrEmpty()) {
            showToast("토큰 오류: 다시 로그인하세요.")
            return
        }

        authService.getParamedics("Bearer $jwtToken", ambulanceId)
            .enqueue(object : Callback<ParamedicsResponse> {
                override fun onResponse(call: Call<ParamedicsResponse>, response: Response<ParamedicsResponse>) {
                    if (response.isSuccessful) {
                        val paramedicsList = response.body()?.data
                        if (!paramedicsList.isNullOrEmpty()) {
                            adapter.updateParamedics(paramedicsList)
                        } else {
                            showToast("구급대원이 없습니다.")
                        }
                    } else {
                        showToast("구급대원 정보를 불러오지 못했습니다.")
                    }
                }

                override fun onFailure(call: Call<ParamedicsResponse>, t: Throwable) {
                    showToast("구급대원 정보를 불러오는 중 오류 발생: ${t.message}")
                }
            })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
