package com.example.sos.ambulance

import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
    private lateinit var hospitalAddressTextView: TextView
    private lateinit var hospitalPhoneTextView: TextView
    private lateinit var patientNameTextView: TextView
    private lateinit var patientAgeTextView: TextView
    private lateinit var patientPhoneTextView: TextView
    private lateinit var patientGenderTextView: TextView
    private lateinit var refreshButton: Button
    private lateinit var retryButton: Button

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
        val toolbar = findViewById<Toolbar>(R.id.tool_bar2)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // UI 요소 초기화
        statusTextView = findViewById(R.id.reception_status)
        hospitalNameTextView = findViewById(R.id.hospital_name)
        hospitalAddressTextView = findViewById(R.id.hospital_address)
        hospitalPhoneTextView = findViewById(R.id.hospital_phone)
        patientNameTextView = findViewById(R.id.patient_name)
        patientAgeTextView = findViewById(R.id.patient_age)
        patientPhoneTextView = findViewById(R.id.patient_phone)
        patientGenderTextView = findViewById(R.id.patient_gender)
        refreshButton = findViewById(R.id.refresh_button)
        retryButton = findViewById(R.id.retry_button)

        // 초기 재요청 버튼 숨기기
        retryButton.visibility = View.GONE

        refreshButton.setOnClickListener { fetchReceptionInfo() }
        retryButton.setOnClickListener { retryRequest() }
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
                        Log.d("ViewPatientActivity", "NO null Reception data: $receptionData")
                        if (receptionData != null) {
                            Log.d("ViewPatientActivity", "Reception data: $receptionData")
                            updateUI(receptionData)
                        } else {
                            showToast("데이터를 불러올 수 없습니다.")
                            Log.e("ViewPatientActivity", "Reception data is null.")
                        }
                    } else {
                        showToast("요청 실패: ${response.message()}")
                        Log.e("ViewPatientActivity", "Response error: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<LoadReceptionResponse>, t: Throwable) {
                    showToast("네트워크 오류: ${t.message}")
                    Log.e("ViewPatientActivity", "Network error: ${t.message}")
                    Log.e("ViewPatientActivity", "Network error: ", t)
                }
            })
    }

    private fun updateUI(reception: ReceptionRes) {
        try {
            // 병원 정보
            hospitalNameTextView.text = reception.hospital.name
            hospitalAddressTextView.text = reception.hospital.address
            hospitalPhoneTextView.text = reception.hospital.telephoneNumber

            // 환자 정보
            patientNameTextView.text = reception.patient.name

            // 나이 처리
            patientAgeTextView.text = try {
                reception.patient.age.toString()
            } catch (e: NumberFormatException) {
                Log.e("ViewPatientActivity", "NumberFormatException 발생: ${e.message}")
                "알 수 없음" // 기본값 설정
            }

            patientPhoneTextView.text = reception.patient.phoneNumber

            // 성별
            patientGenderTextView.text = parseGender(reception.patient.gender)

            // 접수 상태
            when (reception.receptionStatus) {
                "PENDING" -> updateStatus("요청 대기", Color.BLUE, false)
                "MOVE" -> updateStatus("이동 중", Color.GREEN, false)
                "ARRIVAL" -> updateStatus("도착 완료", Color.GREEN, false)
                "REJECTED" -> updateStatus("요청 거절", Color.RED, true)
                else -> updateStatus("알 수 없음", Color.GRAY, false)
            }
        } catch (e: Exception) {
            Log.e("ViewPatientActivity", "UI 업데이트 중 오류 발생: ${e.message}")
            showToast("UI 업데이트 중 오류 발생.")
        }
    }

    private fun updateStatus(status: String, color: Int, showRetry: Boolean) {
        statusTextView.text = status
        statusTextView.setTextColor(color)
        retryButton.visibility = if (showRetry) View.VISIBLE else View.GONE
    }

    private fun parseGender(gender: Gender?): String {
        return when (gender) {
            Gender.MALE -> "남자"
            Gender.FEMALE -> "여자"
            else -> "알 수 없음"
        }
    }

    private fun retryRequest() {
        showToast("재요청을 처리합니다.")
        // 재요청 로직을 추가하세요.
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
