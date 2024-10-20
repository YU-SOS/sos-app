package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.LoadReceptionResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.retrofit.ReceptionResponse
import com.example.sos.token.TokenManager
import com.kakao.sdk.user.model.Gender
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AmbulanceLoadReceptionActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var apiService: AuthService

    // TextView 및 버튼 변수 선언
    private lateinit var patientNameTextView: TextView
    private lateinit var patientPhoneTextView: TextView
    private lateinit var patientGenderTextView: TextView
    private lateinit var addCommentButton: Button // 버튼 변수 추가

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ambulance_load_reception)

        // TextView 및 버튼 초기화
        patientNameTextView = findViewById(R.id.patientNameTextView)
        patientPhoneTextView = findViewById(R.id.patientPhoneTextView)
        patientGenderTextView = findViewById(R.id.patientGenderTextView)
        addCommentButton = findViewById(R.id.add_comment_button) // 버튼 초기화

        tokenManager = TokenManager(this)
        apiService = RetrofitClientInstance.getApiService(tokenManager)

        // 접수 번호를 가져오는 메서드 호출
        loadReceptionDetails()

        // 버튼 클릭 리스너 설정
        addCommentButton.setOnClickListener {
            // AmbulanceAddCommentActivity를 시작
            val intent = Intent(this, AmbulanceAddCommentActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadReceptionDetails() {
        val receptionId = tokenManager.getReceptionId() // SharedPreferences에서 접수 고유 번호 가져오기
        val accessToken = tokenManager.getAccessToken() // 액세스 토큰 가져오기

        receptionId?.let {
            accessToken?.let { token ->
                apiService.getReceptionDetails("Bearer $token", it).enqueue(object : Callback<LoadReceptionResponse> {
                    override fun onResponse(call: Call<LoadReceptionResponse>, response: Response<LoadReceptionResponse>) {
                        if (response.isSuccessful) {
                            response.body()?.let { loadReceptionResponse ->
                                if (loadReceptionResponse.status == 200) {
                                    // data 필드에 접근하여 필요한 정보를 UI에 업데이트
                                    val receptionData = loadReceptionResponse.data

                                    // 환자 정보 표시
                                    patientNameTextView.text = "이름: ${receptionData.patient.name}"
                                    patientPhoneTextView.text = "전화번호: ${receptionData.patient.phoneNumber}"
                                    patientGenderTextView.text = "성별: ${when (receptionData.patient.gender) {
                                        Gender.MALE -> "남자"
                                        Gender.FEMALE -> "여자"
                                        else -> "알 수 없음"
                                    }}"
                                } else {
                                    Toast.makeText(this@AmbulanceLoadReceptionActivity, "접수 정보 로드 실패: ${loadReceptionResponse.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(this@AmbulanceLoadReceptionActivity, "응답 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<LoadReceptionResponse>, t: Throwable) {
                        Toast.makeText(this@AmbulanceLoadReceptionActivity, "오류 발생: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        } ?: run {
            Toast.makeText(this, "접수 번호가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
