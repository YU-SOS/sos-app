package com.example.sos.ambulance

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.sos.R
import com.example.sos.res.ReceptionRes
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.LoadReceptionResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.retrofit.RetryReceptionResponse
import com.example.sos.token.TokenManager
import com.kakao.sdk.user.model.Gender
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewPatientActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var authService: AuthService
    private lateinit var receptionId: String

    private lateinit var statusTextView: TextView
    private lateinit var hospitalNameTextView: TextView
    private lateinit var selectHospitalButton: Button
    private lateinit var retryButton: Button
    private lateinit var refreshButton: Button
    private lateinit var selectHospitalTextView: TextView
    private lateinit var patientGenderTextView: TextView

    private var selectedHospitalId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_patient)

        tokenManager = TokenManager(this)
        authService = RetrofitClientInstance.getApiService(tokenManager)

        // Intent에서 receptionId 가져오기
        receptionId = intent.getStringExtra("receptionId") ?: run {
            showToast("Reception ID를 찾을 수 없습니다.")
            finish()
            return
        }

        initializeUI()
        fetchReceptionInfo()
    }

    private fun initializeUI() {
        val toolbar = findViewById<Toolbar>(R.id.tool_bar2)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // UI 요소 초기화
        statusTextView = findViewById(R.id.reception_status)
        hospitalNameTextView = findViewById(R.id.hospital_name)
        selectHospitalButton = findViewById(R.id.select_hospital_button)
        retryButton = findViewById(R.id.retry_button)
        refreshButton = findViewById(R.id.refresh_button)
        selectHospitalTextView = findViewById(R.id.text_select_hospital)
        patientGenderTextView = findViewById(R.id.patient_gender)

        // 초기 버튼 및 텍스트 숨기기
        selectHospitalButton.visibility = View.GONE
        retryButton.visibility = View.GONE
        selectHospitalTextView.visibility = View.GONE

        refreshButton.setOnClickListener { fetchReceptionInfo() }
        selectHospitalButton.setOnClickListener { selectHospital() }
        retryButton.setOnClickListener { retryReception() }
    }

    private fun fetchReceptionInfo() {
        val jwtToken = tokenManager.getAccessToken()
        if (jwtToken.isNullOrEmpty()) {
            showToast("토큰 오류: 로그인이 필요합니다.")
            return
        }

        authService.getReceptionDetails("Bearer $jwtToken", receptionId)
            .enqueue(object : Callback<LoadReceptionResponse> {
                override fun onResponse(
                    call: Call<LoadReceptionResponse>,
                    response: Response<LoadReceptionResponse>
                ) {
                    if (response.isSuccessful) {
                        val receptionData = response.body()?.data
                        if (receptionData != null) {
                            updateUI(receptionData)
                        } else {
                            showToast("데이터를 불러올 수 없습니다.")
                        }
                    } else {
                        showToast("요청 실패: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<LoadReceptionResponse>, t: Throwable) {
                    showToast("네트워크 오류: ${t.message}")
                }
            })
    }

    private fun updateUI(reception: ReceptionRes) {
        // 병원 정보 업데이트
        hospitalNameTextView.text = reception.hospital.name
        findViewById<TextView>(R.id.hospital_address).text = reception.hospital.address
        findViewById<TextView>(R.id.hospital_phone).text = reception.hospital.telephoneNumber

        // 환자 정보 업데이트
        findViewById<TextView>(R.id.patient_name).text = reception.patient.name
        findViewById<TextView>(R.id.patient_age).text = reception.patient.age.toString()
        findViewById<TextView>(R.id.patient_phone).text = reception.patient.phoneNumber
        patientGenderTextView.text = parseGender(reception.patient.gender)

        // 접수 상태에 따른 UI 업데이트
        when (reception.receptionStatus) {
            "PENDING" -> {
                statusTextView.text = "요청 대기 중"
                statusTextView.setTextColor(Color.BLUE)
                refreshButton.visibility = View.VISIBLE
                selectHospitalButton.visibility = View.GONE
                retryButton.visibility = View.GONE
                selectHospitalTextView.visibility = View.GONE
            }
            "MOVE" -> {
                statusTextView.text = "이동 중"
                statusTextView.setTextColor(Color.GREEN)
                refreshButton.visibility = View.VISIBLE
                selectHospitalButton.visibility = View.GONE
                retryButton.visibility = View.GONE
                selectHospitalTextView.visibility = View.GONE
            }
            "ARRIVAL" -> {
                statusTextView.text = "도착 완료"
                statusTextView.setTextColor(Color.GREEN)
                refreshButton.visibility = View.VISIBLE
                selectHospitalButton.visibility = View.GONE
                retryButton.visibility = View.GONE
                selectHospitalTextView.visibility = View.GONE
                finish() // 알아서 조절 ㄱㄱ
            }
            "REJECTED" -> {
                statusTextView.text = "요청 거절"
                statusTextView.setTextColor(Color.RED)
                refreshButton.visibility = View.GONE
                selectHospitalTextView.visibility = View.VISIBLE
                selectHospitalButton.visibility = View.VISIBLE
                retryButton.visibility = View.VISIBLE
            }
            else -> {
                statusTextView.text = "상태 알 수 없음"
                statusTextView.setTextColor(Color.GRAY)
                refreshButton.visibility = View.GONE
                selectHospitalButton.visibility = View.GONE
                retryButton.visibility = View.GONE
                selectHospitalTextView.visibility = View.GONE
            }
        }
    }


    private fun parseGender(gender: Gender?): String {
        return when (gender) {
            Gender.MALE -> "남자"
            Gender.FEMALE -> "여자"
            else -> "알 수 없음"
        }
    }

    private fun selectHospital() {
        val intent = Intent(this, LoadHospitalActivity::class.java)
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            selectedHospitalId = data?.getStringExtra("hospitalId")
            val selectedHospitalName = data?.getStringExtra("hospitalName")
            hospitalNameTextView.text = selectedHospitalName ?: "병원 선택 안 됨"
        }
    }

    private fun retryReception() {
        if (selectedHospitalId.isNullOrEmpty()) {
            showToast("병원을 먼저 선택해주세요.")
            return
        }

        val jwtToken = tokenManager.getAccessToken()
        if (jwtToken.isNullOrEmpty()) {
            showToast("토큰 오류: 로그인이 필요합니다.")
            return
        }

        val requestBody = mapOf("hospitalId" to selectedHospitalId!!)

        authService.retryReception(
            "Bearer $jwtToken",
            receptionId,
            requestBody
        ).enqueue(object : Callback<RetryReceptionResponse> {
            override fun onResponse(
                call: Call<RetryReceptionResponse>,
                response: Response<RetryReceptionResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val newReceptionId = response.body()!!.data

                    // ViewPatientActivity를 새로운 receptionId로 재실행
                    val intent = Intent(this@ViewPatientActivity, ViewPatientActivity::class.java).apply {
                        putExtra("receptionId", newReceptionId)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    showToast("재접수 요청 실패: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<RetryReceptionResponse>, t: Throwable) {
                showToast("네트워크 오류: ${t.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
