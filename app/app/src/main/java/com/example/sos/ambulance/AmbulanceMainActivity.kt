package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sos.LogoutManager
import com.example.sos.R
import com.example.sos.res.AmbulanceRes
import com.example.sos.retrofit.AmbulanceResponse
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.ParamedicsResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AmbulanceMainActivity : AppCompatActivity() {

    private lateinit var authService: AuthService
    private lateinit var tokenManager: TokenManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ParamedicsAdapter
    private lateinit var ambulanceImage: ImageView
    private lateinit var ambulanceName: TextView
    private lateinit var ambulanceAddress: TextView
    private lateinit var ambulancePhone: TextView
    private lateinit var logoutManager: LogoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ambulance_main)

        initializeUI()

        logoutManager = LogoutManager(this, tokenManager)

        val ambulanceId = tokenManager.getTokenId()
        if (!ambulanceId.isNullOrEmpty()) {
            fetchAmbulanceInfo(ambulanceId)
            fetchParamedics(ambulanceId)
        } else {
            showToast("구급대 ID를 찾을 수 없습니다. 다시 로그인하세요.")
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_info

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_request -> {
                    val intent = Intent(this, AddPatientActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_info -> {
                    true
                }
                else -> false
            }
        }

        // 구급대원 추가
        val addParamedicButton = findViewById<Button>(R.id.add_paramedic_button)
        addParamedicButton.setOnClickListener {
            val intent = Intent(this, AddParamedicActivity::class.java).apply {
                putExtra("ambulanceId", ambulanceId)
            }
            startActivity(intent)
        }

        // 로그아웃 버튼
        val userLogoutButton: ImageButton = findViewById(R.id.logout_button)
        userLogoutButton.setOnClickListener {
            logoutManager.logout()
        }
    }


    private fun initializeUI() {
        tokenManager = TokenManager(this)
        authService = RetrofitClientInstance.getApiService(tokenManager)

        recyclerView = findViewById(R.id.recycler_paramedics)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ParamedicsAdapter { selectedParamedic ->
            val ambulanceId = tokenManager.getTokenId()
            if (ambulanceId != null) {
                val intent = Intent(this, DetailParamedicActivity::class.java).apply {
                    putExtra("paramedicId", selectedParamedic.id)
                    putExtra("paramedicName", selectedParamedic.name)
                    putExtra("paramedicPhone", selectedParamedic.phoneNumber)
                    putExtra("ambulanceId", ambulanceId)
                }
                startActivity(intent)
            } else {
                showToast("구급대 ID를 찾을 수 없습니다.")
            }
        }
        recyclerView.adapter = adapter

        ambulanceImage = findViewById(R.id.ambulance_image)
        ambulanceName = findViewById(R.id.ambulance_name)
        ambulanceAddress = findViewById(R.id.ambulance_address)
        ambulancePhone = findViewById(R.id.ambulance_phone)
    }

    override fun onResume() {
        super.onResume()
        // 액티비티가 다시 활성화될 때 데이터를 갱신
        val ambulanceId = tokenManager.getTokenId()
        if (!ambulanceId.isNullOrEmpty()) {
            fetchParamedics(ambulanceId)
        }
    }

    private fun fetchAmbulanceInfo(ambulanceId: String) {
        val jwtToken = tokenManager.getAccessToken()
        if (jwtToken.isNullOrEmpty()) {
            showToast("토큰 오류: 다시 로그인하세요.")
            return
        }

        authService.getAmbulanceDetails("Bearer $jwtToken", ambulanceId)
            .enqueue(object : Callback<AmbulanceResponse> {
                override fun onResponse(call: Call<AmbulanceResponse>, response: Response<AmbulanceResponse>) {
                    if (response.isSuccessful) {
                        val ambulanceData = response.body()?.data
                        if (ambulanceData != null) {
                            displayAmbulanceInfo(ambulanceData)
                        } else {
                            showToast("구급차 데이터를 찾을 수 없습니다.")
                        }
                    } else {
                        showToast("구급차 정보를 불러오지 못했습니다.")
                    }
                }

                override fun onFailure(call: Call<AmbulanceResponse>, t: Throwable) {
                    showToast("구급차 정보를 불러오는 중 오류 발생: ${t.message}")
                }
            })
    }

    private fun displayAmbulanceInfo(ambulance: AmbulanceRes) {
        ambulanceName.text = ambulance.name
        ambulanceAddress.text = ambulance.address
        ambulancePhone.text = ambulance.telephoneNumber

        // 이미지 로드 (Glide 사용)
        Glide.with(this)
            .load(ambulance.imageUrl)
            .placeholder(R.drawable.ambulance_info) // 로딩 중 기본 이미지
            .error(R.drawable.sos_logo)       // 에러 시 기본 이미지
            .into(ambulanceImage)
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
