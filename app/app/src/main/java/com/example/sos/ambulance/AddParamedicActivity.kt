package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.MemberRequest
import com.example.sos.retrofit.MemberResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddParamedicActivity : AppCompatActivity() {

    private lateinit var authService: AuthService
    private lateinit var tokenManager: TokenManager
    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_paramedic)

        // 툴바 가져오기
        val toolbar = findViewById<Toolbar>(R.id.include_toolbar)
        if (toolbar == null) {
            showToast("툴바를 찾을 수 없습니다. 레이아웃 ID를 확인하세요.")
            return
        }
        setSupportActionBar(toolbar)


        // 툴바를 액션바로 설정
        setSupportActionBar(toolbar)

        // 툴바 제목 설정
        supportActionBar?.title = "구급대원 추가"

        // 뒤로가기 버튼 활성화
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 뒤로가기 버튼 클릭 이벤트
        toolbar.setNavigationOnClickListener {
            finish() // 현재 액티비티 종료
        }

        tokenManager = TokenManager(this)
        authService = RetrofitClientInstance.getApiService(tokenManager)

        nameEditText = findViewById(R.id.edit_text_name)
        phoneEditText = findViewById(R.id.edit_text_phone)

        findViewById<Button>(R.id.btn_add).setOnClickListener {
            addParamedic()
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_request -> {
                    val intent = Intent(this, AddPatientActivity::class.java)
                    startActivity(intent)
                    finish()
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

    private fun addParamedic() {
        val name = nameEditText.text.toString().trim()
        val phoneNumber = phoneEditText.text.toString().trim()

        // 공백 체크
        if (name.isBlank() || phoneNumber.isBlank()) {
            showToast("이름과 전화번호를 올바르게 입력해주세요.")
            return
        }

        val jwtToken = tokenManager.getAccessToken()
        val ambulanceId = try {
            tokenManager.getTokenId() // TokenManager를 통해 구급대 ID 가져오기
        } catch (e: Exception) {
            showToast("구급대 ID를 가져오는 데 실패했습니다: ${e.message}")
            return
        }

        if (jwtToken != null) {
            val paramedic = MemberRequest(
                name = name,
                phoneNumber = phoneNumber
            )

            authService.addAmbulanceMember(
                "Bearer $jwtToken",
                ambulanceId,
                paramedic // BODY로 전송할 구급대원 정보
            ).enqueue(object : Callback<MemberResponse> {
                override fun onResponse(call: Call<MemberResponse>, response: Response<MemberResponse>) {
                    if (response.isSuccessful) {
                        showToast("구급대원 추가 성공")
                        finish() // 구급대원 페이지로 돌아가기
                    } else {
                        showToast("구급대원 추가 실패: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<MemberResponse>, t: Throwable) {
                    showToast("추가 오류: ${t.message}")
                }
            })
        } else {
            showToast("토큰을 찾을 수 없습니다. 다시 로그인 해주세요.")
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
