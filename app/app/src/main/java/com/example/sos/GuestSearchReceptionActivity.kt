package com.example.sos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.ReceptionGuestResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.template.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GuestSearchReceptionActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var apiService: AuthService
    private lateinit var logoutManager: LogoutManager
    private lateinit var shareButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guest_search_reception)

        tokenManager = TokenManager(this)
        apiService = RetrofitClientInstance.getApiService(tokenManager)
        logoutManager = LogoutManager(this, tokenManager)

        // 뷰 초기화
        val receptionIdInput: EditText = findViewById(R.id.reception_id_input)
        val getHospitalInfoButton: Button = findViewById(R.id.get_hospital_info_button)
        val hospitalNameTextView: TextView = findViewById(R.id.hospital_name_textview)
        val hospitalLocationTextView: TextView = findViewById(R.id.hospital_location_textview)
        val hospitalPhoneTextView: TextView = findViewById(R.id.hospital_phone_textview)
        val paramedicInfoTextView: TextView = findViewById(R.id.paramedic_info_textview)
        val hospitalImage: ImageView = findViewById(R.id.hospital_image)
        val hospitalInfoLayout: LinearLayout = findViewById(R.id.hospital_info_layout)
        shareButton = findViewById(R.id.kakao_share_button)

        // 병원 정보 조회 버튼 클릭 리스너
        getHospitalInfoButton.setOnClickListener {
            val receptionId = receptionIdInput.text.toString()
            if (receptionId.isNotEmpty()) {
                getReceptionInfo(
                    receptionId,
                    hospitalNameTextView,
                    hospitalLocationTextView,
                    hospitalPhoneTextView,
                    paramedicInfoTextView,
                    hospitalImage,
                    hospitalInfoLayout
                )
            }
        }

        // 카카오톡 공유 버튼 클릭 리스너
        shareButton.setOnClickListener {
            shareViaKakao()
        }

    }

    private fun getReceptionInfo(
        receptionId: String,
        hospitalNameTextView: TextView,
        hospitalLocationTextView: TextView,
        hospitalPhoneTextView: TextView,
        paramedicInfoTextView: TextView,
        hospitalImage: ImageView,
        hospitalInfoLayout: LinearLayout
    ) {

        apiService.getReceptionGuest(receptionId).enqueue(object : Callback<ReceptionGuestResponse> {
            override fun onResponse(
                call: Call<ReceptionGuestResponse>,
                response: Response<ReceptionGuestResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { receptionGuestResponse ->
                        val hospital = receptionGuestResponse.data.hospital
                        val ambulance = receptionGuestResponse.data.ambulance

                        // 병원 정보 설정
                        hospitalNameTextView.text = "병원 이름: ${hospital.name}"
                        hospitalLocationTextView.text = "병원 주소: ${hospital.address}"
                        hospitalPhoneTextView.text = "병원 위치: (${hospital.location.latitude}, ${hospital.location.longitude})"

                        // 병원 이미지 로드 (Glide 사용)
                        Glide.with(this@GuestSearchReceptionActivity)
                            .load(hospital.imageUrl)
                            .into(hospitalImage)

                        // 구급차 정보 출력 (필요 시 추가)
                        paramedicInfoTextView.text = "구급차 이름: ${ambulance.name}, 주소: ${ambulance.address}"

                        // 병원 정보 레이아웃 표시
                        hospitalInfoLayout.visibility = View.VISIBLE

                        Toast.makeText(this@GuestSearchReceptionActivity, "조회 성공", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@GuestSearchReceptionActivity, "조회 실패 ${response.message()}", Toast.LENGTH_SHORT).show()
                    Log.e("Reception", "Failed to fetch reception guest info: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ReceptionGuestResponse>, t: Throwable) {
                Toast.makeText(this@GuestSearchReceptionActivity, "조회 실패 ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("Reception", "Error: ${t.message}")
            }
        })
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
                Button(
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

    // 인텐트 업데이트 (앱이 백그라운드에 있다가 딥링크로 열리는 경우 처리)
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        intent?.data?.let { uri ->
            val receptionId = uri.getQueryParameter("receptionId")
            if (receptionId != null) {
                val intent = Intent(this, GuestSearchReceptionActivity::class.java)
                intent.putExtra("receptionId", receptionId)
                startActivity(intent)
                finish()
            }
        }
    }
}
