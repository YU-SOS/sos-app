package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.ParamedicModifyRequest
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AmbulanceParamedicModifyActivity : AppCompatActivity() {
    private lateinit var tokenManager: TokenManager
    private lateinit var authService: AuthService
    private lateinit var modifyNameText: EditText
    private lateinit var modifyTelephoneText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ambulance_paramedic_modify)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tokenManager = TokenManager(this)
        authService = RetrofitClientInstance.getApiService(tokenManager)

        modifyNameText = findViewById(R.id.modify_name_text)
        modifyTelephoneText = findViewById(R.id.modify_telephone_text)

        // Intent로부터 데이터 받아오기
        val paramedicId = intent.getStringExtra("id")
        val paramedicName = intent.getStringExtra("name")
        val paramedicPhoneNumber = intent.getStringExtra("phoneNumber")

        // EditText에 데이터 설정
        modifyNameText.setText(paramedicName)
        modifyTelephoneText.setText(paramedicPhoneNumber)

        val modifyButton = findViewById<Button>(R.id.modify_button)
        modifyButton.setOnClickListener {
            // 수정 완료 버튼 클릭 시 PUT 요청
            if (paramedicId != null) {
                updateParamedic(paramedicId)
            }
        }
    }

    private fun updateParamedic(memberId: String) {
        val authorization = "Bearer ${tokenManager.getAccessToken()}"
        val ambulanceId = tokenManager.getTokenId() // ambulanceId 가져오기
        val name = modifyNameText.text.toString()
        val phoneNumber = modifyTelephoneText.text.toString()

        // 데이터 클래스를 사용하여 요청 본문 생성
        val paramedicUpdateRequest = ParamedicModifyRequest(name, phoneNumber)

        authService.modifyParamedic(authorization, ambulanceId, memberId, paramedicUpdateRequest).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AmbulanceParamedicModifyActivity, "수정 완료", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AmbulanceParamedicModifyActivity, "수정 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@AmbulanceParamedicModifyActivity, "통신 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
