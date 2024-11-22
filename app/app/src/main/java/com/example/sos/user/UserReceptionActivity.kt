package com.example.sos.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.sos.LogoutManager
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.ReceptionInfoResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

class UserReceptionActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var apiService: AuthService
    private lateinit var logoutManager: LogoutManager
    private lateinit var shareButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_reception)

        tokenManager = TokenManager(this)
        apiService = RetrofitClientInstance.getApiService(tokenManager)
        logoutManager = LogoutManager(this, tokenManager)

        // BottomNavigationView 설정
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.selectedItemId = R.id.nav_reception

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_reception -> {
                    // 이미 현재 화면이므로 아무 작업도 하지 않음
                    true
                }
                R.id.nav_map -> {
                    // '지도 화면'으로 이동
                    val intent = Intent(this, UserMapActivity::class.java)
                    startActivity(intent)
                    finish() // 현재 액티비티 종료
                    true
                }
                else -> false
            }
        }

        // 로그아웃 버튼 클릭 리스너
        val userLogoutButton: ImageButton = findViewById(R.id.logout_button)
        userLogoutButton.setOnClickListener {
            logoutManager.logout()
        }

        // 뷰 초기화
        val receptionIdInput: EditText = findViewById(R.id.reception_id_input)
        val getHospitalInfoButton: Button = findViewById(R.id.get_hospital_info_button)
        val backButton: Button = findViewById(R.id.back_button) // 뒤로 가기 버튼 초기화
        val kakaoShareButton: ImageButton = findViewById(R.id.kakao_share_button)
        val textView2: TextView = findViewById(R.id.textView2)
        val textView3: TextView = findViewById(R.id.textView3)
        val textView4: TextView = findViewById(R.id.textView4)
        shareButton = findViewById(R.id.kakao_share_button)

        // 조회 버튼 클릭 리스너
        getHospitalInfoButton.setOnClickListener {
            val receptionId = receptionIdInput.text.toString()
            if (receptionId.isNotEmpty()) {
                try {
                    UUID.fromString(receptionId)

                    // 텍스트 숨기기
                    textView2.visibility = View.GONE
                    textView3.visibility = View.GONE
                    textView4.visibility = View.GONE

                    // 카카오톡 공유 버튼 표시
                    kakaoShareButton.visibility = View.VISIBLE

                    showHospitalDetails() // 새로운 화면처럼 전환
                    getReceptionInfo(receptionId)
                } catch (e: IllegalArgumentException) {
                    Toast.makeText(this, "올바른 접수번호 형식이 아닙니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 뒤로 가기 버튼 클릭 리스너
        backButton.setOnClickListener {
            hideHospitalDetails() // 기존 입력 화면으로 돌아가기

            // 텍스트 다시 표시
            textView2.visibility = View.VISIBLE
            textView3.visibility = View.VISIBLE
            textView4.visibility = View.VISIBLE

            // 카카오톡 공유 버튼 숨기기
            kakaoShareButton.visibility = View.GONE
        }

        // 카카오톡 공유 버튼 클릭 리스너
        shareButton.setOnClickListener {
            shareViaKakao()
        }
    }

    private fun showHospitalDetails() {
        val receptionIdInput: EditText = findViewById(R.id.reception_id_input)
        val getHospitalInfoButton: Button = findViewById(R.id.get_hospital_info_button)
        val hospitalInfoLayout: LinearLayout = findViewById(R.id.hospital_info_layout)
        val backButton: Button = findViewById(R.id.back_button)

        // 기존 입력 UI 숨기기
        receptionIdInput.visibility = View.GONE
        getHospitalInfoButton.visibility = View.GONE

        // 접수 내역 UI 표시
        hospitalInfoLayout.visibility = View.VISIBLE
        backButton.visibility = View.VISIBLE
    }

    private fun hideHospitalDetails() {
        val receptionIdInput: EditText = findViewById(R.id.reception_id_input)
        val getHospitalInfoButton: Button = findViewById(R.id.get_hospital_info_button)
        val hospitalInfoLayout: LinearLayout = findViewById(R.id.hospital_info_layout)
        val backButton: Button = findViewById(R.id.back_button)

        // 기존 입력 UI 표시
        receptionIdInput.visibility = View.VISIBLE
        getHospitalInfoButton.visibility = View.VISIBLE

        // 접수 내역 UI 숨기기
        hospitalInfoLayout.visibility = View.GONE
        backButton.visibility = View.GONE
    }

    private fun getReceptionInfo(receptionId: String) {
        val accessToken = tokenManager.getAccessToken()
        accessToken?.let {
            apiService.getReceptionInfo("Bearer $accessToken", receptionId).enqueue(object :
                Callback<ReceptionInfoResponse> {
                override fun onResponse(
                    call: Call<ReceptionInfoResponse>,
                    response: Response<ReceptionInfoResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { receptionInfoResponse ->
                            val hospital = receptionInfoResponse.data.hospital

                            // 병원 정보 설정
                            val hospitalNameTextView: TextView = findViewById(R.id.hospital_name_textview)
                            val hospitalLocationTextView: TextView = findViewById(R.id.hospital_location_textview)
                            val hospitalPhoneTextView: TextView = findViewById(R.id.hospital_phone_textview)

                            hospitalNameTextView.text = hospital.name
                            hospitalLocationTextView.text = "주소: ${hospital.address}"
                            hospitalPhoneTextView.text = "전화번호: ${hospital.telephoneNumber}"

                            // 병원 이미지 설정
                            val hospitalImage: ImageView = findViewById(R.id.hospital_image)
                            Glide.with(this@UserReceptionActivity)
                                .load(hospital.imageUrl)
                                .into(hospitalImage)

                            Toast.makeText(this@UserReceptionActivity, "조회 성공", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@UserReceptionActivity, "조회 실패: ${response.message()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ReceptionInfoResponse>, t: Throwable) {
                    Toast.makeText(this@UserReceptionActivity, "조회 실패: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
    // 카카오톡 공유 기능 추가 (앱 링크)
    private fun shareViaKakao() {
        val defaultFeed = FeedTemplate(
            content = Content(
                title = "앱에서 병원 정보 확인하기",
                description = "이 병원 정보를 확인해보세요!",
                link = Link(
                    androidExecutionParams = mapOf("receptionId" to "12345"), // 앱 설치된 경우 실행할 파라미터
                    mobileWebUrl = "https://play.google.com/store/apps/details?id=com.example.sos" // 앱 미설치 시 스토어로 이동
                )
            ),
            buttons = listOf(
                com.kakao.sdk.template.model.Button(
                    "앱으로 보기",
                    Link(
                        androidExecutionParams = mapOf("receptionId" to "12345"), // 앱 설치된 경우 실행할 인텐트 파라미터
                        mobileWebUrl = "https://play.google.com/store/apps/details?id=com.example.sos" // 앱 미설치 시 스토어로 이동
                    )
                )
            )
        )

        // 카카오톡 공유 요청
        ShareClient.instance.shareDefault(this, defaultFeed) { sharingResult, error ->
            if (error != null) {
                Log.e("KakaoShare", "카카오톡 공유 실패: ${error.message}")
            } else if (sharingResult != null) {
                Log.d("KakaoShare", "카카오톡 공유 성공: ${sharingResult.intent}")
                startActivity(sharingResult.intent)
            }
        }
    }
}
