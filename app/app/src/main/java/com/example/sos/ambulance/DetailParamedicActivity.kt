package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.ParamedicDeleteResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
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
        setContentView(R.layout.activity_detail_paramedic)

        Log.d("DetailParamedicActivity", "onCreate started")

        try {
            authService = RetrofitClientInstance.getApiService(TokenManager(this))
            tokenManager = TokenManager(this)

            paramedicId = intent.getStringExtra("paramedicId") ?: throw IllegalArgumentException("paramedicId is missing")
            ambulanceId = intent.getStringExtra("ambulanceId") ?: throw IllegalArgumentException("ambulanceId is missing")

            Log.d("DetailParamedicActivity", "Paramedic ID: $paramedicId, Ambulance ID: $ambulanceId")

            val paramedicName = intent.getStringExtra("paramedicName") ?: "이름 없음"
            val paramedicPhone = intent.getStringExtra("paramedicPhone") ?: "전화번호 없음"

            findViewById<TextView>(R.id.paramedic_name).text = paramedicName
            findViewById<TextView>(R.id.paramedic_phone).text = paramedicPhone

            findViewById<Button>(R.id.btn_paramedic_delete).setOnClickListener {
                deleteParamedic()
            }

            findViewById<Button>(R.id.btn_paramedic_modify).setOnClickListener {
                val intent = Intent(this, ModifyParamedicActivity::class.java).apply {
                    putExtra("paramedicId", paramedicId)
                    putExtra("paramedicName", paramedicName)
                    putExtra("paramedicPhone", paramedicPhone)
                    putExtra("ambulanceId", ambulanceId) // 전달
                }
                startActivity(intent)
            }

            Log.d("DetailParamedicActivity", "Activity created successfully")
        } catch (e: Exception) {
            Log.e("DetailParamedicActivity", "Initialization failed: ${e.message}")
            showToast("필수 데이터가 누락되었습니다. 앱을 다시 실행해주세요.")
            finish()
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
                override fun onResponse(call: Call<ParamedicDeleteResponse>, response: Response<ParamedicDeleteResponse>) {
                    if (response.isSuccessful && response.body()?.status == 200) {
                        showToast("삭제 성공")
                        finish()
                    } else {
                        showToast("삭제 실패: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ParamedicDeleteResponse>, t: Throwable) {
                    showToast("삭제 오류: ${t.message}")
                }
            })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
