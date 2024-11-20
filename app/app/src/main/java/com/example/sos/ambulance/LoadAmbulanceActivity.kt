package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.sos.R
import com.example.sos.res.AmbulanceRes
import com.example.sos.res.ParamedicsRes
import com.example.sos.retrofit.AmbulanceResponse
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.ParamedicsResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoadAmbulanceActivity : AppCompatActivity() {

    private lateinit var authService: AuthService
    private lateinit var tokenManager: TokenManager
    private lateinit var paramedicSpinner: Spinner
    private lateinit var ambulanceImage: ImageView
    private lateinit var ambulanceName: TextView
    private lateinit var ambulanceAddress: TextView
    private lateinit var ambulanceTelephone: TextView
    private var selectedParamedic: ParamedicsRes? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_ambulance)

        initializeUI()
        val ambulanceId = tokenManager.getTokenId()

        if (!ambulanceId.isNullOrEmpty()) {
            fetchAmbulanceInfo(ambulanceId)
            fetchParamedics(ambulanceId)
        } else {
            showToast("구급대 ID를 찾을 수 없습니다. 다시 로그인하세요.")
        }
    }

    private fun initializeUI() {
        tokenManager = TokenManager(this)
        authService = RetrofitClientInstance.getApiService(tokenManager)

        paramedicSpinner = findViewById(R.id.spinner_paramedics)
        ambulanceImage = findViewById(R.id.ambulance_image)
        ambulanceName = findViewById(R.id.ambulance_name)
        ambulanceAddress = findViewById(R.id.ambulance_address)
        ambulanceTelephone = findViewById(R.id.ambulance_telephone)

        findViewById<Button>(R.id.btn_load_paramedic).setOnClickListener {
            startActivity(Intent(this, LoadParamedicActivity::class.java))
        }

        findViewById<Button>(R.id.btn_add_paramedic).setOnClickListener {
            val ambulanceId = tokenManager.getTokenId()
            startActivity(Intent(this, AddParamedicActivity::class.java).apply {
                putExtra("ambulanceId", ambulanceId)
            })
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
                            showToast("앰뷸런스 데이터가 없습니다.")
                        }
                    } else {
                        showToast("앰뷸런스 정보를 불러오지 못했습니다.")
                    }
                }

                override fun onFailure(call: Call<AmbulanceResponse>, t: Throwable) {
                    showToast("앰뷸런스 정보를 불러오는 중 오류 발생: ${t.message}")
                }
            })
    }

    private fun displayAmbulanceInfo(ambulance: AmbulanceRes) {
        ambulanceName.text = ambulance.name
        ambulanceAddress.text = ambulance.address
        ambulanceTelephone.text = ambulance.telephoneNumber

        // Firebase Storage 이미지 로드
        Glide.with(this)
            .load(ambulance.imageUrl)
            .placeholder(R.drawable.image2) // 로딩 중 기본 이미지
            .error(R.drawable.image)       // 에러 시 기본 이미지
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
                            val paramedicNames = paramedicsList.map { it.name }
                            setupParamedicSpinner(paramedicsList, paramedicNames)
                        } else {
                            setupParamedicSpinner(emptyList(), listOf("구급대원이 없습니다."))
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

    private fun setupParamedicSpinner(paramedicsList: List<ParamedicsRes>, paramedicNames: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paramedicNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        paramedicSpinner.adapter = adapter

        paramedicSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedParamedic = paramedicsList[position]
                selectedParamedic?.let {
                    tokenManager.saveSelectedParamedicId(it.id)
                    showToast("선택된 구급대원: ${it.name}")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedParamedic = null
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
