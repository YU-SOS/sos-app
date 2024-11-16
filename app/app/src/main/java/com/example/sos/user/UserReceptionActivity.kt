package com.example.sos.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.sos.GuestSearchReceptionActivity
import com.example.sos.LogoutManager
import com.example.sos.R
import com.example.sos.SelectLoginActivity
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.ReceptionInfoResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.template.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserReceptionActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var apiService: AuthService
    private lateinit var logoutManager: LogoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_reception)

        tokenManager = TokenManager(this)
        logoutManager = LogoutManager(this, tokenManager)
        apiService = RetrofitClientInstance.getApiService(tokenManager)

        // BottomNavigationView 설정
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_reception -> true // 현재 화면이므로 아무 동작도 하지 않음
                R.id.nav_map -> {
                    val intent = Intent(this, UserMapActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
        val userLogoutButton: Button = findViewById(R.id.logout_button)
        userLogoutButton.setOnClickListener {

            logoutManager.logout()

            val intent = Intent(this, SelectLoginActivity::class.java)
            startActivity(intent)
            finish()
        }

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
                            Glide.with(this@UserReceptionActivity)
                                .load(hospital.imageUrl)
                                .into(hospitalImage)

                            // 병원 정보 레이아웃 표시
                            hospitalInfoLayout.visibility = View.VISIBLE
                        }
                    } else {
                        Log.e("Reception", "Failed to fetch hospital info: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ReceptionInfoResponse>, t: Throwable) {
                    Log.e("Reception", "Error: ${t.message}")
                }
            })
        }
    }
}
