package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.ParamedicDeleteResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailParamedicActivity : AppCompatActivity() {

    private lateinit var authService: AuthService
    private lateinit var tokenManager: TokenManager
    private lateinit var paramedicId: String
    private lateinit var ambulanceId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paramedic_detail)

        val toolbar = findViewById<Toolbar>(R.id.include_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "구급대원 정보"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        try {
            tokenManager = TokenManager(this)
            authService = RetrofitClientInstance.getApiService(tokenManager)

            paramedicId = intent.getStringExtra("paramedicId")
                ?: throw IllegalArgumentException("paramedicId is missing")
            ambulanceId = intent.getStringExtra("ambulanceId")
                ?: throw IllegalArgumentException("ambulanceId is missing")

            Log.d(
                "DetailParamedicActivity",
                "Paramedic ID: $paramedicId, Ambulance ID: $ambulanceId"
            )

            val paramedicName = intent.getStringExtra("paramedicName") ?: "이름 없음"
            val paramedicPhone = intent.getStringExtra("paramedicPhone") ?: "전화번호 없음"

            findViewById<TextView>(R.id.paramedic_name).text = paramedicName
            findViewById<TextView>(R.id.paramedic_phone).text = paramedicPhone

            // 삭제 버튼
            findViewById<Button>(R.id.btn_paramedic_delete).setOnClickListener {
                deleteParamedic()
            }

            // 수정 버튼
            findViewById<Button>(R.id.btn_paramedic_modify).setOnClickListener {
                val intent = Intent(this, ModifyParamedicActivity::class.java).apply {
                    putExtra("paramedicId", paramedicId)
                    putExtra("paramedicName", paramedicName)
                    putExtra("paramedicPhone", paramedicPhone)
                    putExtra("ambulanceId", ambulanceId)
                }
                startActivity(intent)
                finish()
            }

            // 선탑 구급대원 설정 버튼
            findViewById<Button>(R.id.btn_head_paramedic).setOnClickListener {
                setHeadParamedic()
            }

            Log.d("DetailParamedicActivity", "Activity created successfully")
        } catch (e: Exception) {
            Log.e("DetailParamedicActivity", "Initialization failed: ${e.message}")
            showToast("필수 데이터가 누락되었습니다.")
            finish()
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

    // 선탑 구급대원 설정
    private fun setHeadParamedic() {
        try {
            tokenManager.saveSelectedParamedicId(paramedicId)
            showToast("선탑 구급대원이 설정되었습니다.")
            Log.d("DetailParamedicActivity", "Head Paramedic ID saved: $paramedicId")
            finish()
        } catch (e: Exception) {
            showToast("선탑 구급대원 설정에 실패했습니다.")
            Log.e("DetailParamedicActivity", "Failed to save Head Paramedic ID: ${e.message}")
        }
    }

    private fun deleteParamedic() {
        Log.d("DetailParamedicActivity", "Deleting paramedic with ID: $paramedicId")
        val jwtToken = tokenManager.getAccessToken()
        if (jwtToken.isNullOrEmpty()) {
            showToast("토큰 오류: 로그인이 필요합니다.")
            return
        }

        authService.deleteParamedic("Bearer $jwtToken", ambulanceId, paramedicId)
            .enqueue(object : Callback<ParamedicDeleteResponse> {
                override fun onResponse(
                    call: Call<ParamedicDeleteResponse>,
                    response: Response<ParamedicDeleteResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == 200) {
                        showToast("구급대원 삭제를 성공하였습니다.")
                        finish()
                    } else {
                        showToast("구급대원 삭제를 실패했습니다.\n 다시 시도해 주세요.")
                    }
                }

                override fun onFailure(call: Call<ParamedicDeleteResponse>, t: Throwable) {
                    showToast("삭제 오류")
                }
            })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
