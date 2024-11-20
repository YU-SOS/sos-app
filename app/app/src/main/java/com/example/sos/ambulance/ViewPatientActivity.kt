package com.example.sos.ambulance

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.sos.R
import com.example.sos.res.ReceptionRes
import com.kakao.sdk.user.model.Gender
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.retrofit.LoadReceptionResponse
import com.example.sos.token.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewPatientActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var authService: AuthService
    private lateinit var receptionId: String

    private lateinit var statusTextView: TextView
    private lateinit var hospitalNameTextView: TextView
    private lateinit var hospitalAddressTextView: TextView
    private lateinit var hospitalPhoneTextView: TextView
    private lateinit var patientNameTextView: TextView
    private lateinit var patientAgeTextView: TextView
    private lateinit var patientPhoneTextView: TextView
    private lateinit var patientGenderTextView: TextView
    private lateinit var refreshButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_patient)

        tokenManager = TokenManager(this)
        authService = RetrofitClientInstance.getApiService(tokenManager)

        receptionId = tokenManager.getReceptionId() ?: run {
            showToast("Reception ID를 찾을 수 없습니다.")
            finish()
            return
        }

        initializeUI()
        fetchReceptionInfo()
    }

    private fun initializeUI() {
        // 툴바 설정
        val toolbar = findViewById<Toolbar>(R.id.tool_bar2)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // 텍스트뷰와 버튼 초기화
        statusTextView = findViewById(R.id.reception_status)
        hospitalNameTextView = findViewById(R.id.hospital_name)
        hospitalAddressTextView = findViewById(R.id.hospital_address)
        hospitalPhoneTextView = findViewById(R.id.hospital_phone)
        patientNameTextView = findViewById(R.id.patient_name)
        patientAgeTextView = findViewById(R.id.patient_age)
        patientPhoneTextView = findViewById(R.id.patient_phone)
        patientGenderTextView = findViewById(R.id.patient_gender)
        refreshButton = findViewById(R.id.refresh_button)

        // 새로고침 버튼 클릭 이벤트
        refreshButton.setOnClickListener {
            fetchReceptionInfo()
        }
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
        statusTextView.text = reception.receptionStatus ?: "상태 없음"
        hospitalNameTextView.text = reception.hospital.name
        hospitalAddressTextView.text = reception.hospital.address
        hospitalPhoneTextView.text = reception.hospital.telephoneNumber

        // 환자 정보 업데이트
        patientNameTextView.text = reception.patient.name
        patientAgeTextView.text = reception.patient.age.toString()
        patientPhoneTextView.text = reception.patient.phoneNumber

        // 성별 처리
        patientGenderTextView.text = when (reception.patient.gender) {
            Gender.MALE -> "남자"
            Gender.FEMALE -> "여자"
            else -> "알 수 없음"
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
