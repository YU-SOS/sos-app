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
import com.example.sos.retrofit.ReceptionInfoResponse
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
        val emergencyRoomStatusTextView: TextView = findViewById(R.id.emergency_room_status_textview)
        val receptionIdTextView: TextView = findViewById(R.id.reception_id_textview)
        val patientInfoTextView: TextView = findViewById(R.id.patient_info_textview)
        val paramedicInfoTextView: TextView = findViewById(R.id.paramedic_info_textview)
        val hospitalCommentTextView: TextView = findViewById(R.id.hospital_comment_textview)
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
                    emergencyRoomStatusTextView,
                    receptionIdTextView,
                    patientInfoTextView,
                    paramedicInfoTextView,
                    hospitalCommentTextView,
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
        emergencyRoomStatusTextView: TextView,
        receptionIdTextView: TextView,
        patientInfoTextView: TextView,
        paramedicInfoTextView: TextView,
        hospitalCommentTextView: TextView,
        hospitalImage: ImageView,
        hospitalInfoLayout: LinearLayout
    ) {
        val accessToken = tokenManager.getAccessToken()

        accessToken?.let {
            apiService.getReceptionInfo("Bearer $it", receptionId).enqueue(object :
                Callback<ReceptionInfoResponse> {
                override fun onResponse(
                    call: Call<ReceptionInfoResponse>,
                    response: Response<ReceptionInfoResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { receptionInfoResponse ->
                            val reception = receptionInfoResponse.data
                            val hospital = reception.hospital
                            val patient = reception.patient
                            val paramedic = reception.paramedicRes
                            val comments = reception.comments

                            // 병원 정보 설정
                            hospitalNameTextView.text = hospital.name
                            hospitalLocationTextView.text = "주소: ${hospital.address}"
                            hospitalPhoneTextView.text = "전화번호: ${hospital.telephoneNumber}"
                            emergencyRoomStatusTextView.text = "응급실 상태: ${hospital.emergencyRoomStatus}"
                            receptionIdTextView.text = "접수번호: ${reception.id}"

                            // 환자 정보 설정
                            patientInfoTextView.text = "환자 정보: ${patient.name}, ${patient.age}세, ${patient.gender}"

                            // 구급대원 정보 설정
                            paramedicInfoTextView.text = "구급대원 정보: ${paramedic.name}, 전화번호: ${paramedic.phoneNumber}"

                            // 코멘트 설정
                            val comment = if (comments.isNotEmpty()) comments[0].content else "No comments"
                            hospitalCommentTextView.text = "코멘트: $comment"

                            // 병원 이미지 로드 (Glide 사용 예)
                            Glide.with(this@GuestSearchReceptionActivity)
                                .load(hospital.imageUrl)
                                .into(hospitalImage)

                            // 병원 정보 레이아웃 표시
                            hospitalInfoLayout.visibility = View.VISIBLE
                            Toast.makeText(this@GuestSearchReceptionActivity, "조회 성공", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@GuestSearchReceptionActivity, "조회 실패 ${response.message()}", Toast.LENGTH_SHORT).show()
                        Log.e("Reception", "Failed to fetch hospital info: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ReceptionInfoResponse>, t: Throwable) {
                    Toast.makeText(this@GuestSearchReceptionActivity, "조회 실패 ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Reception", "Error: ${t.message}")
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
