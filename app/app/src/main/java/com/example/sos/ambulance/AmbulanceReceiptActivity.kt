package com.example.sos.ambulance

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.ParamedicsRes
import com.example.sos.PatientReq
import com.example.sos.R
import com.example.sos.retrofit.ReceptionRequest
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.retrofit.ReceptionResponse
import com.example.sos.token.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime

class AmbulanceReceiptActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var apiService: AuthService
    private lateinit var paramedicSpinner: Spinner
    private var paramedicId: String? = null // 선택된 구급대원 ID를 String으로 수정

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ambulance_receipt)

        tokenManager = TokenManager(this)
        apiService = RetrofitClientInstance.getApiService(tokenManager)

        // 병원 이름을 Intent에서 가져옴
        val hospitalName = intent.getStringExtra("HOSPITAL_NAME") ?: "병원 정보 없음"
        findViewById<TextView>(R.id.hospitalNameTextView).text = hospitalName

        // 구급대원 스피너 설정
        paramedicSpinner = findViewById(R.id.paramedicSpinner)
        loadParamedics()

        val sendButton = findViewById<Button>(R.id.submitReceptionButton)
        sendButton.setOnClickListener {
            // 접수 요청 전송
            sendReception(hospitalName)
        }
    }

    // 구급대원 목록 불러오기
    private fun loadParamedics() {
        val ambulanceId = tokenManager.getTokenId() // 토큰에서 ambulanceId를 가져옴
        val accessToken = tokenManager.getAccessToken()

        accessToken?.let {
            apiService.getParamedics("Bearer $it", ambulanceId).enqueue(object : Callback<List<ParamedicsRes>> { // ParamedicsRes로 수정
                override fun onResponse(call: Call<List<ParamedicsRes>>, response: Response<List<ParamedicsRes>>) {
                    if (response.isSuccessful) {
                        response.body()?.let { paramedics ->
                            setupParamedicSpinner(paramedics)
                        }
                    } else {
                        Toast.makeText(this@AmbulanceReceiptActivity, "구급대원 목록 불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<ParamedicsRes>>, t: Throwable) {
                    Toast.makeText(this@AmbulanceReceiptActivity, "오류 발생: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // Spinner에 구급대원 목록 설정
    private fun setupParamedicSpinner(paramedics: List<ParamedicsRes>) { // ParamedicsRes로 수정
        val paramedicNames = paramedics.map { it.name } // 구급대원 이름 목록
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paramedicNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        paramedicSpinner.adapter = adapter

        paramedicSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // 선택된 구급대원의 ID를 String으로 저장
                paramedicId = paramedics[position].id
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 아무것도 선택되지 않았을 때
            }
        }
    }

    // 접수 요청 전송
    private fun sendReception(hospitalName: String) {
        val name = findViewById<EditText>(R.id.patientNameEditText).text.toString()
        val age = findViewById<EditText>(R.id.patientAgeEditText).text.toString().toInt()
        val phoneNumber = findViewById<EditText>(R.id.patientPhoneNumberEditText).text.toString()
        val medication = findViewById<EditText>(R.id.patientMedicationEditText).text.toString()
        val reference = findViewById<EditText>(R.id.patientReferenceEditText).text.toString()

        val gender = when (findViewById<RadioGroup>(R.id.genderRadioGroup).checkedRadioButtonId) {
            R.id.genderMale -> false // 남자
            R.id.genderFemale -> true // 여자
            else -> false
        }

        val startTime = LocalDateTime.now()

        // 선택된 구급대원 ID가 있어야 함
        paramedicId?.let { paramedicId ->
            val receptionRequest = ReceptionRequest(
                patient = PatientReq(name, age, phoneNumber, medication, reference, gender),
                hospitalName = hospitalName,
                startTime = startTime,
                paramedicId = paramedicId // String으로 그대로 사용
            )

            val accessToken = tokenManager.getAccessToken()
            accessToken?.let {
                apiService.addReception("Bearer $it", receptionRequest).enqueue(object : Callback<ReceptionResponse> {
                    override fun onResponse(call: Call<ReceptionResponse>, response: Response<ReceptionResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@AmbulanceReceiptActivity, "접수 성공", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@AmbulanceReceiptActivity, "접수 실패: ${response.message()}", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<ReceptionResponse>, t: Throwable) {
                        Toast.makeText(this@AmbulanceReceiptActivity, "오류 발생: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
            }
        } ?: run {
            Toast.makeText(this, "구급대원을 선택해주세요", Toast.LENGTH_LONG).show()
        }
    }
}
