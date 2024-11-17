package com.example.sos.ambulance

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.res.HospitalRes
import com.example.sos.PatientReq
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.HospitalLoadResponse
import com.example.sos.retrofit.ReceptionRequest
import com.example.sos.retrofit.ReceptionResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime

class AddPatientActivity : AppCompatActivity() {

    private lateinit var authService: AuthService
    private lateinit var tokenManager: TokenManager
    private lateinit var hospitalSpinner: Spinner
    private var selectedHospital: HospitalRes? = null // 선택된 병원 정보를 저장

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
            val name = inputName.text.toString().trim()
            val age = inputAge.text.toString().toIntOrNull() ?: 0
            val phoneNumber = inputPhoneNumber.text.toString().trim()
            val medication = inputMedication.text.toString().trim()
            val reference = inputReference.text.toString().trim()
            val gender = radioGroupGender.checkedRadioButtonId == R.id.radio_female
            val paramedicId = tokenManager.getSelectedParamedicId() ?: ""

            if (name.isEmpty() || phoneNumber.isEmpty()) {
                Toast.makeText(this, "환자 이름과 전화번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val patientReq = PatientReq(
                name = name,
                age = age,
                phoneNumber = phoneNumber,
                medication = medication,
                reference = reference,
                gender = gender
            )

            selectedHospital?.let { hospital ->
                createReception(patientReq, hospital.name, paramedicId)
            } ?: run {
                Toast.makeText(this, "병원을 선택해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchHospitalList() {
        val jwtToken = tokenManager.getAccessToken()
        if (jwtToken != null) {
            authService.getHospitalList("Bearer $jwtToken", categories = null, page = 0)
                .enqueue(object : Callback<HospitalLoadResponse<HospitalRes>> {
                    override fun onResponse(
                        call: Call<HospitalLoadResponse<HospitalRes>>,
                        response: Response<HospitalLoadResponse<HospitalRes>>
                    ) {
                        if (response.isSuccessful) {
                            val hospitalList = response.body()?.data?.content
                            if (!hospitalList.isNullOrEmpty()) {
                                setupSpinner(hospitalList)
                            } else {
                                Toast.makeText(this@AddPatientActivity, "병원이 없습니다.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@AddPatientActivity, "병원 조회 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<HospitalLoadResponse<HospitalRes>>, t: Throwable) {
                        Toast.makeText(this@AddPatientActivity, "오류 발생: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            Toast.makeText(this, "토큰이 없습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSpinner(hospitalList: List<HospitalRes>) {
        val hospitalNames = hospitalList.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, hospitalNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        hospitalSpinner.adapter = adapter

        hospitalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedHospital = hospitalList[position]
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

            authService.addReception("Bearer $jwtToken", receptionRequest)
                .enqueue(object : Callback<ReceptionResponse> {
                    override fun onResponse(call: Call<ReceptionResponse>, response: Response<ReceptionResponse>) {
                        if (response.isSuccessful && response.body()?.status == 200) {
                            response.body()?.data?.let { receptionId ->
                                tokenManager.saveReceptionId(receptionId)
                                Toast.makeText(this@AddPatientActivity, "접수 생성 완료", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        } else {
                            Toast.makeText(
                                this@AddPatientActivity,
                                "접수 생성 실패: ${response.body()?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<ReceptionResponse>, t: Throwable) {
                        Toast.makeText(this@AddPatientActivity, "오류 발생: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            Toast.makeText(this, "토큰 오류. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
        }
    }
}
