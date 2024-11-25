package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.sos.LogoutManager
import com.example.sos.PatientReq
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.ReceptionRequest
import com.example.sos.retrofit.ReceptionResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kakao.sdk.user.model.Gender
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AddPatientActivity : AppCompatActivity() {

    private val LOAD_HOSPITAL_REQUEST_CODE = 1000
    private lateinit var authService: AuthService
    private lateinit var tokenManager: TokenManager
    private var selectedHospitalName: String? = null
    private lateinit var logoutManager: LogoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_patient)

        // 툴바 설정
        val toolbar = findViewById<Toolbar>(R.id.include_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "환자 접수 등록"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        authService = RetrofitClientInstance.getApiService(TokenManager(this))
        tokenManager = TokenManager(this)
        logoutManager = LogoutManager(this, tokenManager)

        // UI 요소 초기화
        val inputName = findViewById<EditText>(R.id.input_name)
        val inputAge = findViewById<EditText>(R.id.input_age)
        val inputPhoneNumber = findViewById<EditText>(R.id.input_phoneNumber)
        val inputSymptom = findViewById<EditText>(R.id.input_symptom)
        val inputMedication = findViewById<EditText>(R.id.input_medication)
        val inputReference = findViewById<EditText>(R.id.input_reference)
        val radioGroupGender = findViewById<RadioGroup>(R.id.radio_group_gender)
        val selectHospitalButton = findViewById<Button>(R.id.button)
        val hospitalTextView = findViewById<TextView>(R.id.textView4)
        val saveButton = findViewById<Button>(R.id.btn_save)

        // 병원 선택 버튼 클릭
        selectHospitalButton.setOnClickListener {
            val intent = Intent(this, LoadHospitalActivity::class.java)
            startActivityForResult(intent, LOAD_HOSPITAL_REQUEST_CODE)
        }

        // 저장 버튼 클릭
        saveButton.setOnClickListener {
            val name = inputName.text.toString().trim()
            val age = inputAge.text.toString().toIntOrNull() ?: 0
            val phoneNumber = inputPhoneNumber.text.toString().trim()
            val symptom = inputSymptom.text.toString().trim()
            val medication = inputMedication.text.toString().trim()
            val reference = inputReference.text.toString().trim()
            val gender = when (radioGroupGender.checkedRadioButtonId) {
                R.id.radio_male -> Gender.MALE
                R.id.radio_female -> Gender.FEMALE
                else -> null
            }

            if (name.isEmpty() || phoneNumber.isEmpty() || gender == null || selectedHospitalName.isNullOrEmpty()) {
                Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val patientReq = PatientReq(
                name = name,
                age = age,
                phoneNumber = phoneNumber,
                symptom = symptom,
                medication = medication,
                reference = reference,
                gender = gender
            )

            createReception(patientReq, selectedHospitalName!!)
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_request -> {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOAD_HOSPITAL_REQUEST_CODE && resultCode == RESULT_OK) {
            selectedHospitalName = data?.getStringExtra("selectedHospitalName")
            findViewById<TextView>(R.id.textView4).text = selectedHospitalName ?: "병원을 선택해주세요."
        }
    }

    private fun createReception(patientReq: PatientReq, hospitalName: String) {
        val jwtToken = tokenManager.getAccessToken()
        if (jwtToken.isNullOrEmpty()) {
            Toast.makeText(this, "토큰 오류. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val paramedicId = tokenManager.getSelectedParamedicId()
        if (paramedicId.isNullOrEmpty()) {
            // 선탑 구급대원이 선택되지 않은 경우 메시지 표시
            Toast.makeText(this, "선탑 구급대원을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val receptionRequest = ReceptionRequest(
            patient = patientReq,
            hospitalName = hospitalName,
            startTime = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
            paramedicId = paramedicId
        )

        authService.addReception("Bearer $jwtToken", receptionRequest)
            .enqueue(object : Callback<ReceptionResponse> {
                override fun onResponse(call: Call<ReceptionResponse>, response: Response<ReceptionResponse>) {
                    if (response.isSuccessful && response.body()?.status == 201) {
                        Toast.makeText(this@AddPatientActivity, "접수 요청이 완료되었습니다.", Toast.LENGTH_SHORT).show()

                        // ViewPatientActivity로 이동
                        val receptionId = response.body()?.data?.toString()
                        val intent = Intent(this@AddPatientActivity, ViewPatientActivity::class.java).apply {
                            putExtra("receptionId", receptionId)
                        }
                        startActivity(intent)
                        finish() // AddPatientActivity 종료
                    } else {
                        Toast.makeText(
                            this@AddPatientActivity,
                            "접수 요청 실패: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ReceptionResponse>, t: Throwable) {
                    Toast.makeText(this@AddPatientActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
