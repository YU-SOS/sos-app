package com.example.sos.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.MapView
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextBuilder

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
                    true
                }
                R.id.nav_map -> {
                    val intent = Intent(this, UserMapActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

        val userLogoutButton: ImageButton = findViewById(R.id.logout_button)
        userLogoutButton.setOnClickListener {
            logoutManager.logout()
        }

        val receptionIdInput: EditText = findViewById(R.id.reception_id_input)
        val getHospitalInfoButton: Button = findViewById(R.id.get_hospital_info_button)
        val backButton: Button = findViewById(R.id.back_button) // 뒤로 가기 버튼 초기화
        val kakaoShareButton: ImageButton = findViewById(R.id.kakao_share_button)
        val textView2: TextView = findViewById(R.id.textView2)
        val textView3: TextView = findViewById(R.id.textView3)
        val textView4: TextView = findViewById(R.id.textView4)

        shareButton = findViewById(R.id.kakao_share_button)

        // 조회 버튼
        getHospitalInfoButton.setOnClickListener {
            val receptionId = receptionIdInput.text.toString().trim() // 입력값의 공백 제거
            if (receptionId.isEmpty()) {
                Toast.makeText(this, "공백을 제외하고 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    getReceptionInfo(receptionId)
                } catch (e: IllegalArgumentException) {
                    Toast.makeText(this, "올바른 접수번호 형식이 아닙니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 뒤로 가기 버튼
        backButton.setOnClickListener {
            hideHospitalDetails()

            textView2.visibility = View.VISIBLE
            textView3.visibility = View.VISIBLE
            textView4.visibility = View.VISIBLE

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

    private fun setupMap(latitude: Double, longitude: Double, hospitalName: String) {
        val hospitalLocation = LatLng.from(latitude, longitude) // 병원 위치

        val mapView: MapView = findViewById(R.id.hospital_map_view)
        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                Log.d("UserReceptionActivity", "Map destroyed")
            }

            override fun onMapError(exception: Exception?) {
                Log.e("UserReceptionActivity", "Map error: ${exception?.message}")
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(map: KakaoMap) {
                val kakaoMap = map
                val labelManager = kakaoMap.labelManager

                val labelStyles = LabelStyles.from(
                    "hospitalStyle",
                    LabelStyle.from(R.drawable.hospital_point).setZoomLevel(8),
                    LabelStyle.from(R.drawable.hospital_point).setZoomLevel(11)
                        .setTextStyles(32, android.graphics.Color.BLACK, 1, android.graphics.Color.GRAY)
                )

                val labelText = LabelTextBuilder()
                    .setTexts(hospitalName)

                val labelOptions = LabelOptions.from(hospitalLocation)
                    .setStyles(labelStyles)
                    .setTexts(labelText)

                try {
                    labelManager?.layer?.addLabel(labelOptions)

                    // 카메라를 병원 위치로 이동
                    kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(hospitalLocation, 18))
                } catch (e: Exception) {
                    Log.e("UserReceptionActivity", "Error adding label: ${e.message}", e)
                }
            }
        })
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
                            val patient = receptionInfoResponse.data.patient
                            val paramedic = receptionInfoResponse.data.paramedic
                            val comments = receptionInfoResponse.data.comments

                            val hospitalNameTextView: TextView = findViewById(R.id.hospital_name_textview)
                            val hospitalLocationTextView: TextView = findViewById(R.id.hospital_location_textview)
                            val hospitalPhoneTextView: TextView = findViewById(R.id.hospital_phone_textview)
                            val patientInfo: TextView = findViewById(R.id.patient_info_textview)
                            val paramedicInfo: TextView = findViewById(R.id.paramedic_info_textview)
                            val hospitalComment: TextView = findViewById(R.id.hospital_comment_textview)

                            hospitalNameTextView.text = hospital.name
                            hospitalLocationTextView.text = "주소: ${hospital?.address}"
                            hospitalPhoneTextView.text = "전화번호: ${hospital?.telephoneNumber}"
                            patientInfo.text = "환자 정보: ${patient?.name}, ${patient?.age}, ${patient?.gender}, ${patient?.phoneNumber}, ${patient?.medication}"
                            paramedicInfo.text = "구급대원 정보: ${paramedic?.name}, ${paramedic?.phoneNumber}"

                            hospitalComment.text = if (!comments.isNullOrEmpty()) {
                                "코멘트:\n" + comments.joinToString(separator = "\n") { it.content }
                            } else {
                                "코멘트: 없음"
                            }

                            val latitude = hospital.location.latitude.toDouble()
                            val longitude = hospital.location.longitude.toDouble()
                            val hospitalName = hospital.name ?: "병원 이름 없음"

                            setupMap(latitude, longitude, hospitalName)

                            showHospitalDetails()

                            val textView2: TextView = findViewById(R.id.textView2)
                            val textView3: TextView = findViewById(R.id.textView3)
                            val textView4: TextView = findViewById(R.id.textView4)
                            val kakaoShareButton: ImageButton = findViewById(R.id.kakao_share_button)

                            textView2.visibility = View.GONE
                            textView3.visibility = View.GONE
                            textView4.visibility = View.GONE
                            kakaoShareButton.visibility = View.VISIBLE

                            Toast.makeText(this@UserReceptionActivity, "조회 성공", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@UserReceptionActivity, "없는 조회번호입니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ReceptionInfoResponse>, t: Throwable) {
                    Toast.makeText(this@UserReceptionActivity, "조회 실패: ${t.message}", Toast.LENGTH_LONG).show()
                    Log.e("UserReceptionActivity: ", "${t.message}")
                }
            })
        }
    }


    private fun shareViaKakao() {
        val receptionIdInput: EditText = findViewById(R.id.reception_id_input)
        val receptionId = receptionIdInput.text.toString().trim()

        if (receptionId.isEmpty()) {
            Toast.makeText(this, "접수번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "http://www.yu-sos.co.kr/reception/$receptionId/guest"

        val defaultFeed = FeedTemplate(
            content = Content(
                title = "웹에서 환자 정보 확인하기",
                description = "이 환자 정보를 확인해보세요!",
                link = Link(
                    webUrl = url,
                    mobileWebUrl = url
                )
            ),
            buttons = listOf(
                com.kakao.sdk.template.model.Button(
                    "환자 정보 확인하기",
                    Link(
                        webUrl = url,
                        mobileWebUrl = url
                    )
                )
            )
        )

        // 카카오톡 공유 요청
        ShareClient.instance.shareDefault(this, defaultFeed) { sharingResult, error ->
            if (error != null) {
                Log.e("KakaoShare", "카카오톡 공유 실패: ${error.message}")
                Toast.makeText(this, "카카오톡 공유 실패: ${error.message}", Toast.LENGTH_LONG).show()
            } else if (sharingResult != null) {
                Log.d("KakaoShare", "카카오톡 공유 성공")
                startActivity(sharingResult.intent)  // 공유 화면 열기
                Toast.makeText(this, "카카오톡 공유 화면이 열렸습니다. 공유를 완료하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
