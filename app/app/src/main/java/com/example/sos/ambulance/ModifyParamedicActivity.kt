package com.example.sos.ambulance

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.ParamedicModifyRequest
import com.example.sos.retrofit.ParamedicModifyResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
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
        setContentView(R.layout.activity_modify_paramedic)

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
    }

    private fun modifyParamedic() {
        val jwtToken = tokenManager.getAccessToken()
        if (jwtToken.isNullOrEmpty()) {
            showToast("토큰 오류: 로그인이 필요합니다.")
            return
        }

        val updatedParamedic = ParamedicModifyRequest(
            name = nameEditText.text.toString(),
            phoneNumber = phoneEditText.text.toString()
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
                            Toast.makeText(this@ModifyParamedicActivity, "수정 성공", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            showToast("수정 실패: ${apiResponse?.message ?: "알 수 없는 오류"}")
                        }
                    } else {
                        showToast("수정 실패: ${response.message()}")
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
