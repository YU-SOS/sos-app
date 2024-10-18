package com.example.sos.ambulance

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.ParamedicDeleteResponse
import com.example.sos.retrofit.RetrofitClientInstance
import com.example.sos.token.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ParamedicDetailActivity : AppCompatActivity() {
    private lateinit var paramedicIdTextView: TextView
    private lateinit var paramedicNameTextView: TextView
    private lateinit var paramedicPhoneTextView: TextView
    private lateinit var tokenManager: TokenManager
    private lateinit var authService: AuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_paramedic_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // TextView 초기화
        paramedicIdTextView = findViewById(R.id.paramedic_id_text_view)
        paramedicNameTextView = findViewById(R.id.paramedic_name_text_view)
        paramedicPhoneTextView = findViewById(R.id.paramedic_phone_text_view)

        // TokenManager와 AuthService 초기화
        tokenManager = TokenManager(this)
        authService = RetrofitClientInstance.getApiService(tokenManager)

        // Intent로부터 넘어온 데이터 변수에 저장
        val paramedicId = intent.getStringExtra("id")
        val paramedicName = intent.getStringExtra("name")
        val paramedicPhoneNumber = intent.getStringExtra("phoneNumber")

        // TextView에 데이터 설정
        paramedicIdTextView.text = paramedicId
        paramedicNameTextView.text = paramedicName
        paramedicPhoneTextView.text = paramedicPhoneNumber

        // 수정 버튼 클릭 리스너 설정
        val paramedicModifyButton = findViewById<Button>(R.id.paramedic_modify_button)
        paramedicModifyButton.setOnClickListener {
            val intent = Intent(this, AmbulanceParamedicModifyActivity::class.java)
            intent.putExtra("id", paramedicId) // id 전달
            intent.putExtra("name", paramedicName) // 이름 전달
            intent.putExtra("phoneNumber", paramedicPhoneNumber) // 전화번호 전달
            startActivity(intent)
        }

        // 삭제 버튼 클릭 리스너 설정
        val paramedicDeleteButton = findViewById<Button>(R.id.paramedic_delete_button)
        paramedicDeleteButton.setOnClickListener {
            if (paramedicId != null) {
                deleteParamedic(paramedicId)
            }
        }
    }

    private fun deleteParamedic(paramedicId: String) {
        val authorization = "Bearer ${tokenManager.getAccessToken()}"
        val ambulanceId = tokenManager.getTokenId() // ambulanceId 가져오기

        authService.deleteParamedic(authorization, ambulanceId, paramedicId).enqueue(object : Callback<ParamedicDeleteResponse> {
            override fun onResponse(call: Call<ParamedicDeleteResponse>, response: Response<ParamedicDeleteResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        when (it.status) {
                            200 -> {
                                Toast.makeText(this@ParamedicDetailActivity, "삭제 성공", Toast.LENGTH_SHORT).show()
                                finish() // 활동 종료
                            }
                            400 -> {
                                Toast.makeText(this@ParamedicDetailActivity, "삭제 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this@ParamedicDetailActivity, "응답 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ParamedicDeleteResponse>, t: Throwable) {
                Toast.makeText(this@ParamedicDetailActivity, "통신 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
