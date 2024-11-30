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
import com.example.sos.retrofit.ParamedicModifyRequest
import com.example.sos.retrofit.ParamedicModifyResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ModifyParamedicActivity : AppCompatActivity() {

    private lateinit var authService: AuthService
    private lateinit var tokenManager: TokenManager
    private lateinit var paramedicId: String
    private lateinit var ambulanceId: String
    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paramedic_modify)

        val toolbar = findViewById<Toolbar>(R.id.include_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "구급대원 수정"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        authService = RetrofitClientInstance.getApiService(TokenManager(this))
        tokenManager = TokenManager(this)

        paramedicId = intent.getStringExtra("paramedicId") ?: run {
            showToast("구급대원 ID가 없습니다.")
            finish()
            return
        }
        ambulanceId = intent.getStringExtra("ambulanceId") ?: run {
            showToast("구급대 ID가 없습니다.")
            finish()
            return
        }

        nameEditText = findViewById(R.id.edit_text_name)
        phoneEditText = findViewById(R.id.edit_text_phone)

        // 초기 값 설정 (이전 액티비티에서 가져옴)
        nameEditText.setText(intent.getStringExtra("paramedicName"))
        phoneEditText.setText(intent.getStringExtra("paramedicPhone"))

        findViewById<Button>(R.id.btn_modify).setOnClickListener {
            modifyParamedic()
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

    private fun modifyParamedic() {
        val name = nameEditText.text.toString().trim()
        val phoneNumber = phoneEditText.text.toString().trim()

        if (name.isBlank()) {
            showToast("이름을 올바르게 입력해주세요.")
            return
        }
        if (phoneNumber.isBlank()) {
            showToast("전화번호를 올바르게 입력해주세요.")
            return
        }

        val jwtToken = tokenManager.getAccessToken()
        if (jwtToken.isNullOrEmpty()) {
            showToast("토큰 오류: 로그인이 필요합니다.")
            return
        }

        val updatedParamedic = ParamedicModifyRequest(
            name = name,
            phoneNumber = phoneNumber
        )
        authService.modifyParamedic("Bearer $jwtToken", ambulanceId, paramedicId, updatedParamedic)
            .enqueue(object : Callback<ParamedicModifyResponse> {
                override fun onResponse(
                    call: Call<ParamedicModifyResponse>,
                    response: Response<ParamedicModifyResponse>
                ) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse != null && apiResponse.status == 200) {
                            Toast.makeText(this@ModifyParamedicActivity, "구급대원 수정에 성공했습니다.", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            showToast("수정 실패에 실패했습니다. ${apiResponse?.message ?: "알 수 없는 오류"}")
                        }
                    }
                }

                override fun onFailure(call: Call<ParamedicModifyResponse>, t: Throwable) {
                    showToast("수정 오류: ${t.message}")
                }
            })
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
