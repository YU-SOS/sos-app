package com.example.sos.ambulance

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.ParamedicModifyRequest
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
        paramedicId = intent.getStringExtra("paramedicId") ?: ""
        ambulanceId = tokenManager.getTokenId()

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
        if (jwtToken != null) {
            val updatedParamedic = ParamedicModifyRequest(
                name = nameEditText.text.toString(),
                phoneNumber = phoneEditText.text.toString()
            )

            authService.modifyParamedic("Bearer $jwtToken", ambulanceId, paramedicId, updatedParamedic)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful && response.code() == 200) {
                            Toast.makeText(this@ModifyParamedicActivity, "수정 성공", Toast.LENGTH_SHORT).show()
                            finish() // 구급대원 페이지로 돌아가기
                        } else {
                            showToast("수정 실패")
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        showToast("수정 오류: ${t.message}")
                    }
                })
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
