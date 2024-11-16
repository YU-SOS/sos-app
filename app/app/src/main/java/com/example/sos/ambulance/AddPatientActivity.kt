package com.example.sos.ambulance

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.HospitalRes
import com.example.sos.Page
import com.example.sos.PatientReq
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.retrofit.ReceptionRequest
import com.example.sos.retrofit.ReceptionResponse
import com.example.sos.token.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime

class AddPatientActivity : AppCompatActivity() {
    private lateinit var authService: AuthService
    private lateinit var tokenManager: TokenManager
    private lateinit var hospitalSpinner: Spinner
    private var selectedHospital: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_patient)

        tokenManager = TokenManager(this)
        authService = RetrofitClientInstance.getApiService(tokenManager)

        // UI 요소 초기화
        hospitalSpinner = findViewById(R.id.spinner_hospitals)
        val inputName = findViewById<EditText>(R.id.input_name)
        val inputAge = findViewById<EditText>(R.id.input_age)
        val inputPhoneNumber = findViewById<EditText>(R.id.input_phoneNumber)
        val inputMedication = findViewById<EditText>(R.id.input_medication)
        val inputReference = findViewById<EditText>(R.id.input_reference)
        val radioGroupGender = findViewById<RadioGroup>(R.id.radio_group_gender)
        val saveButton = findViewById<Button>(R.id.btn_save)

        // 병원 목록 가져오기
        fetchHospitalList()

        saveButton.setOnClickListener {
            val name = inputName.text.toString()
            val age = inputAge.text.toString().toIntOrNull() ?: 0
            val phoneNumber = inputPhoneNumber.text.toString()
            val medication = inputMedication.text.toString()
            val reference = inputReference.text.toString()
            val gender = radioGroupGender.checkedRadioButtonId == R.id.radio_female
            val paramedicId = tokenManager.getSelectedParamedicId() ?: ""

            val patientReq = PatientReq(
                name = name,
                age = age,
                phoneNumber = phoneNumber,
                medication = medication,
                reference = reference,
                gender = gender
            )

            selectedHospital?.let { hospitalName ->
                createReception(patientReq, hospitalName, paramedicId)
            } ?: run {
                Toast.makeText(this, "병원을 선택해 주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchHospitalList() {
        val jwtToken = tokenManager.getAccessToken()
        if (jwtToken != null) {
            authService.getHospitalList("Bearer $jwtToken", categories = null, page = 0).enqueue(object : Callback<Page<HospitalRes>> {
                override fun onResponse(call: Call<Page<HospitalRes>>, response: Response<Page<HospitalRes>>) {
                    if (response.isSuccessful) {
                        response.body()?.content?.let { hospitalList ->
                            val hospitalNames = hospitalList.map { it.name }
                            setupSpinner(hospitalNames)
                        } ?: run {
                            Toast.makeText(this@AddPatientActivity, "No data available", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@AddPatientActivity, "Failed to retrieve hospitals", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Page<HospitalRes>>, t: Throwable) {
                    Toast.makeText(this@AddPatientActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "No token found. Please log in again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSpinner(hospitalNames: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, hospitalNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        hospitalSpinner.adapter = adapter

        hospitalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedHospital = hospitalNames[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedHospital = null
            }
        }
    }

    private fun createReception(patientReq: PatientReq, hospitalName: String, paramedicId: String) {
        val jwtToken = tokenManager.getAccessToken()
        if (jwtToken != null) {
            val receptionRequest = ReceptionRequest(
                patient = patientReq,
                hospitalName = hospitalName,
                startTime = LocalDateTime.now(),
                paramedicId = paramedicId
            )

            authService.addReception("Bearer $jwtToken", receptionRequest).enqueue(object : Callback<ReceptionResponse> {
                override fun onResponse(call: Call<ReceptionResponse>, response: Response<ReceptionResponse>) {
                    if (response.isSuccessful && response.body()?.status == 200) {
                        response.body()?.data?.let { receptionId ->
                            tokenManager.saveReceptionId(receptionId) // 접수 고유 번호 저장
                            Toast.makeText(this@AddPatientActivity, "접수 생성 완료", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        Toast.makeText(this@AddPatientActivity, "접수 생성 실패: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ReceptionResponse>, t: Throwable) {
                    Toast.makeText(this@AddPatientActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "토큰 오류. 다시 로그인 해주세요.", Toast.LENGTH_SHORT).show()
        }
    }
}
